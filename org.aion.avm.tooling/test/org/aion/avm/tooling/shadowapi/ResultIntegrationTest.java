package org.aion.avm.tooling.shadowapi;

import org.aion.avm.api.Address;
import org.aion.avm.api.Result;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


public class ResultIntegrationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address from = avmRule.getPreminedAccount();
    private Address dappAddr;

    @Before
    public void setUp() {
        byte[] jar = avmRule.getDappBytes(ResultTestTarget.class, new byte[0]);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, jar).getDappAddress();
    }

    @Test
    public void testToString() {
        Object result = call("getToStringSuccessTrue");
        Assert.assertEquals(
                "success:true, returnData:000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f",
                result.toString());

        result = call("getToStringSuccessFalse");
        Assert.assertEquals(
                "success:false, returnData:000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f",
                result.toString());
    }

    @Test
    public void testEquals() {
        Object result = call("getEquals");
        Assert.assertEquals(true, result);

        result = call("getUnequalsSameSuccessDiffData");
        Assert.assertEquals(false, result);

        result = call("getUnequalsDiffSuccessSameData");
        Assert.assertEquals(false, result);
    }

    @Test
    public void testHashCode() {
        Object result = call("getHashCodeSuccessTrue");
        Assert.assertEquals(497, result);

        result = call("getHashCodeSuccessFalse");
        Assert.assertEquals(496, result);
    }

    private Object call(String methodName, Result ...results) {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments(methodName, (Object[]) results);
        return avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments).getDecodedReturnData();
    }
}