package org.aion.avm.core.miscvisitors;

import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.ACC_STRICT;


public class StrictFPVisitorTest {
    // transaction
    private long energyLimit = 10_000_000L;
    private long energyPrice = 1L;

    // block
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddress;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
        
        byte[] jar = JarBuilder.buildJarForMainAndClasses(StrictFPVisitorTestResource.class);
        byte[] arguments = null;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), 0L, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        dappAddress = txResult.getReturnData();
        assertTrue(null != dappAddress);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testAccessFlag() {
        LoadedJar jar = LoadedJar.fromBytes(kernel.getCode(dappAddress));
        for (byte[] klass : jar.classBytesByQualifiedNames.values()) {
            ClassReader reader = new ClassReader(klass);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_FRAMES);
            assertTrue((node.access & ACC_STRICT) != 0);
        }
    }

    @Test
    public void testFp() {
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0, new byte[0], energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);
        assertTrue(txResult.getStatusCode().isSuccess());
    }
}
