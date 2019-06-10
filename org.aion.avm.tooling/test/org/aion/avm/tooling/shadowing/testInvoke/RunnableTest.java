package org.aion.avm.tooling.shadowing.testInvoke;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.*;

import java.math.BigInteger;

public class RunnableTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes (RunnableResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData).getDappAddress();
    }

    @Test
    public void onPrimitive(){
        callStatic("onPrimitive");
    }

    @Test
    public void onAionList(){
        callStatic("onAionList");
    }

    @Test
    public void onArray(){
        callStatic("onArray");
    }

    @Test
    public void onStatic(){
        callStatic("onStatic");
    }

    @Test
    public void onFunction(){
        callStatic("onFunction");
    }

    @Test
    public void onStaticFunction(){
        callStatic("onStaticFunction");
    }

    @Test
    public void onNewInvokeSpecialFunction(){
        callStatic("onNewInvokeSpecialFunction");
    }

    @Test
    public void onNewObject(){
        callStatic("onNewObject");
    }

    private void callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, data);
        Assert.assertTrue(result.getTransactionResult().getResultCode().isSuccess());
    }
}
