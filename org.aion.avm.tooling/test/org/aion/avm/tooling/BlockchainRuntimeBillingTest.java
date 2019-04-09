package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class BlockchainRuntimeBillingTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;


    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(BlockchainRuntimeBillingTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().getResultCode().isSuccess());
        contract = deployResult.getDappAddress();
    }

    @Test
    public void fillArray() {
        AvmRule.ResultWrapper result = callStatic("fillArray");
        System.out.println(2_000_000- result.getTransactionResult().getEnergyRemaining());
    }

    private AvmRule.ResultWrapper callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        return avmRule.call(sender, contract, value, data, 2_000_000, 1);
    }
}
