package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.tooling.AvmRule;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link org.aion.avm.api.BlockchainRuntime} functionality for getting the origin, caller
 * and current contract addresses out on chains of dApp calls.
 *
 * The final dApp in the chain will REVERT and produce no data, so the data we validate has one entry
 * less than the total number of calls.
 *
 * It's meaningless to test failures at other depths because a failed dApp cannot communicate its
 * report (its return data is null), so you lose all of the information of the dApps below it.
 *
 * This test is really just meant to test that the second last dApp grabs the proper data after its
 * child fails, in order to convince us that the {@link org.aion.avm.api.BlockchainRuntime} class is
 * being handled correctly even when dApps fail.
 */
public class FailedInternalCallAddressesTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private static final int MAX_CALL_DEPTH = 10;
    private static Address from = avmRule.getPreminedAccount();
    private static long energyLimit = 50_000_000L;
    private static long energyPrice = 5;


    // All of the print calls become active when this is set true. By default this is false to speed
    // up the running of these tests / not pollute stdout, but a handy option for debugging.
    private static final boolean ACTIVATE_OUTPUTS_FOR_DEBUGGING = false;

    /**
     * Tests that each dApp reports the correct addresses for: origin, caller, contract
     *
     * This tests the case in which N separate deploys are deployed and there is a chain of calls
     * from one into the next through all N and each of the 3 address queries are done inside eac
     * dApp and this 'report' is returned back, parsed and verified as being correct.
     *
     * This tests the following order of events:
     *    - call other dapp first, grab its report (via recursion)
     *    - produce dapp report for self
     *
     * (ie. recursion first, self report last)
     *
     *
     * NOTE: The dApp that is called last will REVERT, but none of the other dApps will revert. Thus
     * the report is 1 address less than the total, because the last dApp generates no report.
     */
    @Test
    public void testInternalCallsIntoOtherDappsDoingRecursionFirst() {
        for (int i = 1; i < MAX_CALL_DEPTH; i++) {
            verifyRunningInternalCallsWithFailureFromDappIntoOtherDapp(i, true);
        }
    }

    /**
     * Tests that each dApp reports the correct addresses for: origin, caller, contract
     *
     * This tests the case in which N separate deploys are deployed and there is a chain of calls
     * from one into the next through all N and each of the 3 address queries are done inside eac
     * dApp and this 'report' is returned back, parsed and verified as being correct.
     *
     * This tests the following order of events:
     *    - produce dapp report for self
     *    - call other dapp, grab its report (via recursion)
     *
     * (ie. self report first, recursion last)
     *
     *
     * NOTE: The dApp that is called last will REVERT, but none of the other dApps will revert. Thus
     * the report is 1 address less than the total, because the last dApp generates no report.
     */
    @Test
    public void testInternalCallsIntoOtherDappsDoingRecursionLast() {
        for (int i = 1; i < MAX_CALL_DEPTH; i++) {
            verifyRunningInternalCallsWithFailureFromDappIntoOtherDapp(i, false);
        }
    }

    /**
     * Tests that the dApp reports the correct addresses for: origin, caller, contract
     *
     * This tests the case in which a single dApp is deployed and there is a chain of calls from
     * itself into itself recursively N times and each of the 3 address queries are done inside each
     * call and this 'report' is returned back, parsed and verified as being correct.
     *
     * This tests the following order of events:
     *     - call into self first, grab its report (via recursion)
     *     - produce dapp report for self at the current call level
     *
     * (ie. recursion first, self report last)
     *
     * NOTE: The last dApp call will REVERT, but none of the other dApps calls will revert. Thus
     * the report is 1 address less than the total, because the last dApp call generates no report.
     */
    @Test
    public void testInternalCallsIntoSelfDoingRecursionFirst() {
        for (int i = 1; i < MAX_CALL_DEPTH; i++) {
            verifyRunningInternalCallsWithFailureFromDappIntoSelf(i, true);
        }
    }

    /**
     * Tests that the dApp reports the correct addresses for: origin, caller, contract
     *
     * This tests the case in which a single dApp is deployed and there is a chain of calls from
     * itself into itself recursively N times and each of the 3 address queries are done inside each
     * call and this 'report' is returned back, parsed and verified as being correct.
     *
     * This tests the following order of events:
     *     - produce dapp report for self at the current call level
     *     - call into self, grab its report (via recursion)
     *
     *
     * (ie. self report first, recursion last)
     *
     * NOTE: The last dApp call will REVERT, but none of the other dApps calls will revert. Thus
     * the report is 1 address less than the total, because the last dApp call generates no report.
     */
    @Test
    public void testInternalCallsIntoSelfDoingRecursionLast() {
        for (int i = 1; i < MAX_CALL_DEPTH; i++) {
            verifyRunningInternalCallsWithFailureFromDappIntoSelf(i, false);
        }
    }

    private void verifyRunningInternalCallsWithFailureFromDappIntoOtherDapp(int numInternalTransactionsToSpawn, boolean recurseFirst) {
        printTestContext("verifyRunningInternalCallsWithFailureFromDappIntoOtherDapp", numInternalTransactionsToSpawn, recurseFirst);

        Address topContract = deployFailedInternalCallAddressTrackerContract();
        Address[] deployedContracts = deployInternalCallAddressTrackerContracts(numInternalTransactionsToSpawn);
        printOrderOfContractCalls(topContract, deployedContracts);

        // Grab the 'report', the batch of all addresses that were tracked by the contract.
        Address[] report = callFailedInternalCallAddressesContract(topContract, deployedContracts, recurseFirst);

        // Extract the origin, caller and address addresses from the report.
        Address[] originAddresses = extractOriginAddressesFromReport(report);
        Address[] callerAddresses = extractCallerAddressesFromReport(report);
        Address[] contractAddresses = extractContractAddressesFromReport(report);

        // Verify that the sizes of the batches of addresses collected is correct.
        assertEquals(numInternalTransactionsToSpawn, originAddresses.length);
        assertEquals(numInternalTransactionsToSpawn, callerAddresses.length);
        assertEquals(numInternalTransactionsToSpawn, contractAddresses.length);

        // Every transaction should have the same origin address: the initial deployer.
        printExpectedOrderOfOriginAddresses(numInternalTransactionsToSpawn);
        printActualOrderOfOriginAddresses(originAddresses);

        for (Address origin : originAddresses) {
            assertEquals(from, origin);
        }

        // Each contract should have been called by the contract before it.
        int lengthWithLastContractDropped = Math.max(0, deployedContracts.length - 1);
        Address[] allCallersInOrder = (numInternalTransactionsToSpawn == 0)
            ? joinArrays(new Address[]{ from }, Arrays.copyOf(deployedContracts, lengthWithLastContractDropped))
            : joinArrays(new Address[]{ from, topContract }, Arrays.copyOf(deployedContracts, lengthWithLastContractDropped));

        // Remove the last caller since that contract is REVERT'd and so it produces no report.
        Address[] allCallersInOrderMinusLastCaller = Arrays.copyOf(allCallersInOrder, allCallersInOrder.length - 1);

        printExpectedOrderOfCallerAddresses(allCallersInOrderMinusLastCaller);
        printActualOrderOfCallerAddresses(callerAddresses);

        for (int i = 0; i < numInternalTransactionsToSpawn; i++) {
            assertEquals(allCallersInOrderMinusLastCaller[i], callerAddresses[i]);
        }

        // Each contract should have reported its own address for 'address'.
        Address[] allContractsInOrderOfBeingCalled = joinArrays(new Address[]{ topContract }, deployedContracts);

        // Remove the last contract since that contract is REVERT'd and so it produces no report.
        Address[] allContractsInOrderOfBeingCalledMinusLastContract = Arrays.copyOf(allContractsInOrderOfBeingCalled, allContractsInOrderOfBeingCalled.length - 1);

        printExpectedOrderOfContractAddresses(allContractsInOrderOfBeingCalledMinusLastContract);
        printActualOrderOfContractAddresses(contractAddresses);

        for (int i = 0; i < numInternalTransactionsToSpawn; i++) {
            assertEquals(allContractsInOrderOfBeingCalledMinusLastContract[i], contractAddresses[i]);
        }
    }

    private void verifyRunningInternalCallsWithFailureFromDappIntoSelf(int numInternalTransactionsToSpawn, boolean recurseFirst) {
        printTestContext("verifyRunningInternalCallsWithFailureFromDappIntoSelf", numInternalTransactionsToSpawn, recurseFirst);

        Address topContract = deployInternalCallAddressTrackerContract();
        Address[] deployedContracts = replicateSelfAcrossArray(topContract, numInternalTransactionsToSpawn);
        printOrderOfContractCalls(topContract, deployedContracts);

        // Grab the 'report', the batch of all addresses that were tracked by the contract.
        Address[] report = callFailedInternalCallAddressesContract(topContract, deployedContracts, recurseFirst);

        // Extract the origin, caller and address addresses from the report.
        Address[] originAddresses = extractOriginAddressesFromReport(report);
        Address[] callerAddresses = extractCallerAddressesFromReport(report);
        Address[] contractAddresses = extractContractAddressesFromReport(report);

        // Verify that the sizes of the batches of addresses collected is correct.
        assertEquals(numInternalTransactionsToSpawn, originAddresses.length);
        assertEquals(numInternalTransactionsToSpawn, callerAddresses.length);
        assertEquals(numInternalTransactionsToSpawn, contractAddresses.length);

        // Every transaction should have the same origin address: the initial deployer.
        printExpectedOrderOfOriginAddresses(numInternalTransactionsToSpawn);
        printActualOrderOfOriginAddresses(originAddresses);

        for (Address origin : originAddresses) {
            assertEquals(from, origin);
        }

        // Each contract should have been called by the contract before it.
        int lengthWithLastContractDropped = Math.max(0, deployedContracts.length - 1);
        Address[] allCallersInOrder = (numInternalTransactionsToSpawn == 0)
            ? joinArrays(new Address[]{ from }, Arrays.copyOf(deployedContracts, lengthWithLastContractDropped))
            : joinArrays(new Address[]{ from, topContract }, Arrays.copyOf(deployedContracts, lengthWithLastContractDropped));

        // Remove the last caller since that contract is REVERT'd and so it produces no report.
        Address[] allCallersInOrderMinusLastCaller = Arrays.copyOf(allCallersInOrder, allCallersInOrder.length - 1);

        printExpectedOrderOfCallerAddresses(allCallersInOrderMinusLastCaller);
        printActualOrderOfCallerAddresses(callerAddresses);

        for (int i = 0; i < numInternalTransactionsToSpawn; i++) {
            assertEquals(allCallersInOrderMinusLastCaller[i], callerAddresses[i]);
        }

        // Each contract should have reported its own address for 'address'.
        Address[] allContractsInOrderOfBeingCalled = joinArrays(new Address[]{ topContract }, deployedContracts);

        // Remove the last contract since that contract is REVERT'd and so it produces no report.
        Address[] allContractsInOrderOfBeingCalledMinusLastContract = Arrays.copyOf(allContractsInOrderOfBeingCalled, allContractsInOrderOfBeingCalled.length - 1);

        printExpectedOrderOfContractAddresses(allContractsInOrderOfBeingCalledMinusLastContract);
        printActualOrderOfContractAddresses(contractAddresses);

        for (int i = 0; i < numInternalTransactionsToSpawn; i++) {
            assertEquals(allContractsInOrderOfBeingCalledMinusLastContract[i], contractAddresses[i]);
        }
    }

    private Address[] callFailedInternalCallAddressesContract(Address contract, Address[] otherContracts, boolean recurseFirst) {
        byte[] callData;
        if (recurseFirst) {
            callData = ABIUtil.encodeMethodArguments("runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress", (Object)otherContracts);
        } else {
            callData = ABIUtil.encodeMethodArguments("runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse", (Object)otherContracts);
        }

        TransactionResult result = avmRule.call(from, contract, BigInteger.ZERO, callData, energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
        return (Address[]) ABIDecoder.decodeOneObject(result.getReturnData());
    }

    private static Address deployFailedInternalCallAddressTrackerContract() {
        TransactionResult result = avmRule.deploy(from, BigInteger.ZERO, avmRule.getDappBytes(FailedInternalCallAddressesContract.class, new byte[0]), energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
        return new Address(result.getReturnData());
    }

    private static Address[] deployInternalCallAddressTrackerContracts(int numContractsToDeploy) {
        Address[] contracts = new Address[numContractsToDeploy];
        for (int i = 0; i < numContractsToDeploy; i++) {
            contracts[i] = deployInternalCallAddressTrackerContract();
        }
        return contracts;
    }

    private static Address deployInternalCallAddressTrackerContract() {
        TransactionResult result = avmRule.deploy(from, BigInteger.ZERO, avmRule.getDappBytes(FailedInternalCallAddressesContract.class, new byte[0]), energyLimit, energyPrice).getTransactionResult();
        assertTrue(result.getResultCode().isSuccess());
        return new Address(result.getReturnData());
    }

    /**
     * ASSUMPTION: report is an array with the following recurring pattern of addresses:
     *
     *   [ origin, caller, address ]
     *
     * AND: report.length > 0
     */
    private static Address[] extractOriginAddressesFromReport(Address[] report) {
        return extractEveryThirdAddressWithOffset(report, 0);
    }

    /**
     * ASSUMPTION: report is an array with the following recurring pattern of addresses:
     *
     *   [ origin, caller, address ]
     *
     * AND: report.length > 0
     */
    private static Address[] extractCallerAddressesFromReport(Address[] report) {
        return extractEveryThirdAddressWithOffset(report, 1);
    }

    /**
     * ASSUMPTION: report is an array with the following recurring pattern of addresses:
     *
     *   [ origin, caller, address ]
     *
     * AND: report.length > 0
     */
    private static Address[] extractContractAddressesFromReport(Address[] report) {
        return extractEveryThirdAddressWithOffset(report, 2);
    }

    private static Address[] extractEveryThirdAddressWithOffset(Address[] addresses, int offset) {
        int length = addresses.length;

        // Verify the length is a multiple of 3.
        assertEquals(0, length % 3);

        // The offset can only be in the range [0, 3).
        assertTrue(offset >= 0);
        assertTrue(offset < 3);

        Address[] origins = new Address[length / 3];
        for (int i = 0; i < (length / 3); i++) {
            origins[i] = addresses[(i * 3) + offset];
        }

        return origins;
    }

    /**
     * Produces an array of length numArrayEntries such that every index in the array holds a
     * reference to self.
     */
    private static Address[] replicateSelfAcrossArray(Address self, int numArrayEntries) {
        Address[] array = new Address[numArrayEntries];
        for (int i = 0; i < numArrayEntries; i++) {
            array[i] = self;
        }
        return array;
    }

    private static void printTestContext(String testName, int numInternalTransactions, boolean recurseFirst) {
        if (ACTIVATE_OUTPUTS_FOR_DEBUGGING) {
            System.out.println("Running " + testName
                + " on " + numInternalTransactions + " internal calls "
                + "with recursion first: " + recurseFirst);
        }
    }

    private static void printActualOrderOfContractAddresses(Address[] contracts) {
        printAddressesWithLeadingMessage("Actual order of contract addresses", contracts, ",", true);
    }

    private static void printExpectedOrderOfContractAddresses(Address[] contracts) {
        printLineSeparator();
        printAddressesWithLeadingMessage("Expected order of contract addresses", contracts, ",", true);
    }

    private static void printActualOrderOfCallerAddresses(Address[] callers) {
        printAddressesWithLeadingMessage("Actual order of caller addresses", callers, ",", true);
    }

    private static void printExpectedOrderOfCallerAddresses(Address[] callers) {
        printLineSeparator();
        printAddressesWithLeadingMessage("Expected order of caller addresses", callers, ",", true);
    }

    private static void printActualOrderOfOriginAddresses(Address[] origins) {
        printAddressesWithLeadingMessage("Actual order of origin addresses", origins, ",", true);
    }

    private static void printExpectedOrderOfOriginAddresses(int numInternalCalls) {
        Address[] origins = new Address[numInternalCalls];
        for (int i = 0; i < numInternalCalls; i++) {
            origins[i] = from;
        }

        printLineSeparator();
        printAddressesWithLeadingMessage("Expected order of origin addresses", origins, ",", true);
    }

    private static void printOrderOfContractCalls(Address topContract, Address[] otherContracts) {
        printAddressesWithLeadingMessage(
            "Order of contract calls beginning with deployer address",
            joinArrays(new Address[]{ from, topContract }, otherContracts),
            "->",
            false);
    }

    private static void printLineSeparator() {
        if (ACTIVATE_OUTPUTS_FOR_DEBUGGING) {
            System.out.println("-----------------------------------");
        }
    }

    private static void printAddressesWithLeadingMessage(String message, Address[] addresses, String separator, boolean printAsSingleLine) {
        if (ACTIVATE_OUTPUTS_FOR_DEBUGGING) {
            String newlineOrNothing = (printAsSingleLine) ? "" : "\n\t";

            System.out.print(padEndingTo37Characters(message + ":") + "\t");
            System.out.print(newlineOrNothing + addressToStringShortForm(addresses[0]));
            for (Address address : Arrays.copyOfRange(addresses, 1, addresses.length)) {
                System.out.print(" " + separator + " " + addressToStringShortForm(address));
            }
            System.out.println();
        }
    }

    /**
     * For readability, 4 bytes should be enough to distinguish the addresses..
     */
    private static String addressToStringShortForm(Address address) {
        String addressAsString = address.toString();
        return addressAsString.substring(0, 8) + "..";
    }

    /**
     * Why 37? That's our longest message... this just ensures the tabs all line up; pain to read
     * the logs otherwise.
     */
    private static String padEndingTo37Characters(String string) {
        if (string.length() >= 37) {
            return string;
        } else {
            return string + String.valueOf(new char[37 - string.length()]).replace('\0', ' ');
        }
    }

    private static Address[] joinArrays(Address[] array1, Address[] array2) {
        Address[] array = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        return array;
    }


}
