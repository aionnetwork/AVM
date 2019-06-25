package org.aion.avm.embed.bootstrapmethods;

import avm.Address;

import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.types.TransactionResult;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests that a contract that uses string concatenation and lambdas charges the same amount when
 * invoked multiple times.
 */
public class BootstrappingEnergyChargeConsistencyTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private long energyLimit = 6_000_000;
    private Address deployer = avmRule.getPreminedAccount();



    @Test
    public void testConsistencyOverMultipleInvocations() {
        // Deploy the contract.
        Address contractAddress = deployContract();

        // Run the contract multiple times.
        TransactionResult result1 = runContract(deployer, contractAddress);
        TransactionResult result2 = runContract(deployer, contractAddress);
        TransactionResult result3 = runContract(deployer, contractAddress);

        // Ensure the calls were all successful.
        assertTrue(result1.transactionStatus.isSuccess());
        assertTrue(result2.transactionStatus.isSuccess());
        assertTrue(result3.transactionStatus.isSuccess());

        // Compare the energy charges.
        long energy1 = result1.energyUsed;
        long energy2 = result2.energyUsed;
        long energy3 = result3.energyUsed;

        assertEquals(energy1, energy2);
        assertEquals(energy2, energy3);
    }

    private Address deployContract() {
        return avmRule.deploy(deployer, BigInteger.ZERO, avmRule.getDappBytes(EnergyChargeConsistencyTarget.class, null)).getDappAddress();
    }

    private TransactionResult runContract(Address sender, Address contract) {
        byte[] callData = ABIUtil.encodeMethodArguments("run");
        return avmRule.call(sender, contract, BigInteger.ZERO, callData, energyLimit, 1L).getTransactionResult();
    }

}
