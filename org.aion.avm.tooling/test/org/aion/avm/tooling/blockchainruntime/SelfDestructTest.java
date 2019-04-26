package org.aion.avm.tooling.blockchainruntime;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.AvmRule;
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
        Assert.assertEquals(57830 - refundPerContract, energyUsed);
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new org.aion.types.Address(dappAddr.unwrap())));
        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new org.aion.types.Address(beneficiary.unwrap())));
    }

    @Test
    public void selfDestructMulti() {
        long energyUsed = call("selfDestructMulti", beneficiary);
        Assert.assertEquals(79357 - refundPerContract, energyUsed);
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new org.aion.types.Address(dappAddr.unwrap())));
        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new org.aion.types.Address(beneficiary.unwrap())));
    }

    @Test
    public void reentrantSelfDestruct() {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] txData = encoder.encodeOneString("selfDestruct").encodeOneAddress(beneficiary).toBytes();
        long energyUsed = call("reentrantSelfDestruct", txData);
        Assert.assertEquals(90923 - refundPerContract, energyUsed);
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new org.aion.types.Address(dappAddr.unwrap())));
        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new org.aion.types.Address(beneficiary.unwrap())));
    }

    @Test
    public void killOtherContracts() {
        Address[] contracts = new Address[8];
        for(int i =0 ; i < contracts.length; i++){
            contracts[i] = deploy();
        }
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] txData = encoder.encodeOneString("selfDestruct").encodeOneAddress(beneficiary).toBytes();

        long energyUsed = call("killOtherContracts", contracts, txData);
        // capped off at half of the total energy used
        Assert.assertEquals(374327 - (374327 / 2), energyUsed);
        Assert.assertEquals(initialBalance.multiply(BigInteger.valueOf(contracts.length)).add(BigInteger.valueOf(contracts.length)),
                avmRule.kernel.getBalance(new org.aion.types.Address(beneficiary.unwrap())));
    }

    @Test
    public void selfDestructAndTransferToSelf() {
        long energyUsed = call("selfDestruct", dappAddr);
        Assert.assertEquals(58010 - refundPerContract, energyUsed);
        //burns the balance
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new org.aion.types.Address(dappAddr.unwrap())));
    }

    @Test
    public void selfDestructDifferentAddress() {
        Address[] addresses = {
                new Address(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f72000000000")),
                new Address(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f72000000001")),
                new Address(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f72000000002"))};
        long energyUsed = call("selfDestructDifferentAddress", (Object) addresses);

        Assert.assertEquals(84002 - refundPerContract, energyUsed);

        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new org.aion.types.Address(dappAddr.unwrap())));

        Assert.assertEquals(initialBalance, avmRule.kernel.getBalance(new org.aion.types.Address(addresses[0].unwrap())));
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new org.aion.types.Address(addresses[1].unwrap())));
        Assert.assertEquals(BigInteger.ZERO, avmRule.kernel.getBalance(new org.aion.types.Address(addresses[2].unwrap())));
    }

    private long call(String methodName, Object... objects) {
        byte[] txData = ABIUtil.encodeMethodArguments(methodName, objects);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getReceiptStatus().isSuccess());
        return energyLimit - result.getTransactionResult().getEnergyRemaining();
    }

    public Address deploy() {
        byte[] jar = avmRule.getDappBytes(SelfDestructTarget.class, new byte[0]);
        Address contract = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice).getDappAddress();
        avmRule.balanceTransfer(from, contract, initialBalance, energyLimit, energyPrice);
        return contract;
    }
}
