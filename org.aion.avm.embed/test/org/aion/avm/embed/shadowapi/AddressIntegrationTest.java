package org.aion.avm.embed.shadowapi;

import avm.Address;

import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.junit.*;

import java.math.BigInteger;


public class AddressIntegrationTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Address dappAddr;

    @BeforeClass
    public static void setUp() {
        byte[] jar = avmRule.getDappBytes(AddressTestTarget.class, new byte[0]);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testToString() {
        byte[] data = createByteArray(0);

        Object result = call("getToString", new Address(data));

        Assert.assertEquals("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", result);
    }

    @Test
    public void testEquals() {
        byte[] data = createByteArray(0);
        byte[] data1 = createByteArray(1);

        Object resultEqual = call("getEquals", new Address(data), new Address(data));
        Assert.assertEquals(true, resultEqual);

        Object resultUnequal = call("getEquals", new Address(data), new Address(data1));
        Assert.assertEquals(false, resultUnequal);
    }

    @Test
    public void testHashCode() {
        byte[] data = createByteArray(0);
        Object result = call("getHashCode", new Address(data));
        Assert.assertEquals(496, result);

        byte[] data1 = createByteArray(1);
        result = call("getHashCode", new Address(data1));
        Assert.assertEquals(528, result);
    }

    private byte[] createByteArray(int startValue) {
        byte[] data = new byte[32];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte)(i + startValue);
        }

        return data;
    }

    private Object call(String methodName, Address ...addresses) {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments(methodName, (Object[]) addresses);
        return avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice).getDecodedReturnData();
    }
}
