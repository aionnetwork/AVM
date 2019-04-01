package org.aion.avm.tooling.arraywrapping;

import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.api.Address;
import org.aion.avm.tooling.AvmRule;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.*;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class ReferenceArrayTest {

    @Rule
    public AvmRule avmRule = new AvmRule(true);

    private final Address sender = avmRule.getPreminedAccount();
    private final BigInteger value = BigInteger.ZERO;
    private Address contract;

    @Before
    public void setup() {
        byte[] data = avmRule.getDappBytes(ReferenceArrayTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void test2DimArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("twoDimArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void test2DimArraySize2() {
        byte[] data = ABIUtil.encodeMethodArguments("twoDimArraySize2");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void test2DimArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("twoDimArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testMultiDimArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("multiDimArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }


    @Test
    public void testMultiDimArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("multiDimArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void InterfaceArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("InterfaceArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void InterfaceArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("InterfaceArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }


    @Test
    public void ObjectArrayAccess() {
        byte[] data = ABIUtil.encodeMethodArguments("ObjectArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void ObjectArraySize() {
        byte[] data = ABIUtil.encodeMethodArguments("ObjectArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

}
