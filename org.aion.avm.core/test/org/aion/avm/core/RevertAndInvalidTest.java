package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.persistence.keyvalue.StorageKeys;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;


public class RevertAndInvalidTest {

    // transaction
    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    // block
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    // kernel & vm
    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    private byte[] dappAddress;

    public RevertAndInvalidTest() {
        dappAddress = deploy();
    }

    private byte[] deploy() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RevertAndInvalidTestResource.class);
        byte[] arguments = null;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), 0L, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        return txResult.getReturnData();
    }

    @Test
    public void testRevert() {
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0, new byte[]{1}, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_REVERT, txResult.getStatusCode());
        assertNull(txResult.getReturnData());
        assertTrue(energyLimit > txResult.getEnergyUsed());

        dumpStorage(dappAddress);
        assertArrayEquals(new byte[]{0,0,0,0, 0,0,0,4, 0,0,0,0}, kernel.getStorage(dappAddress, StorageKeys.CLASS_STATICS));
    }

    @Test
    public void testInvalid() {
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), 0, new byte[]{2}, energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(tx, block);
        TransactionResult txResult = avm.run(txContext);

        assertEquals(TransactionResult.Code.FAILED_INVALID, txResult.getStatusCode());
        assertNull(txResult.getReturnData());
        assertEquals(energyLimit, txResult.getEnergyUsed());

        dumpStorage(dappAddress);
        assertArrayEquals(new byte[]{0,0,0,0, 0,0,0,4, 0,0,0,0}, kernel.getStorage(dappAddress, StorageKeys.CLASS_STATICS));
    }

    private void dumpStorage(byte[] dappAddress) {
        Map<ByteArrayWrapper, byte[]> s = kernel.getStorageEntries(dappAddress);
        for (ByteArrayWrapper k : s.keySet()) {
            System.out.println(k + "=" + Helpers.bytesToHexString(s.get(k)));
        }
    }
}
