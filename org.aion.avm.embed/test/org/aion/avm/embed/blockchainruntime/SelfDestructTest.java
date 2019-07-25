package org.aion.avm.embed.blockchainruntime;

import avm.Address;
import org.aion.types.AionAddress;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

public class SelfDestructTest {

    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address from = avmRule.getPreminedAccount();
    private Address beneficiary = new Address(Helpers.hexStringToBytes("a055f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f72000000005"));
    private final BigInteger initialBalance = BigInteger.valueOf(100_000_000L);
    private long energyLimit = 5_000_000L;
    private long energyPrice = 1;
    private long refundPerContract = 24000;
    private Address dappAddr;

    @Before
    public void setup() {
        dappAddr = deploy();
    }

    @Test
    public void selfDestruct() {
        long energyUsed = call("selfDestruct", beneficiary);
        Assert.assertEquals(37649 - 37649/2, energyUsed);
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new AionAddress(dappAddr.toByteArray())));
        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new AionAddress(beneficiary.toByteArray())));
    }

    @Test
    public void selfDestructMulti() {
        long energyUsed = call("selfDestructMulti", beneficiary);
        Assert.assertEquals(59171 - refundPerContract, energyUsed);
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new AionAddress(dappAddr.toByteArray())));
        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new AionAddress(beneficiary.toByteArray())));
    }

    @Test
    public void reentrantSelfDestruct() {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] txData = encoder.encodeOneString("selfDestruct").encodeOneAddress(beneficiary).toBytes();
        long energyUsed = call("reentrantSelfDestruct", txData);

        Assert.assertEquals(55446 - refundPerContract, energyUsed);
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new AionAddress(dappAddr.toByteArray())));
        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new AionAddress(beneficiary.toByteArray())));
    }

    @Test
    public void killOtherContracts() {
        Address[] contracts = new Address[7];
        for(int i =0 ; i < contracts.length; i++){
            contracts[i] = deploy();
        }
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] txData = encoder.encodeOneString("selfDestruct").encodeOneAddress(beneficiary).toBytes();

        long energyUsed = call("killOtherContracts", contracts, txData);
        // capped off at half of the total energy used
        Assert.assertEquals(207462 - (207462 / 2), energyUsed);

        Assert.assertEquals(initialBalance.multiply(BigInteger.valueOf(contracts.length)).add(BigInteger.valueOf(contracts.length)),
                avmRule.kernel.getBalance(new AionAddress(beneficiary.toByteArray())));
    }

    @Test
    public void selfDestructAndTransferToSelf() {
        long energyUsed = call("selfDestruct", dappAddr);
        Assert.assertEquals(37829 - 37829/2, energyUsed);
        //burns the balance
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new AionAddress(dappAddr.toByteArray())));
    }

    @Test
    public void selfDestructDifferentAddress() {
        Address[] addresses = {
                new Address(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f72000000000")),
                new Address(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f72000000001")),
                new Address(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f72000000002"))};
        long energyUsed = call("selfDestructDifferentAddress", (Object) addresses);

        Assert.assertEquals(63801 - refundPerContract, energyUsed);

        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new AionAddress(dappAddr.toByteArray())));

        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new AionAddress(addresses[0].toByteArray())));
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new AionAddress(addresses[1].toByteArray())));
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new AionAddress(addresses[2].toByteArray())));
    }

    private long call(String methodName, Object... objects) {
        byte[] txData = ABIUtil.encodeMethodArguments(methodName, objects);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        return result.getTransactionResult().energyUsed;
    }

    public Address deploy() {
        byte[] jar = avmRule.getDappBytes(SelfDestructTarget.class, new byte[0]);
        Address contract = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getDappAddress();
        avmRule.balanceTransfer(from, contract, initialBalance, energyLimit, energyPrice);
        return contract;
    }
}
