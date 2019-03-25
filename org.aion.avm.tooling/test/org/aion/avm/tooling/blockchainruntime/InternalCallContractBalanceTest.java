package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link BlockchainRuntime#getBalanceOfThisContract()} method on a contract that is at
 * some specified depth in a chain of internal contract calls.
 */
public class InternalCallContractBalanceTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private static final int MAX_CALL_DEPTH = 10;
    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 50_000_000L;
    private static long energyPrice = 5;

    /**
     * Tests running a contract that spawns a chain of internal calls such that the dapp at the targeted
     * depth returns its balance and all of its parents propagate this balance up the call chain until
     * it is returned as the result of the external contract.
     *
     * This allows us to test out each depth and target each level in that depth range.
     */
    @Test
    public void testBalanceOfContractAtSpecificDepthInCallStack() {
        for (int numInternalContracts = 1; numInternalContracts < MAX_CALL_DEPTH; numInternalContracts++) {
            for (int depthOfContractToQuery = 1; depthOfContractToQuery <= numInternalContracts; depthOfContractToQuery++) {
                verifyBalanceOfContractAtSpecificDepthInCallStack(numInternalContracts, depthOfContractToQuery);
            }
        }
    }

    /**
     * Same as testBalanceOfContractAtSpecificDepthInCallStack except we add balance to the contracts
     * after they have been deployed so that we are not just testing the balance of the contract at
     * deploy time.
     */
    @Test
    public void testBalanceOfContractAtspecificDepthInCallStackAfterTransferringMoreValue() {
        for (int numInternalContracts = 1; numInternalContracts < MAX_CALL_DEPTH; numInternalContracts++) {
            for (int depthOfContractToQuery = 1; depthOfContractToQuery <= numInternalContracts; depthOfContractToQuery++) {
                verifyBalanceOfContractAtSpecificDepthInCallStackAfterTransferringMoreValue(numInternalContracts, depthOfContractToQuery);
            }
        }
    }

    /**
     * Each contract saves its balance that it queries at clinit time and provides a method that
     * returns this amount. We simply verify the amount is correct.
     */
    @Test
    public void testBalanceOfContractWhenQueriedDuringClinit() {
        // Deploy the contract with enough balance to transfer to its child.
        BigInteger initialBalance = BigInteger.valueOf(10000000);
        Address contract = deployContract(initialBalance);

        // Call the contract that will create & transfer this value to the new contract at deploy time.
        long balanceToTransferToChild = 123456;
        Address childContract = callContractDoCreateAndTransferValueAtDeployTime(contract, balanceToTransferToChild);

        // Check that the clinit balance is as expected.
        BigInteger clinitBalance = getClinitBalanceOfContract(childContract);
        assertEquals(balanceToTransferToChild, clinitBalance.longValueExact());
    }

    private void verifyBalanceOfContractAtSpecificDepthInCallStack(int numInternalContracts, int depthOfContractToQuery) {
        System.out.println("verifyBalanceOfContractAtSpecificDepthInCallStack on " + numInternalContracts + " internal calls and targeting depth " + depthOfContractToQuery);

        // Produce some arbitrary balances that the contracts will have.
        BigInteger topContractBalance = BigInteger.ZERO;
        BigInteger[] contractBalances = produceRandomBalances(numInternalContracts);

        // Deploy the contracts and verify each one has the correct balance.
        Address topContract = deployContract(topContractBalance);
        Address[] internalContracts = deployContracts(contractBalances);
        verifyEachContractHasSpecifiedBalance(internalContracts, contractBalances);

        // Make the call into the top contract and get the result.
        BigInteger balanceOfTargetContract = callContractToGetBalanceAtDepth(topContract, internalContracts, depthOfContractToQuery);

        // depth - 1 because topContract is at depth 0 so internal depths begin at 1.
        assertEquals(contractBalances[depthOfContractToQuery - 1], balanceOfTargetContract);
    }

    private void verifyBalanceOfContractAtSpecificDepthInCallStackAfterTransferringMoreValue(int numInternalContracts, int depthOfContractToQuery) {
        System.out.println("verifyBalanceOfContractAtSpecificDepthInCallStackAfterTransferringMoreValue on " + numInternalContracts
            + " internal calls and targeting depth " + depthOfContractToQuery);

        // Produce some arbitrary balances that the contracts will have.
        BigInteger topContractBalance = BigInteger.ZERO;
        BigInteger[] contractBalances = produceRandomBalances(numInternalContracts);

        // Deploy the contracts and verify each one has the correct balance.
        Address topContract = deployContract(topContractBalance);
        Address[] internalContracts = deployContracts(contractBalances);

        // give some addition balance to the contracts.
        BigInteger[] additionalBalances = produceRandomBalances(numInternalContracts);
        giveValueToContracts(internalContracts, additionalBalances);

        // get the new balance amounts and verify each contract has the correct balance.
        BigInteger[] newContractBalances = bulkAdd(contractBalances, additionalBalances);
        verifyEachContractHasSpecifiedBalance(internalContracts, newContractBalances);

        // Make the call into the top contract and get the result.
        BigInteger balanceOfTargetContract = callContractToGetBalanceAtDepth(topContract, internalContracts, depthOfContractToQuery);

        // depth - 1 because topContract is at depth 0 so internal depths begin at 1.
        assertEquals(newContractBalances[depthOfContractToQuery - 1], balanceOfTargetContract);
    }

    /**
     * Deploys initialBalances.length new contracts and transfers initialBalances[i] value into the
     * i'th contract.
     *
     * Returns the addresses of the newly deployed contracts.
     */
    private Address[] deployContracts(BigInteger[] initialBalances) {
        Address[] contracts = new Address[initialBalances.length];
        for (int i = 0; i < initialBalances.length; i++) {
            contracts[i] = deployContract(initialBalances[i]);
        }
        return contracts;
    }

    private Address deployContract(BigInteger value) {
        AvmRule.ResultWrapper result = avmRule.deploy(from, value, getDappBytes(), energyLimit, energyPrice);
        assertTrue(result.getTransactionResult().getResultCode().isSuccess());
        return result.getDappAddress();
    }

    private BigInteger callContractToGetBalanceAtDepth(Address contract, Address[] otherContracts, int depthToQuery) {

        byte[] callData = ABIEncoder.encodeMethodArguments("getBalanceOfDappViaInternalCall", otherContracts, depthToQuery);
        AvmRule.ResultWrapper result = avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice);
        assertTrue(result.getTransactionResult().getResultCode().isSuccess());
        return new BigInteger((byte[]) result.getDecodedReturnData());
    }

    private Address callContractDoCreateAndTransferValueAtDeployTime(Address contract, long amountToTransfer) {
        byte[] dappBytes = getDappBytes();

        byte[] dappBytesFirstHalf = new byte[dappBytes.length / 2];
        byte[] dappBytesSecondHalf = new byte[dappBytes.length - dappBytesFirstHalf.length];
        System.arraycopy(dappBytes, 0, dappBytesFirstHalf, 0, dappBytesFirstHalf.length);
        System.arraycopy(dappBytes, dappBytesFirstHalf.length, dappBytesSecondHalf, 0, dappBytesSecondHalf.length);

        byte[] callData = ABIEncoder.encodeMethodArguments("createNewContractWithValue", dappBytesFirstHalf, dappBytesSecondHalf, amountToTransfer);

        AvmRule.ResultWrapper result = avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice);
        assertTrue(result.getTransactionResult().getResultCode().isSuccess());
        return new Address((byte[]) result.getDecodedReturnData());
    }

    private BigInteger getClinitBalanceOfContract(Address contract) {
        byte[] callData = ABIEncoder.encodeMethodArguments("getBalanceOfThisContractDuringClinit");
        AvmRule.ResultWrapper result = avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice);
        assertTrue(result.getTransactionResult().getResultCode().isSuccess());
        return new BigInteger((byte[]) result.getDecodedReturnData());
    }

    private void giveValueToContracts(Address[] contracts, BigInteger[] value) {
        for (int i = 0; i < contracts.length; i++) {
            avmRule.kernel.adjustBalance(org.aion.types.Address.wrap(contracts[i].unwrap()), value[i]);
        }
    }

    private BigInteger[] produceRandomBalances(int numBalances) {
        BigInteger[] balances = new BigInteger[numBalances];
        for (int i = 0; i < numBalances; i++) {
            // Not using signum constructor incase byte array is all zeroes, will throw an error.
            BigInteger balance = new BigInteger(Helpers.randomBytes(4));
            balances[i] = (balance.compareTo(BigInteger.ZERO) < 0) ? balance.negate() : balance;
        }
        return balances;
    }

    private static void verifyEachContractHasSpecifiedBalance(Address[] contracts, BigInteger[] balances) {
        for (int i = 0; i < contracts.length; i++) {
            assertEquals(avmRule.kernel.getBalance(org.aion.types.Address.wrap(contracts[i].unwrap())), balances[i]);
        }
    }

    private BigInteger[] bulkAdd(BigInteger[] values1, BigInteger[] values2) {
        BigInteger[] results = new BigInteger[values1.length];
        for (int i = 0; i < values1.length; i++) {
            results[i] = values1[i].add(values2[i]);
        }
        return results;
    }

    private static byte[] getDappBytes() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(InternalCallContractBalanceTarget.class);
        return new CodeAndArguments(jar, new byte[0]).encodeToBytes();
    }

}
