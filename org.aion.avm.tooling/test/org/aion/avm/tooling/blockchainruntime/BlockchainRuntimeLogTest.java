package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.api.Address;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.*;

import java.math.BigInteger;

public class BlockchainRuntimeLogTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;
    private static Address dappAddr;

    @BeforeClass
    public static void setUp() {
        byte[] jar = avmRule.getDappBytes(BlockchainRuntimeLogTarget.class, new byte[0]);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testLog0Topic() {
        Assert.assertEquals((long)600, call("testLog0Topic", 50));
        Assert.assertEquals((long)3200200, call("testLog0Topic", 400000));
    }

    @Test
    public void testLog1Topic() {
        /*
            The maximum topic size is 32, because only the first 32 bytes of a topic parameter would be
            stored by kernel, no matter how big the topic parameter was passed to log method.
         */
        Assert.assertEquals((long)700, call("testLog1Topic", 32, 50));
        Assert.assertEquals((long)3200300, call("testLog1Topic", 32, 400000));

    }

    @Test
    public void testLog2Topics() {
        Assert.assertEquals((long)800, call("testLog2Topics", 32, 50));
        Assert.assertEquals((long)3200400, call("testLog2Topics", 32, 400000));

    }

    @Test
    public void testLog3Topics() {
        Assert.assertEquals((long)900, call("testLog3Topics", 32, 50));
        Assert.assertEquals((long)3200500, call("testLog3Topics", 32, 400000));

    }

    @Test
    public void testLog4Topics() {
        Assert.assertEquals((long)1000, call("testLog4Topics", 32, 50));
        Assert.assertEquals((long)3200600, call("testLog4Topics", 32, 400000));
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