package org.aion.avm.embed.blockchainruntime;

import avm.Address;
import org.aion.types.AionAddress;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.embed.AvmRule.ResultWrapper;
import org.aion.avm.tooling.ABIUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;


public class BlockchainGetDataTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Address dappAddr;

    @BeforeClass
    public static void setUp() {
        byte[] jar = avmRule.getDappBytes(BlockchainGetDataResource.class, new byte[0]);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getDappAddress();
    }

    private ResultWrapper call(String methodName, Object ...objects) {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments(methodName, objects);
        return avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
    }

    /**
     * We want to make sure the actual data object can't be modified by the data object returned by Blockchain.getData in DApp.
     * Object txData is passed to AvmRule, then to BlockchainRuntime.
     * We need to check txData is not modified after we modify the data object returned by Blockchain.getData inside DApp.
     */
    @Test
    public void testGetDataThenModify() {
        testGetDataThenModify(true);
    }

    @Test
    public void testGetDataThenNotModify() {
        testGetDataThenModify(false);
    }

    public void testGetDataThenModify(boolean isModify) {
        byte[] txData = ABIUtil.encodeMethodArguments("getDataAndModify", isModify);
        byte[] expected = Arrays.copyOf(txData, txData.length);

        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals(isModify, new String((byte[]) result.getDecodedReturnData()).startsWith("modified!"));
        Assert.assertArrayEquals(expected, txData);
    }

    @Test
    public void testGetDataAndCompare() {
        testGetAndCompare("getDataAndCompare");
    }

    private void testGetAndCompare(String methodName) {
        byte[] txData = ABIUtil.encodeMethodArguments(methodName);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertTrue((boolean) result.getDecodedReturnData());
    }

    /**
     * Object dappAddr is passed to AvmRule, then to BlockchainRuntime.
     */
    @Test
    public void testGetAddressThenModify() {
        testGetAddressThenModify(true);
    }

    @Test
    public void testGetAddressThenNotModify() {
        testGetAddressThenModify(false);
    }

    private void testGetAddressThenModify(boolean isModify) {
        Address expected = new Address(dappAddr.toByteArray().clone());

        ResultWrapper result = call("getAddressAndModify", isModify);

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        // AKI-143: It is now not possible to modify an Address.
        Assert.assertEquals(false, new String((byte[]) result.getDecodedReturnData()).startsWith("modified!"));
        Assert.assertArrayEquals(expected.toByteArray(), dappAddr.toByteArray());
    }

    @Test
    public void testGetAddressThenModifyWhenDeploy() {
        ResultWrapper result = call("getAddress");

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertArrayEquals(dappAddr.toByteArray(), (byte[]) result.getDecodedReturnData());
    }

    @Test
    public void testGetAddressAndCompare() {
        testGetAndCompare("getAddressAndCompare");
    }

    /**
     * The test logic is the same as getData.
     */
    @Test
    public void testGetCallerThenModify() {
        testGetCallerThenModify(true);
    }

    @Test
    public void testGetCallerThenNotModify() {
        testGetCallerThenModify(false);
    }

    private void testGetCallerThenModify(boolean isModify) {
        Address expected = new Address(from.toByteArray().clone());

        ResultWrapper result = call("getCallerAndModify", isModify);

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        // AKI-143: It is now not possible to modify an Address.
        Assert.assertEquals(false, new String((byte[]) result.getDecodedReturnData()).startsWith("modified!"));
        Assert.assertArrayEquals(expected.toByteArray(), from.toByteArray());
    }

    @Test
    public void testGetCallerAndCompare() {
        testGetAndCompare("getCallerAndCompare");
    }

    /**
     * The test logic is the same as getData.
     */
    @Test
    public void testGetOriginThenModify() {
        testGetOriginThenModify(true);
    }

    @Test
    public void testGetOriginThenNotModify() {
        testGetOriginThenModify(false);
    }

    private void testGetOriginThenModify(boolean isModify) {
        Address expected = new Address(from.toByteArray().clone());

        ResultWrapper result = call("getOriginAndModify", isModify);

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        // AKI-143: It is now not possible to modify an Address.
        Assert.assertEquals(false, new String((byte[]) result.getDecodedReturnData()).startsWith("modified!"));
        Assert.assertArrayEquals(expected.toByteArray(), from.toByteArray());
    }

    @Test
    public void testGetOriginAndCompare() {
        testGetAndCompare("getOriginAndCompare");
    }

    /**
     * The test logic is the same as getData.
     */
    @Test
    public void testGetBlockCoinbaseThenModify() {
        testGetBlockCoinbaseThenModify(true);
    }

    @Test
    public void testGetBlockCoinbaseThenNotModify() {
        testGetBlockCoinbaseThenModify(false);
    }

    private void testGetBlockCoinbaseThenModify(boolean isModify) {
        AionAddress blockCoinbase = avmRule.kernel.getMinerAddress();
        Address expected = new Address(blockCoinbase.toByteArray());

        ResultWrapper result = call("getBlockCoinbaseAndModify", isModify);

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        // AKI-143: It is now not possible to modify an Address.
        Assert.assertEquals(false, new String((byte[]) result.getDecodedReturnData()).startsWith("modified!"));
        Assert.assertArrayEquals(expected.toByteArray(), blockCoinbase.toByteArray());
    }

    @Test
    public void testGetBlockCoinbaseAndCompare() {
        testGetAndCompare("getBlockCoinbaseAndCompare");
    }

    /**
     * The test logic is the same as getData.
     */
    @Test
    public void testGetBlockDifficultyThenModify() {
        ResultWrapper result = call("getBlockDifficulty");

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals(BigInteger.valueOf(avmRule.kernel.getBlockDifficulty()),
                new BigInteger((byte[])result.getDecodedReturnData()));
    }

    @Test
    public void testGetBlockDifficultyAndCompare() {
        testGetAndCompare("getBlockDifficultyAndCompare");
    }
}
