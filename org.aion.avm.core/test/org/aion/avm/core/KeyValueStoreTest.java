package org.aion.avm.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KeyValueStoreTest {
    // transaction
    private long energyLimit = 10_000_000L;
    private long energyPrice = 1L;

    // block
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    // kernel & vm
    private TestingKernel kernel;
    private AvmImpl avm;

    private Address deployer = TestingKernel.PREMINED_ADDRESS;
    private Address dappAddress;


    @Before
    public void setup() {
        this.kernel = new TestingKernel(block);

        AvmConfiguration avmConfig = new AvmConfiguration();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), avmConfig);

        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(KeyValueStoreTestTarget.class);
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(this.kernel, new Transaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        dappAddress = Address.wrap(txResult.getReturnData());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testGetStorageKeyNotExist() {
        byte[] key = Helpers.randomBytes(32);
        byte[] data = ABIUtil.encodeMethodArguments("testAvmGetStorage", key);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(null, txResult.getReturnData());
    }

    @Test
    public void testGetStorageWrongSizeKey() {
        byte[] key = Helpers.randomBytes(33);
        byte[] data = ABIUtil.encodeMethodArguments("testAvmGetStorage", key);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.FAILED_EXCEPTION, txResult.getResultCode());
    }

    @Test
    public void testPutStorageWrongSizeKey() {
        byte[] key = Helpers.randomBytes(33);
        byte[] value = Helpers.randomBytes(32);

        byte[] data = ABIUtil.encodeMethodArguments("testAvmPutStorage", key, value);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.FAILED_EXCEPTION, txResult.getResultCode());
    }

    @Test
    public void testPutGetStorageSuccess() {
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(32);

        byte[] data = ABIUtil.encodeMethodArguments("testAvmPutStorage", key, value);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());

        data = ABIUtil.encodeMethodArguments("testAvmGetStorage", key);
        tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(value, txResult.getReturnData());
    }

    @Test
    public void testStoragePutNullDelete() {
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(32);

        byte[] data = ABIUtil.encodeMethodArguments("testAvmPutStorage", key, value);
        Transaction tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());

        data = ABIUtil.encodeMethodArguments("testAvmGetStorage", key);
        tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(value, txResult.getReturnData());

        // put null to delete
        data = ABIUtil.encodeMethodArguments("testAvmPutStorageNullValue", key);
        tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());

        data = ABIUtil.encodeMethodArguments("testAvmGetStorage", key);
        tx = Transaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(null, txResult.getReturnData());

    }
}
