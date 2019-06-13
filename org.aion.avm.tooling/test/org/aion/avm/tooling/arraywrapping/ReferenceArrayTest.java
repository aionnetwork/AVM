package org.aion.avm.tooling.arraywrapping;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.kernel.AvmTransactionResult;
import org.junit.*;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class ReferenceArrayTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(ReferenceArrayTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void test2DimArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("twoDimArraySize");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void test2DimArraySize2() {
        byte[] data = ABIUtil.encodeMethodArguments("twoDimArraySize2");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void test2DimArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("twoDimArrayAccess");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testMultiDimArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("multiDimArraySize");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }


    @Test
    public void testMultiDimArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("multiDimArrayAccess");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void InterfaceArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("InterfaceArrayAccess");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void InterfaceArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("InterfaceArraySize");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }


    @Test
    public void ObjectArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("ObjectArrayAccess");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void ObjectArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("ObjectArraySize");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

}
