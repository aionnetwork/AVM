package org.aion.avm.core.miscvisitors;

import java.math.BigInteger;
import java.util.Collections;

import org.aion.avm.core.*;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.TestingBlock;
import org.aion.types.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.Assert.assertTrue;


public class StrictFPVisitorTest {
    // transaction
    private long energyLimit = 10_000_000L;
    private long energyPrice = 1L;

    private AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private AionAddress dappAddress;

    private TestingState kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingState(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(StrictFPVisitorTestResource.class, Collections.emptyMap());
        byte[] arguments = null;
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(this.kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();

        dappAddress = new AionAddress(txResult.copyOfTransactionOutput().orElseThrow());
        assertTrue(null != dappAddress);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testAccessFlag() {
        LoadedJar jar = LoadedJar.fromBytes(kernel.getTransformedCode(dappAddress));
        for (byte[] klass : jar.classBytesByQualifiedNames.values()) {
            ClassReader reader = new ClassReader(klass);
            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM6) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    boolean isAbstract = (Opcodes.ACC_ABSTRACT == (access & Opcodes.ACC_ABSTRACT));
                    boolean isStrict = (Opcodes.ACC_STRICT == (access & Opcodes.ACC_STRICT));
                    
                    // Must be one or the other but never both.
                    assertTrue(isAbstract ^ isStrict);
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            };
            reader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        }
    }

    @Test
    public void testStrictFP() {
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, new byte[0], energyLimit, energyPrice);
        TransactionResult txResult = avm.run(this.kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
    }

    @Test
    public void testDirect() {
        // Just call the main directly - if this passes, it means that we will be unable to detect if the strictfp has an effect.
        StrictFPVisitorTestResource.main();
    }
}
