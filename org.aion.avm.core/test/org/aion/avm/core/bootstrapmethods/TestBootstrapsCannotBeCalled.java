package org.aion.avm.core.bootstrapmethods;

import static junit.framework.TestCase.assertTrue;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestBootstrapsCannotBeCalled {
    private Avm avm;
    private KernelInterface kernel;
    private Block block;
    private byte[] deployer;

    @Before
    public void setup() {
        kernel = new KernelInterfaceImpl();
        avm = NodeEnvironment.singleton.buildAvmInstance(kernel);
        block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    }

    @After
    public void tearDown() {
        avm.shutdown();
        avm = null;
        kernel = null;
        block = null;
        deployer = null;
    }

    @Test
    public void testStringConcatFactoryMakeConcat() {
        TransactionResult result = deployContract(MakeConcatTarget.class, kernel.getNonce(deployer));
        assertTrue(result.getStatusCode().isFailed());
    }

    @Test
    public void testStringConcatFactoryMakeConcatWithConstants() {
        TransactionResult result = deployContract(MakeConcatWithConstantsTarget.class, kernel.getNonce(deployer));
        assertTrue(result.getStatusCode().isFailed());
    }

    @Test
    public void testLambdaMetaFactory() {
        TransactionResult result = deployContract(MetaFactoryTarget.class, kernel.getNonce(deployer));
        assertTrue(result.getStatusCode().isFailed());
    }

    private TransactionResult deployContract(Class<?> contract, long nonce) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(contract);
        byte[] createData = new CodeAndArguments(jar, null).encodeToBytes();
        Transaction transaction = Transaction.create(
            deployer,
            nonce,
            BigInteger.ZERO,
            createData,
            6_000_000,
            1);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

}
