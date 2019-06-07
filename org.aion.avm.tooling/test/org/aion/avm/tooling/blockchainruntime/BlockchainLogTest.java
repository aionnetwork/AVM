package org.aion.avm.tooling.blockchainruntime;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

public class BlockchainLogTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Address dappAddr;
    private static final long getRemainingEnergyOperationCost = 100;

    @BeforeClass
    public static void setUp() {
        byte[] jar = avmRule.getDappBytes(BlockchainLogTarget.class, new byte[0]);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testLog0Topic() {
        Assert.assertEquals(775, (long) call("testLog0Topic", 50) - getRemainingEnergyOperationCost);
        Assert.assertEquals(3200375, (long) call("testLog0Topic", 400000) - getRemainingEnergyOperationCost);
    }

    @Test
    public void testLog1Topic() {
        /*
            The maximum topic size is 32, because only the first 32 bytes of a topic parameter would be
            stored by kernel, no matter how big the topic parameter was passed to log method.
         */
        Assert.assertEquals(1150, (long) call("testLog1Topic", 32, 50) - getRemainingEnergyOperationCost);
        Assert.assertEquals(3200750, (long) call("testLog1Topic", 32, 400000) - getRemainingEnergyOperationCost);

    }

    @Test
    public void testLog2Topics() {
        Assert.assertEquals(1525, (long) call("testLog2Topics", 32, 50) - getRemainingEnergyOperationCost);
        Assert.assertEquals(3201125, (long) call("testLog2Topics", 32, 400000) - getRemainingEnergyOperationCost);

    }

    @Test
    public void testLog3Topics() {
        Assert.assertEquals(1900, (long) call("testLog3Topics", 32, 50) - getRemainingEnergyOperationCost);
        Assert.assertEquals(3201500, (long) call("testLog3Topics", 32, 400000) - getRemainingEnergyOperationCost);

    }

    @Test
    public void testLog4Topics() {
        Assert.assertEquals(2275, (long) call("testLog4Topics", 32, 50) - getRemainingEnergyOperationCost);
        Assert.assertEquals(3201875, (long) call("testLog4Topics", 32, 400000) - getRemainingEnergyOperationCost);
    }


    @Test
    public void testLog4TopicsNull() {
        Assert.assertTrue((boolean)call("testLog4TopicsNull"));
    }

    private Object call(String methodName, Object ...objects) {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments(methodName, objects);
        return avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice).getDecodedReturnData();
    }
}