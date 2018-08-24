package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AvmFailureTest {

    // transaction
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    // block
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    // kernel & vm
    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddress;

    public AvmFailureTest() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AvmFailureTestResource.class);
        byte[] arguments = null;
        Transaction tx = new Transaction(Transaction.Type.CREATE, deployer, null, 0, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        dappAddress = txResult.getReturnData();
    }

    @Test
    public void testOutOfEnergy() {
        byte[] data = ABIEncoder.encodeMethodArguments("testOutOfEnergy");
        Transaction tx = new Transaction(Transaction.Type.CALL, deployer, dappAddress, 0, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_OUT_OF_ENERGY, txResult.getStatusCode());
    }

    @Test
    public void testOutOfStack() {
        byte[] data = ABIEncoder.encodeMethodArguments("testOutOfStack");
        Transaction tx = new Transaction(Transaction.Type.CALL, deployer, dappAddress, 0, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_OUT_OF_STACK, txResult.getStatusCode());
    }

    @Test
    public void testRevert() {
        byte[] data = ABIEncoder.encodeMethodArguments("testRevert");
        Transaction tx = new Transaction(Transaction.Type.CALL, deployer, dappAddress, 0, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_REVERT, txResult.getStatusCode());
    }

    @Test
    public void testInvalid() {
        byte[] data = ABIEncoder.encodeMethodArguments("testInvalid");
        Transaction tx = new Transaction(Transaction.Type.CALL, deployer, dappAddress, 0, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_INVALID, txResult.getStatusCode());
    }

    @Test
    public void testUncaughtException() {
        byte[] data = ABIEncoder.encodeMethodArguments("testUncaughtException");
        Transaction tx = new Transaction(Transaction.Type.CALL, deployer, dappAddress, 0, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_EXCEPTION, txResult.getStatusCode());
    }
}
