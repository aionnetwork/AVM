package org.aion.avm.tooling.arraywrapping;

import org.aion.avm.api.ABIEncoder;
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
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data, 5_000_000, 1);
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = avmRule.deploy(sender, value, data, 5_000_000, 1).getDappAddress();
    }

    @Test
    public void test2DimArraySize() {
        byte[] data = ABIEncoder.encodeMethodArguments("twoDimArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void test2DimArraySize2() {
        byte[] data = ABIEncoder.encodeMethodArguments("twoDimArraySize2");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void test2DimArrayAccess() {
        byte[] data = ABIEncoder.encodeMethodArguments("twoDimArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testMultiDimArraySize() {
        byte[] data = ABIEncoder.encodeMethodArguments("multiDimArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }


    @Test
    public void testMultiDimArrayAccess() {
        byte[] data = ABIEncoder.encodeMethodArguments("multiDimArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void InterfaceArrayAccess() {
        byte[] data = ABIEncoder.encodeMethodArguments("InterfaceArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void InterfaceArraySize() {
        byte[] data = ABIEncoder.encodeMethodArguments("InterfaceArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }


    @Test
    public void ObjectArrayAccess() {
        byte[] data = ABIEncoder.encodeMethodArguments("ObjectArrayAccess");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void ObjectArraySize() {
        byte[] data = ABIEncoder.encodeMethodArguments("ObjectArraySize");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
    }

}
