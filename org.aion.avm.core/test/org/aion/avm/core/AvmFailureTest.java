package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.types.InternalTransaction;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class AvmFailureTest {
    // transaction
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    // block
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    // kernel & vm
    private KernelInterfaceImpl kernel;
    private Avm avm;

    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddress;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
        
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AvmFailureTestResource.class);
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
    public void testFailedTransaction() {
        byte[] data = ABIEncoder.encodeMethodArguments("reentrantCall", 5);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0L, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_REVERT, txResult.getStatusCode());
        assertEquals(5, txResult.getInternalTransactions().size());
        assertEquals(0, txResult.getLogs().size());

        for (InternalTransaction i : txResult.getInternalTransactions()) {
            assertTrue(i.isRejected());
        }
    }

    @Test
    public void testOutOfEnergy() {
        byte[] data = ABIEncoder.encodeMethodArguments("testOutOfEnergy");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0L, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_OUT_OF_ENERGY, txResult.getStatusCode());
    }

    @Test
    public void testOutOfStack() {
        byte[] data = ABIEncoder.encodeMethodArguments("testOutOfStack");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0L, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_OUT_OF_STACK, txResult.getStatusCode());
    }

    @Test
    public void testRevert() {
        byte[] data = ABIEncoder.encodeMethodArguments("testRevert");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0L, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_REVERT, txResult.getStatusCode());
        assertNotEquals(energyLimit, txResult.getEnergyUsed());
    }

    @Test
    public void testInvalid() {
        byte[] data = ABIEncoder.encodeMethodArguments("testInvalid");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0L, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_INVALID, txResult.getStatusCode());
        assertEquals(energyLimit, txResult.getEnergyUsed());
    }

    @Test
    public void testUncaughtException() {
        byte[] data = ABIEncoder.encodeMethodArguments("testUncaughtException");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0L, data, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_EXCEPTION, txResult.getStatusCode());
        assertTrue(txResult.getUncaughtException() instanceof RuntimeException);
    }
}
