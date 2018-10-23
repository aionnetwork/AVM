package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.persistence.keyvalue.StorageKeys;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class RevertAndInvalidTest {

    // transaction
    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    // block
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    // kernel & vm
    private KernelInterfaceImpl kernel;
    private Avm avm;

    private byte[] dappAddress;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
        
        dappAddress = deploy();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    private byte[] deploy() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RevertAndInvalidTestResource.class);
        byte[] arguments = null;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();

        return txResult.getReturnData();
    }

    @Test
    public void testRevert() {
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, new byte[]{1}, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();

        assertEquals(TransactionResult.Code.FAILED_REVERT, txResult.getStatusCode());
        assertNull(txResult.getReturnData());
        assertTrue(energyLimit > txResult.getEnergyUsed());

        assertArrayEquals(new byte[]{0,0,0,0, 0,0,0,4, 0,0,0,0}, kernel.getStorage(dappAddress, StorageKeys.CLASS_STATICS));
    }

    @Test
    public void testInvalid() {
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, new byte[]{2}, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();

        assertEquals(TransactionResult.Code.FAILED_INVALID, txResult.getStatusCode());
        assertNull(txResult.getReturnData());
        assertEquals(energyLimit, txResult.getEnergyUsed());

        assertArrayEquals(new byte[]{0,0,0,0, 0,0,0,4, 0,0,0,0}, kernel.getStorage(dappAddress, StorageKeys.CLASS_STATICS));
    }

}
