package org.aion.avm.core;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.AvmRule.ResultWrapper;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the serialization and deserialization of {@link org.aion.avm.shadow.java.math.BigInteger}
 * and {@link org.aion.avm.shadow.java.math.BigDecimal}, to verify fixes made to these classes.
 */
public class ShadowClassSerializationTest {
    private static final Address sender = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;

    @Rule
    public AvmRule avmRule = new AvmRule(false);

    @Before
    public void setup() {
        byte[] data = avmRule.getDappBytes(ShadowClassSerializationTarget.class, null);
        ResultWrapper deployResult = avmRule.deploy(sender, value, data, 5_000_000, 1);
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = avmRule.deploy(sender, value, data, 5_000_000, 1).getDappAddress();
    }

    @Test
    public void testIdentitiesOfBigIntegerConstants() {
        byte[] data = ABIEncoder.encodeMethodArguments("checkBigIntegerSerialization");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testIdentitiesOfBigDecimalConstants() {
        byte[] data = ABIEncoder.encodeMethodArguments("checkBigDecimalSerialization");
        TransactionResult result = avmRule.call(sender, contract, value, data, 2_000_000, 1).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
    }
}
