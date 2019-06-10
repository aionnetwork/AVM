package org.aion.avm.tooling.arraywrapping;

import avm.Address;
import org.aion.avm.tooling.AvmRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class InvalidArrayDimensionTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    Address sender = avmRule.getPreminedAccount();
    BigInteger value = BigInteger.ZERO;

    @Test
    public void testPrimitive(){

        byte[] data = avmRule.getDappBytes(InvalidDimensionPrimitiveTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().getResultCode().isFailed());
    }

    @Test
    public void testObject(){
        byte[] data = avmRule.getDappBytes(InvalidDimensionObjectTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().getResultCode().isFailed());
    }
}
