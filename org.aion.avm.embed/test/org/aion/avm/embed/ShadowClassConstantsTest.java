package org.aion.avm.embed;

import avm.Address;
import org.aion.avm.embed.AvmRule.ResultWrapper;
import org.aion.avm.tooling.ABIUtil;
import org.aion.types.TransactionResult;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class ShadowClassConstantsTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(ShadowClassConstantsTarget.class, null);
        ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void testIdentitiesOfBigIntegerConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkBigIntegerConstants");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testIdentitiesOfRoundingModeConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkRoundingModeConstants");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testIdentitiesOfMathContextConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkMathContextConstants");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testIdentitiesOfBooleanConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkBooleanConstants");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testIdentitiesOfPrimitiveTypeConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkPrimitiveTypeConstants");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void checkTimeUnitConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkTimeUnitConstants");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

}