package org.aion.avm.tooling.bootstrapmethods;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class StringConcatTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(StringConcatTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void concatWithPrimitiveArray(){
        Assert.assertTrue(callStatic("concatWithPrimitiveArray"));
    }

    @Test
    public void concatWithPrimitiveArrays(){
        Assert.assertTrue(callStatic("concatWithArrays"));
    }

    @Test
    public void concatWithObjectArray(){
        Assert.assertTrue(callStatic("concatWithObjectArray"));
    }

    @Test
    public void concatWithInterfaceArray(){
        Assert.assertTrue(callStatic("concatWithInterfaceArray"));
    }

    @Test
    public void concatWithUserDefinedArray(){
        Assert.assertTrue(callStatic("concatWithUserDefinedArray"));
    }

    @Test
    public void concatWithMultiDimArray(){
        Assert.assertTrue(callStatic("concatWithMultiDimArray"));
    }

    @Test
    public void concatWithDynamicBoolean(){
        Assert.assertTrue(callStatic("concatWithDynamicBoolean"));
    }

    @Test
    public void concatWithAddress(){
        Assert.assertTrue(callStatic("concatWithAddress", avmRule.getPreminedAccount()));
    }

    @Test
    public void concat(){
        Assert.assertTrue(callStatic("concat"));
    }

    private boolean callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        return (boolean) avmRule.call(sender, contract, value, data, 2_000_000, 1).getDecodedReturnData();
    }

}
