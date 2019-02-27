package org.aion.avm.tooling.arraywrapping;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.tooling.AvmRule;
import org.junit.*;


public class ArrayWrappingTest {

    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private Address from = avmRule.getPreminedAccount();
    private Address dappAddr;

    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    @Before
    public void setup() {
        byte[] txData = avmRule.getDappBytes(TestResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testBooleanArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testBooleanArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testByteArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testByteArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testCharArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testCharArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testDoubleArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testDoubleArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testFloatArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testFloatArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testIntArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testIntArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testLongArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testLongArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testShortArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testShortArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testObjectArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testObjectArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testStringArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testStringArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testSignature() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testSignature");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testVarargs() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testVarargs");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testTypeChecking() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testTypeChecking");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testClassField() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testClassField");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiInt() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiInt");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiByte() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiByte");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiChar() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiChar");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiFloat() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiFloat");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiLong() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiLong");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiDouble() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiDouble");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testMultiRef() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMultiRef");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testHierarachy() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testHierarachy");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testIncompleteArrayIni() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testIncompleteArrayIni");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayEnergy() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayEnergy");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testInterfaceArray() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testInterfaceArray");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayClone() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayClone");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }
}
