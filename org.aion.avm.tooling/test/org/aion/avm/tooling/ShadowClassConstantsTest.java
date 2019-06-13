package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.tooling.AvmRule.ResultWrapper;
import org.aion.kernel.AvmTransactionResult;
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
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void testIdentitiesOfBigIntegerConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkBigIntegerConstants");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testIdentitiesOfRoundingModeConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkRoundingModeConstants");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testIdentitiesOfMathContextConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkMathContextConstants");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testIdentitiesOfBooleanConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkBooleanConstants");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testIdentitiesOfPrimitiveTypeConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkPrimitiveTypeConstants");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void checkTimeUnitConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkTimeUnitConstants");
        AvmTransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }

}