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

/**
 * Tests the serialization and deserialization of {@link org.aion.avm.shadow.java.math.BigInteger}
 * and {@link org.aion.avm.shadow.java.math.BigDecimal}, to verify fixes made to these classes.
 */
public class ShadowClassSerializationTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(ShadowClassSerializationTarget.class, null);
        ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void testIdentitiesOfBigIntegerConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkBigIntegerSerialization");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testIdentitiesOfBigDecimalConstants() {
        byte[] data = ABIUtil.encodeMethodArguments("checkBigDecimalSerialization");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
    }
}
