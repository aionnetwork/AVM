package org.aion.avm.embed.blockchainruntime;

import avm.Address;
import org.aion.types.AionAddress;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

public class SelfDestructClinitTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address from = avmRule.getPreminedAccount();
    private final BigInteger initialBalance = BigInteger.valueOf(100_000_000L);
    private long energyLimit = 5_000_000L;
    private long energyPrice = 1;
    private long refundPerContract = 24000;

    @Test
    public void destructDuringClinit() {
        byte[] args = ABIUtil.encodeDeploymentArguments(0);
        AvmRule.ResultWrapper result = deploy(args);
        byte[] code = avmRule.kernel.getCode(new AionAddress(result.getDappAddress().toByteArray()));
        Assert.assertNull(code);
        Assert.assertEquals(342176 - refundPerContract, result.getTransactionResult().energyUsed);
    }

    @Test
    public void destructOtherContractDuringClinit() {
        byte[] jar = avmRule.getDappBytes(SelfDestructTarget.class, new byte[0]);
        Address toBeDestroyed = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getDappAddress();
        avmRule.balanceTransfer(from, toBeDestroyed, initialBalance, energyLimit, energyPrice);

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] txData = encoder.encodeOneString("selfDestruct").encodeOneAddress(from).toBytes();
        byte[] args = ABIUtil.encodeDeploymentArguments(1, toBeDestroyed, txData);

        AvmRule.ResultWrapper result = deploy(args);
        byte[] code = avmRule.kernel.getCode(new AionAddress(toBeDestroyed.toByteArray()));
        Assert.assertNull(code);
        Assert.assertEquals(360692 - refundPerContract, result.getTransactionResult().energyUsed);
    }

    private AvmRule.ResultWrapper deploy(byte[] args) {
        byte[] jar = avmRule.getDappBytes(SelfDestructClinit.class, args);
        AvmRule.ResultWrapper result = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        return result;
    }
}
