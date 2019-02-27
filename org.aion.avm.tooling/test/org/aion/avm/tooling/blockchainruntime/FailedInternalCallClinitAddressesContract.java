package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

public class FailedInternalCallClinitAddressesContract {
    private static final Address ORIGIN = BlockchainRuntime.getOrigin();
    private static final Address CALLER = BlockchainRuntime.getCaller();
    private static final Address CONTRACT = BlockchainRuntime.getAddress();

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(FailedInternalCallClinitAddressesContract.class, BlockchainRuntime.getData());
    }

    /**
     * Triggers a chain of internal transactions such that this contract will create a contract and
     * call into it and so on, forming a chain of numOtherContracts contract calls.
     *
     * Each of the called contracts will report their address, caller and origin - as these values
     * have been set in their clinits.
     *
     * The returned array for N calls deep will look like this (assume external transaction has
     * depth zero):
     *
     *   index 0: origin for contract at depth 0
     *   index 1: caller for contract at depth 0
     *   index 2: address for contract at depth 0
     *   ...
     *   index (N * 3): origin for contract at depth N
     *   index (N * 3) + 1: caller for contract at depth N
     *   index (N * 3) + 2: address for contract at depth N
     *
     *
     *   ASSUMPTION: dappBytes are the bytes of THIS dapp.
     *
     *   NOTE: the deepest call in this chain will do a REVERT.
     */
    public static Address[] runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress(byte[] dappBytes, int numOtherContracts) {
        return recurseAndTrackAddressesByRecursingFirst(dappBytes, numOtherContracts, 0);
    }

    /**
     * Triggers a chain of internal transactions such that this contract will create a contract and
     * call into it and so on, forming a chain of numOtherContracts contract calls.
     *
     * Each of the called contracts will report their address, caller and origin - as these values
     * have been set in their clinits.
     *
     * The returned array for N calls deep will look like this (assume external transaction has
     * depth zero):
     *
     *   index 0: origin for contract at depth 0
     *   index 1: caller for contract at depth 0
     *   index 2: address for contract at depth 0
     *   ...
     *   index (N * 3): origin for contract at depth N
     *   index (N * 3) + 1: caller for contract at depth N
     *   index (N * 3) + 2: address for contract at depth N
     *
     *
     *   ASSUMPTION: dappBytes are the bytes of THIS dapp.
     *
     *   NOTE: the deepest call in this chain will do a REVERT.
     */
    public static Address[] runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(byte[] dappBytes, int numOtherContracts) {
        return recurseAndTrackAddressesByRecursingLast(dappBytes, numOtherContracts, 0);
    }

    public static Address[] recurseAndTrackAddressesByRecursingFirst(byte[] dappBytes, int numOtherContracts, int currentDepth) {
        return recurseAndTrackAddresses(dappBytes, numOtherContracts, currentDepth, true);
    }

    public static Address[] recurseAndTrackAddressesByRecursingLast(byte[] dappBytes, int numOtherContracts, int currentDepth) {
        return recurseAndTrackAddresses(dappBytes, numOtherContracts, currentDepth, false);
    }

    public static Address[] recurseAndTrackAddresses(byte[] dappBytes, int numOtherContracts, int currentDepth, boolean recurseFirst) {
        if (currentDepth < numOtherContracts) {
            Address[] reportForThisContract = null;

            if (!recurseFirst) {
                reportForThisContract = getAddresses();
            }

            // First, we create the dapp we are going to call into.
            Result createResult = BlockchainRuntime.create(BigInteger.ZERO, dappBytes, BlockchainRuntime.getRemainingEnergy());

            // This way we actually know if something went wrong...
            if (!createResult.isSuccess()) {
                BlockchainRuntime.revert();
            }

            // Grab the address of the newly created dapp.
            Address newDappAddress = new Address(createResult.getReturnData());

            // Now call into the dapp. We assume its code is this same class, so this is 'recursive'.
            byte[] callData = ABIEncoder.encodeMethodArguments("recurseAndTrackAddresses", dappBytes, numOtherContracts, currentDepth + 1, recurseFirst);
            Result callResult = BlockchainRuntime.call(newDappAddress, BigInteger.ZERO, callData, BlockchainRuntime.getRemainingEnergy());

            // check the revert on the deepest child.
            if (currentDepth == numOtherContracts - 1) {
                if (callResult.isSuccess()) {
                    throw new IllegalStateException("Child of me was supposed to revert but did not! Child: " + newDappAddress);
                }
            } else {
                // not the deepest child, so something actually went wrong...
                if (!callResult.isSuccess()) {
                    BlockchainRuntime.revert();
                }
            }

            if (recurseFirst) {
                reportForThisContract = getAddresses();
            }

            // then this child is the deepest and they failed so just return my report.
            if (!callResult.isSuccess()) {
                return reportForThisContract;
            }

            Address[] reportForOtherContracts = (Address[]) ABIDecoder.decodeOneObject(callResult.getReturnData());
            return joinArrays(reportForThisContract, reportForOtherContracts);
        } else {
            BlockchainRuntime.revert();
            return null;
        }
    }

    /**
     * Returns an array of length 3 of the following addresses in this order:
     *
     *   [ origin, caller, address ]
     */
    private static Address[] getAddresses() {
        Address[] addresses = new Address[3];
        addresses[0] = ORIGIN;
        addresses[1] = CALLER;
        addresses[2] = CONTRACT;
        return addresses;
    }

    /**
     * Returns a concatenation of array1 and array2 in this order.
     */
    private static Address[] joinArrays(Address[] array1, Address[] array2) {
        Address[] array = new Address[array1.length + array2.length];

        for (int i = 0; i < array1.length; i++) {
            array[i] = array1[i];
        }

        for (int i = 0; i < array2.length; i++) {
            array[array1.length + i] = array2[i];
        }

        return array;
    }

}
