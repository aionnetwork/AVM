package org.aion.avm.embed.blockchainruntime;

import avm.Address;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.types.TransactionResult;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class ResultTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 2_000_000L;
    private static long energyPrice = 5;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        deployContract();
    }

    @Test
    public void testHashCode() {
        byte[] callData = ABIUtil.encodeMethodArguments("testHashCode");
        TransactionResult result = avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testEquality() {
        byte[] data = ABIUtil.encodeMethodArguments("returnInt");
        byte[] callData = ABIUtil.encodeMethodArguments("testEquality", data);
        TransactionResult result = avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    private static void deployContract() {
        byte[] jar = avmRule.getDappBytes(ResultTarget.class, new byte[0]);
        TransactionResult result = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
        contract = new Address(result.copyOfTransactionOutput().orElseThrow());
    }
}
