package org.aion.avm.embed.concurrent;

import avm.Address;

import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.junit.*;

import java.math.BigInteger;

public class TimeUnitTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);
    private static Address dappAddr;
    private static Address preminedAccount = avmRule.getPreminedAccount();

    @BeforeClass
    public static void deployDapp() {
        byte[] dapp = avmRule.getDappBytes(TimeUnitTestTarget.class, new byte[0]);
        dappAddr = avmRule.deploy(preminedAccount, BigInteger.ZERO, dapp).getDappAddress();
    }

    @Test
    public void testavm_convert() { Assert.assertEquals(true, call("testavm_convert")); }

    @Test
    public void testavm_toX() { Assert.assertEquals(true, call("testavm_toX")); }

    @Test
    public void testavm_values() { Assert.assertEquals(true, call("testavm_values")); }

    @Test
    public void testavm_valueOf() { Assert.assertEquals(true, call("testavm_valueOf")); }

    public Object call(String methodName) {
        long energyLimit = 600_000_00000L;
        long energyPrice = 1L;
        byte[] txData = ABIUtil.encodeMethodArguments(methodName);
        return avmRule.call(preminedAccount, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
    }
}
