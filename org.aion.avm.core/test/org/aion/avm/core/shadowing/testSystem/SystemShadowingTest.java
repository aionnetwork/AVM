package org.aion.avm.core.shadowing.testSystem;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.util.AvmRule;
import org.aion.kernel.*;
import org.junit.*;


public class SystemShadowingTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddr;

    @Before
    public void setup() {
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, avmRule.getDappBytes(TestResource.class, null)).getDappAddress();
    }

    @Test
    public void testArrayCopy() {
        byte[] txData = ABIEncoder.encodeMethodArguments("testArrayCopy");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }
}