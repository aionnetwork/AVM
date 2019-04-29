package org.aion.avm.tooling.blockchainruntime;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.tooling.AvmRule;
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
        byte[] code = avmRule.kernel.getCode(org.aion.types.Address.wrap(result.getDappAddress().unwrap()));
        Assert.assertNull(code);
        Assert.assertEquals(532425 - refundPerContract, energyLimit - result.getTransactionResult().getEnergyRemaining());
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
        byte[] code = avmRule.kernel.getCode(org.aion.types.Address.wrap(toBeDestroyed.unwrap()));
        Assert.assertNull(code);
        Assert.assertEquals(573202 - refundPerContract, energyLimit - result.getTransactionResult().getEnergyRemaining());
    }

    private AvmRule.ResultWrapper deploy(byte[] args) {
        byte[] jar = avmRule.getDappBytes(SelfDestructClinit.class, args);
        AvmRule.ResultWrapper result = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        return result;
    }
}
