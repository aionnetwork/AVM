package org.aion.avm.tooling.shadowing.testSystem;

import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


public class SystemShadowingTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private Address from = avmRule.getPreminedAccount();
    private Address dappAddr;

    @Before
    public void setup() {
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, avmRule.getDappBytes(TestResource.class, null)).getDappAddress();
    }

    @Test
    public void testArrayCopy() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayCopy");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayCopyNullPointerException() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayCopyNullPointerException");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayCopyIndexOutOfBoundsException() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayCopyIndexOutOfBoundsException");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayCopyArrayStoreException() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayCopyArrayStoreException");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }
}