package org.aion.avm.tooling.shadowing.testInvoke;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

public class InvokeDynamicsTest {

    @Rule
    public AvmRule avmRule = new AvmRule(true);

    private Address from = avmRule.getPreminedAccount();
    private Address dappAddr;

    @Before
    public void setup() {
        byte[] txData = avmRule.getDappBytes (TestResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData).getDappAddress();
    }

    @Test
    public void applyLambda(){
        callStatic("applyLambda");
    }

    @Test
    public void applyMethodReference(){
        callStatic("applyMethodReference");
    }

    @Test
    public void applyMethodReferenceNewInstance(){
        callStatic("applyMethodReferenceNewInstance");
    }

    @Test
    public void applyLambdaVirtual(){
        callStatic("applyLambdaVirtual");
    }

    @Test
    public void applyMethodReferenceVirtual(){
        callStatic("applyMethodReferenceVirtual");
    }

    @Test
    public void applyMethodReferenceStatic(){
        callStatic("applyMethodReferenceStatic");
    }

    @Test
    public void applyLambdaStatic(){
        callStatic("applyLambdaStatic");
    }

    @Test
    public void applyMethodReferenceNewSpecial(){
        callStatic("applyMethodReferenceNewSpecial");
    }

    @Test
    public void applyLambdaSpecial(){
        callStatic("applyLambdaSpecial");
    }

    @Test
    public void applyMethodReferenceInterface(){
        callStatic("applyMethodReferenceInterface");
    }

    @Test
    public void applyMethodReferenceVirtual2(){
        callStatic("applyMethodReferenceVirtual2");
    }

    @Test
    public void applyMethodReferenceInnerClass(){
        callStatic("applyMethodReferenceInnerClass");
    }

    @Test
    public void applyMethodReferenceInterfacePrimitiveArg(){
        callStatic("applyMethodReferenceInterfacePrimitiveArg");
    }

    @Test
    public void applyException(){
        callStatic("applyException");
    }

    @Test
    public void applyMethodReferenceException(){
        callStatic("applyMethodReferenceException");
    }

    @Test
    public void apply1DArray(){
        callStatic("apply1DArray");
    }

    @Test
    public void apply2DArray(){
        callStatic("apply2DArray");
    }

    @Test
    public void applyObjectArray(){
        callStatic("applyObjectArray");
    }

    @Test
    public void applyInterfaceArray(){
        callStatic("applyInterfaceArray");
    }

    @Test
    public void applyReturnArray(){
        callStatic("applyReturnArray");
    }

    @Test
    public void applyOnFunction(){
        callStatic("applyOnFunction");
    }

    @Test
    public void applyOnFunction2(){
        callStatic("applyOnFunction2");
    }

    @Test
    public void applyBlockchainGetBalance(){
        callStatic("applyBlockchainGetBalance");
    }

    @Test
    public void applyMethodReferenceStaticForInstance(){
        callStatic("applyMethodReferenceStaticForInstance");
    }

    @Test
    public void applyLambdaPrimitiveInput(){
        callStatic("applyLambdaPrimitiveInput");
    }

    @Test
    public void applyLambdaArrayInput(){
        callStatic("applyLambdaArrayInput");
    }

    @Test
    public void applyLambdaTryCatch(){
        callStatic("applyLambdaTryCatch");
    }


    private void callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, data);
        Assert.assertTrue(result.getTransactionResult().getResultCode().isSuccess());
    }
}
