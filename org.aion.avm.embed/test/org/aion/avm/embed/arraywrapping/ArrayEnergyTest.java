package org.aion.avm.embed.arraywrapping;

import a.ArrayElement;
import avm.Address;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

/**
 * This test ensures charge energy methods in {@link a.Array} return the same result as before this commit.
 */
public class ArrayEnergyTest {

    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private Address from = avmRule.getPreminedAccount();
    private Address dappAddr;

    private long energyLimit = 2_000_000;
    private long energyPrice = 1;

    @Before
    public void setup() {
        byte[] txData = avmRule.getDappBytes(ArrayEnergyTarget.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testInitArray1DimEnergyUsage() {
        byte[] txData = ABIUtil.encodeMethodArguments("initArray1Dim");
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        int length = 100;
        long energy = 26294
                // init array cost
                + length * (ArrayElement.BYTE.getEnergy()
                + ArrayElement.CHAR.getEnergy()
                + ArrayElement.DOUBLE.getEnergy()
                + ArrayElement.FLOAT.getEnergy()
                + ArrayElement.INT.getEnergy()
                + ArrayElement.LONG.getEnergy()
                + ArrayElement.SHORT.getEnergy()
                + ArrayElement.REF.getEnergy()
                + ArrayElement.REF.getEnergy()
                + ArrayElement.REF.getEnergy())
                // array base constructor cost
                + (10 * 100);
        Assert.assertEquals(energy, result.getTransactionResult().energyUsed);
    }

    @Test
    public void testInitArrayMultiDimEnergyUsage() {
        byte[] txData = ABIUtil.encodeMethodArguments("initArrayMultiDim");
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals(152911, result.getTransactionResult().energyUsed);
    }

    @Test
    public void testCloneMultiDimEnergyUsage() {
        byte[] txData = ABIUtil.encodeMethodArguments("cloneMultiDim");
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals(54657, result.getTransactionResult().energyUsed);
    }
}
