package org.aion.avm.core.blockchainruntime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.EmptyInstrumentation;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link BlockchainRuntime#getBalanceOfThisContract()} method on a contract that is at
 * some specified depth in a chain of internal contract calls.
 */
public class InternalCallContractBalanceTest {
    private static final int MAX_CALL_DEPTH = 10;
    private static Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 5;
    private static Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private static KernelInterface kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        kernel = new KernelInterfaceImpl();
        avm = CommonAvmFactory.buildAvmInstance(kernel);
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

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
        Transaction transaction = Transaction.create(from, kernel.getNonce(from), value, getDappBytes(), energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return AvmAddress.wrap(result.getReturnData());
    }

    private BigInteger callContractToGetBalanceAtDepth(Address contract, Address[] otherContracts, int depthToQuery) {
        org.aion.avm.api.Address[] otherContractsAsAbiAddresses = toAbiAddress(otherContracts);

        byte[] callData = ABIEncoder.encodeMethodArguments("getBalanceOfDappViaInternalCall", otherContractsAsAbiAddresses, depthToQuery);
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return new BigInteger((byte[]) ABIDecoder.decodeOneObject(result.getReturnData()));
    }

    private Address callContractDoCreateAndTransferValueAtDeployTime(Address contract, long amountToTransfer) {
        byte[] callData = ABIEncoder.encodeMethodArguments("createNewContractWithValue", getDappBytes(), amountToTransfer);
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return AvmAddress.wrap((byte[]) ABIDecoder.decodeOneObject(result.getReturnData()));
    }

    private BigInteger getClinitBalanceOfContract(Address contract) {
        byte[] callData = ABIEncoder.encodeMethodArguments("getBalanceOfThisContractDuringClinit");
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return new BigInteger((byte[]) ABIDecoder.decodeOneObject(result.getReturnData()));
    }

    private void giveValueToContracts(Address[] contracts, BigInteger[] value) {
        for (int i = 0; i < contracts.length; i++) {
            kernel.adjustBalance(contracts[i], value[i]);
        }
    }

    private org.aion.avm.api.Address[] toAbiAddress(Address[] addresses) {
        IInstrumentation instrumentation = new EmptyInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        org.aion.avm.api.Address[] convertedAddresses = new org.aion.avm.api.Address[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            convertedAddresses[i] = new org.aion.avm.api.Address(addresses[i].toBytes());
        }
        InstrumentationHelpers.detachThread(instrumentation);
        return convertedAddresses;
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
            assertEquals(kernel.getBalance(contracts[i]), balances[i]);
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
        byte[] jar = JarBuilder.buildJarForMainAndClasses(InternalCallContractBalanceTarget.class);
        return new CodeAndArguments(jar, new byte[0]).encodeToBytes();
    }

}
