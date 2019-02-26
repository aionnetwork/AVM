package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.InternalTransactionInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
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
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    // kernel & vm
    private KernelInterfaceImpl kernel;
    private AvmImpl avm;

    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddress;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
        
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AvmFailureTestResource.class);
        byte[] arguments = null;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        TransactionResult txResult = avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();

        dappAddress = AvmAddress.wrap(txResult.getReturnData());
        assertTrue(null != dappAddress);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testFailedTransaction() {
        byte[] data = ABIEncoder.encodeMethodArguments("reentrantCall", 5);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_REVERT, txResult.getResultCode());
        assertEquals(5, txContext.getSideEffects().getInternalTransactions().size());
        assertEquals(0, txContext.getSideEffects().getExecutionLogs().size());

        for (InternalTransactionInterface i : txContext.getSideEffects().getInternalTransactions()) {
            assertTrue(i.isRejected());
        }
    }

    @Test
    public void testOutOfEnergy() {
        byte[] data = ABIEncoder.encodeMethodArguments("testOutOfEnergy");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        TransactionResult txResult = avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY, txResult.getResultCode());
    }

    @Test
    public void testOutOfStack() {
        byte[] data = ABIEncoder.encodeMethodArguments("testOutOfStack");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        TransactionResult txResult = avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_STACK, txResult.getResultCode());
    }

    @Test
    public void testRevert() {
        byte[] data = ABIEncoder.encodeMethodArguments("testRevert");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_REVERT, txResult.getResultCode());
        assertNotEquals(energyLimit, txResult.getEnergyUsed());
        assertNotEquals(0, txResult.getEnergyRemaining());
    }

    @Test
    public void testInvalid() {
        byte[] data = ABIEncoder.encodeMethodArguments("testInvalid");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_INVALID, txResult.getResultCode());
        assertEquals(energyLimit, txResult.getEnergyUsed());
        assertEquals(0, txResult.getEnergyRemaining());
    }

    @Test
    public void testUncaughtException() {
        byte[] data = ABIEncoder.encodeMethodArguments("testUncaughtException");
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, txResult.getResultCode());
        assertTrue(txResult.getUncaughtException() instanceof RuntimeException);
    }
}
