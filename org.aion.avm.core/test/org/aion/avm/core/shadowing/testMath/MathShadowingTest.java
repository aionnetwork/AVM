package org.aion.avm.core.shadowing.testMath;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.util.AvmRule;
import org.aion.kernel.KernelInterfaceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


public class MathShadowingTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);
    
    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddr;

    private long energyLimit = 600_000_00000L;
    private long energyPrice = 1;


    @Before
    public void setup() {
        byte[] txData = avmRule.getDappBytes (TestResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testMaxMin() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMaxMin");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    /**
     * Creates a basic MathContext, just to prove that it is correctly formed.
     */
    @Test
    public void createSimpleContext() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testMathContext");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(5, result);
    }
}
