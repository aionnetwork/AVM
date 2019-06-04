package org.aion.rules;

import org.aion.types.AionAddress;
import avm.Address;
import org.aion.avm.core.util.LogSizeUtils;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.userlib.AionMap;
import org.aion.vm.api.interfaces.ResultCode;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.*;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class JUnitRuleTest {

    // ClassRule annotation instantiates them only once for the whole test class.
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address dappAddr;
    private static Address preminedAccount = avmRule.getPreminedAccount();

    @BeforeClass
    public static void deployDapp() {
        byte[] arguments = ABIUtil.encodeDeploymentArguments(8);
        byte[] dapp = avmRule.getDappBytes(JUnitRuleTestTarget.class, arguments, AionMap.class);
        dappAddr = avmRule.deploy(preminedAccount, BigInteger.ZERO, dapp).getDappAddress();
    }

    @Test
    public void testIncreaseNumber() {
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        byte[] txData = ABIUtil.encodeMethodArguments("increaseNumber", 10);
        Object result = avmRule.call(preminedAccount, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testSumInput() {
        Address sender = avmRule.getRandomAddress(BigInteger.valueOf(10_000_000L));
        byte[] txData = ABIUtil.encodeMethodArguments("sum", 15, 10);
        Object result = avmRule.call(sender, dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(15 + 10, result);
    }

    @Test
    public void testMapPut() {
        byte[] txData = ABIUtil.encodeMethodArguments("mapPut", "1", 42);
        ResultCode result = avmRule.call(preminedAccount, dappAddr, BigInteger.ZERO, txData).getReceiptStatus();
        Assert.assertTrue(result.isFailed());
    }

    @Test
    public void testMapGet() {
        byte[] txData = ABIUtil.encodeMethodArguments("mapPut", 1, "val1");
        ResultCode status = avmRule.call(preminedAccount, dappAddr, BigInteger.ZERO, txData).getReceiptStatus();
        Assert.assertTrue(status.isSuccess());

        txData = ABIUtil.encodeMethodArguments("mapGet", 1);
        AvmRule.ResultWrapper result = avmRule.call(preminedAccount, dappAddr, BigInteger.ZERO, txData);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals("val1", result.getDecodedReturnData());
    }

    @Test
    public void testLogEvent() {
        byte[] txData = ABIUtil.encodeMethodArguments("logEvent");
        AvmRule.ResultWrapper result = avmRule.call(preminedAccount, dappAddr, BigInteger.ZERO, txData);

        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        Assert.assertEquals(2, result.getLogs().size());
        assertEquals(dappAddr, new Address(result.getLogs().get(0).getSourceAddress().toByteArray()));
        assertArrayEquals(new byte[]{ 0x1 }, result.getLogs().get(0).getData());
        assertArrayEquals(LogSizeUtils.truncatePadTopic(new byte[]{ 0xf, 0xe, 0xd, 0xc, 0xb, 0xa }), result.getLogs().get(1).getTopics().get(0));
    }

    @Test
    public void balanceTransfer(){
        // balance transfer to account
        Address to = avmRule.getRandomAddress(BigInteger.ZERO);
        TransactionResult result = avmRule.balanceTransfer(preminedAccount, to, BigInteger.valueOf(100L), 21000, 1L).getTransactionResult();

        assertTrue(result.getResultCode().isSuccess());
        assertEquals(BigInteger.valueOf(100L), avmRule.kernel.getBalance(new AionAddress(to.toByteArray())));

        // balance transfer to contract
        result = avmRule.balanceTransfer(preminedAccount, dappAddr, BigInteger.valueOf(100L), 51000, 1L).getTransactionResult();

        assertTrue(result.getResultCode().isSuccess());
        assertEquals(BigInteger.valueOf(100L), avmRule.kernel.getBalance(new AionAddress(dappAddr.toByteArray())));

    }
}
