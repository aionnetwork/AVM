package org.aion.avm.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.types.AionAddress;
import org.aion.avm.RuntimeMethodFeeSchedule;
import org.aion.avm.StorageFees;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.junit.*;

public class KeyValueStoreTest {
    // transaction
    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 1L;

    // kernel & vm
    private static TestingKernel kernel;
    private static AvmImpl avm;

    private static AionAddress deployer = TestingKernel.PREMINED_ADDRESS;
    private static AionAddress dappAddress;



    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(block);

        AvmConfiguration avmConfig = new AvmConfiguration();
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), avmConfig);

        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(KeyValueStoreTestTarget.class);
        AvmTransaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        dappAddress = new AionAddress(txResult.getReturnData());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testGetStorageKeyNotExist() {
        kernel.generateBlock();
        byte[] key = Helpers.randomBytes(32);
        byte[] data = encodeOptionalArgsMethodCall("testAvmGetStorage", key, null);
        AvmTransaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(null, txResult.getReturnData());
    }

    @Test
    public void testGetStorageWrongSizeKey() {
        kernel.generateBlock();
        byte[] key = Helpers.randomBytes(33);
        byte[] data = encodeOptionalArgsMethodCall("testAvmGetStorage", key, null);
        AvmTransaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.FAILED_EXCEPTION, txResult.getResultCode());
    }

    @Test
    public void testPutStorageWrongSizeKey() {
        kernel.generateBlock();
        byte[] key = Helpers.randomBytes(33);
        byte[] value = Helpers.randomBytes(32);

        byte[] data = encodeOptionalArgsMethodCall("testAvmPutStorage", key, value);
        AvmTransaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.FAILED_EXCEPTION, txResult.getResultCode());
    }

    @Test
    public void testPutGetStorageSuccess() {
        kernel.generateBlock();
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(32);

        byte[] data = encodeOptionalArgsMethodCall("testAvmPutStorage", key, value);
        AvmTransaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());
        kernel.generateBlock();

        data = encodeOptionalArgsMethodCall("testAvmGetStorage", key, null);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(value, txResult.getReturnData());
    }

    @Test
    public void testStoragePutNullDelete() {
        kernel.generateBlock();
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(32);

        byte[] data = encodeOptionalArgsMethodCall("testAvmPutStorage", key, value);
        AvmTransaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());
        kernel.generateBlock();

        data = encodeOptionalArgsMethodCall("testAvmGetStorage", key, null);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(value, txResult.getReturnData());
        kernel.generateBlock();

        // put null to delete
        data = encodeOptionalArgsMethodCall("testAvmPutStorageNullValue", key, null);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());
        kernel.generateBlock();

        data = encodeOptionalArgsMethodCall("testAvmGetStorage", key, null);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();

        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(null, txResult.getReturnData());
    }

    @Test
    public void testStorageBilling() {
        kernel.generateBlock();
        byte[] key = new byte[]{93, -35, 110, 84, -89, -100, 19, 127, -13, 28, -39, 74, -110, -26, 13, -22, -30, 108, 115, 17, 57, 54, 74, -90, 45, -35, -21, 39, 109, -3, 111, -105};
        byte[] value= new byte[]{124, 22, 37, 17, -40, 97, -46, -103, -38, 48, 46, -115, -78, -5, -116, -15, 81, 94, -61, 52, -68, 73, -35, -34, -44, -82, -43, 68, -32, 100, -124, -124};

        // zero -> zero
        byte[] data = encodeOptionalArgsMethodCall("testAvmPutStorageNullValue", key, null);
        AvmTransaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());
        long deleteZeroCost = txResult.getEnergyUsed();
        assertEquals(51680L + RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage, deleteZeroCost);
        kernel.generateBlock();

        // zero -> nonzero
        data = encodeOptionalArgsMethodCall("testAvmPutStorage", key, value);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());
        long setStorageCost = txResult.getEnergyUsed();
        assertEquals(55007L + RuntimeMethodFeeSchedule.BlockchainRuntime_avm_setStorage + StorageFees.WRITE_PRICE_PER_BYTE * value.length, setStorageCost);
        kernel.generateBlock();

        // nonzero -> nonzero
        data = encodeOptionalArgsMethodCall("testAvmPutStorage", key, value);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());
        long modifyStorageCost = txResult.getEnergyUsed();
        assertEquals(55007L + RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage + StorageFees.WRITE_PRICE_PER_BYTE * value.length, modifyStorageCost);
        // set storage cost 20000 + linear factor cost, modify storage cost 5000
        assertEquals(15000L, setStorageCost - modifyStorageCost);
        kernel.generateBlock();

        // get nonzero
        data = encodeOptionalArgsMethodCall("testAvmGetStorage", key, null);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        long getStorageCost = txResult.getEnergyUsed();
        assertEquals(49283L + RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getStorage + StorageFees.READ_PRICE_PER_BYTE * value.length, getStorageCost);
        kernel.generateBlock();

        // nonzero -> zero
        data = encodeOptionalArgsMethodCall("testAvmPutStorageNullValue", key, null);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(new byte[0], txResult.getReturnData());
        long deleteStorageCost = txResult.getEnergyUsed();
        assertEquals(51680 + RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage - RuntimeMethodFeeSchedule.BlockchainRuntime_avm_deleteStorage_refund, deleteStorageCost);
        // both deletion cost 5000, but deleting a non-zero value gets 20000 refund
        assertEquals(15000L, deleteZeroCost - deleteStorageCost);
        kernel.generateBlock();

        // get zero
        data = encodeOptionalArgsMethodCall("testAvmGetStorage", key, null);
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        txResult = avm.run(kernel, new AvmTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        assertArrayEquals(null, txResult.getReturnData());
        long getZeroCost = txResult.getEnergyUsed();
        assertEquals(49283 + RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getStorage, getZeroCost);
        assertEquals(value.length * StorageFees.READ_PRICE_PER_BYTE, getStorageCost - getZeroCost);
    }


    private static byte[] encodeOptionalArgsMethodCall(String methodName, byte[] key, byte[] value) {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString(methodName);
        if (null != key) {
            encoder.encodeOneByteArray(key);
        }
        if (null != value) {
            encoder.encodeOneByteArray(value);
        }
        return encoder.toBytes();
    }
}
