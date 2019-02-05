package org.aion.avm.core.concurrent;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.util.AvmRule;
import org.aion.vm.api.interfaces.Address;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

public class TimeUnitTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);
    private Address dappAddr;
    private Address preminedAccount;

    public TimeUnitTest() {
        this.preminedAccount = avmRule.getPreminedAccount();
    }

    @Before
    public void deployDapp() {
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
        byte[] txData = ABIEncoder.encodeMethodArguments(methodName);
        return avmRule.call(this.preminedAccount, this.dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
    }
}