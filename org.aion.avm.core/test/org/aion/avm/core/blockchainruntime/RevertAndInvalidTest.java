package org.aion.avm.core.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.RevertAndInvalidTestResource;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.persistence.keyvalue.StorageKeys;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class RevertAndInvalidTest {

    // transaction
    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    // block
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    // kernel & vm
    private KernelInterfaceImpl kernel;
    private VirtualMachine avm;

    private org.aion.vm.api.interfaces.Address dappAddress;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        
        dappAddress = deploy();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    private org.aion.vm.api.interfaces.Address deploy() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RevertAndInvalidTestResource.class);
        byte[] arguments = null;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();

        return AvmAddress.wrap(txResult.getReturnData());
    }

    @Test
    public void testRevert() {
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, new byte[]{1}, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_REVERT, txResult.getResultCode());
        assertNull(txResult.getReturnData());
        assertTrue(energyLimit > txResult.getEnergyUsed());
        assertTrue(0 < txResult.getEnergyRemaining());

        assertArrayEquals(new byte[]{0,0,0,0, 0,0,0,4, 0,0,0,0}, kernel.getStorage(dappAddress, StorageKeys.CLASS_STATICS));
    }

    @Test
    public void testInvalid() {
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, new byte[]{2}, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(new TransactionContext[] {txContext})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_INVALID, txResult.getResultCode());
        assertNull(txResult.getReturnData());
        assertEquals(energyLimit, txResult.getEnergyUsed());
        assertEquals(0, txResult.getEnergyRemaining());

        assertArrayEquals(new byte[]{0,0,0,0, 0,0,0,4, 0,0,0,0}, kernel.getStorage(dappAddress, StorageKeys.CLASS_STATICS));
    }

}
