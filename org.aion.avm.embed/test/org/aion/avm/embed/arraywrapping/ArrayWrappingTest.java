package org.aion.avm.embed.arraywrapping;

import java.math.BigInteger;
import avm.Address;

import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.junit.*;


public class ArrayWrappingTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    private static long energyLimit = 6_000_0000;
    private static long energyPrice = 1;

    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes(TestResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testBooleanArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testBooleanArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testByteArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testByteArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testCharArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testCharArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testDoubleArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testDoubleArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testFloatArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testFloatArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testIntArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testIntArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testLongArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testLongArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testShortArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testShortArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testObjectArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testObjectArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testStringArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testStringArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testSignature() {
        byte[] txData = ABIUtil.encodeMethodArguments("testSignature");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testVarargs() {
        byte[] txData = ABIUtil.encodeMethodArguments("testVarargs");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testTypeChecking() {
        byte[] txData = ABIUtil.encodeMethodArguments("testTypeChecking");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testClassField() {
        byte[] txData = ABIUtil.encodeMethodArguments("testClassField");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiInt() {
        byte[] txData = ABIUtil.encodeMethodArguments("testMultiInt");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiByte() {
        byte[] txData = ABIUtil.encodeMethodArguments("testMultiByte");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiChar() {
        byte[] txData = ABIUtil.encodeMethodArguments("testMultiChar");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiFloat() {
        byte[] txData = ABIUtil.encodeMethodArguments("testMultiFloat");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiLong() {
        byte[] txData = ABIUtil.encodeMethodArguments("testMultiLong");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiDouble() {
        byte[] txData = ABIUtil.encodeMethodArguments("testMultiDouble");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiRef() {
        byte[] txData = ABIUtil.encodeMethodArguments("testMultiRef");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testHierarachy() {
        byte[] txData = ABIUtil.encodeMethodArguments("testHierarachy");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testIncompleteArrayIni() {
        byte[] txData = ABIUtil.encodeMethodArguments("testIncompleteArrayIni");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayEnergy() {
        byte[] txData = ABIUtil.encodeMethodArguments("testArrayEnergy");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testInterfaceArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testInterfaceArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayClone() {
        byte[] txData = ABIUtil.encodeMethodArguments("testArrayClone");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testInt2DArray() {
        byte[] txData = ABIUtil.encodeMethodArguments("testInt2DArray");
        int[][] result = (int[][]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertArrayEquals(new int[][] {{1, 2}, {3, 4}}, result);
    }

    @Test
    public void testBooleanSignature(){
        boolean[] b = new boolean[10];
        b[0] = true;
        byte[] txData = ABIUtil.encodeMethodArguments("testBooleanSignature", b);
        boolean[] result = (boolean [])avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result);
    }

    @Test
    public void testByteSignature(){
        byte[] b = new byte[10];
        byte[] txData = ABIUtil.encodeMethodArguments("testByteSignature", b);
        byte[] result = (byte[]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result);
    }

    @Test
    public void testCharSignature(){
        char[] b = new char[10];
        b[0] = 'a';
        byte[] txData = ABIUtil.encodeMethodArguments("testCharSignature", b);
        char[] result = (char[]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result);
    }

    @Test
    public void testDoubleSignature(){
        double[] b = new double[10];
        b[0] = 1d;
        byte[] txData = ABIUtil.encodeMethodArguments("testDoubleSignature", b);
        double[] result = (double[]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result, 0);
    }

    @Test
    public void testFloatSignature(){
        float[] b = new float[10];
        b[0] = 1.0f;
        byte[] txData = ABIUtil.encodeMethodArguments("testFloatSignature", b);
        float[] result = (float[]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result, 0);
    }

    @Test
    public void testIntSignature(){
        int[] b = new int[10];
        b[0] = 1;
        byte[] txData = ABIUtil.encodeMethodArguments("testIntSignature", b);
        int[] result = (int[]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result);
    }

    @Test
    public void testLongSignature(){
        long[] b = new long[10];
        b[0] = 1L;
        byte[] txData = ABIUtil.encodeMethodArguments("testLongSignature", b);
        long[] result = (long[]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result);
    }

    @Test
    public void testShortSignature(){
        short[] b = new short[10];
        b[0] = 1;
        byte[] txData = ABIUtil.encodeMethodArguments("testShortSignature", b);
        short[] result = (short[]) avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertArrayEquals(b, result);
    }
}
