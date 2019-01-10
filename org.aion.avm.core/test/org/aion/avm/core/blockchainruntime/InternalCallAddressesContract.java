package org.aion.avm.core.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

/**
 * A contract that tracks the following addresses in internal calls: origin, caller, contract
 */
public class InternalCallAddressesContract {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(InternalCallAddressesContract.class, BlockchainRuntime.getData());
    }

    /**
     * Triggers a chain of internal transactions such that this contract will call into contract
     * otherContracts[0], who will call into otherContracts[1], and so on until the final contract
     * is called.
     *
     * Each of the called contracts will report their address, caller and origin.
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
     *   ASSUMPTION: all of the other contracts are instance of InternalCallAddressTrackerContract!
     */
    public static Address[] runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress(Address[] otherContracts) {
        return recurseAndTrackAddressesByRecursingFirst(otherContracts, 0);
    }

    /**
     * Triggers a chain of internal transactions such that this contract will call into contract
     * otherContracts[0], who will call into otherContracts[1], and so on until the final contract
     * is called.
     *
     * Each of the called contracts will report their address, caller and origin.
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
     *   ASSUMPTION: all of the other contracts are instance of InternalCallAddressTrackerContract!
     */
    public static Address[] runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(Address[] otherContracts) {
        return recurseAndTrackAddressesByRecursingLast(otherContracts, 0);
    }

    public static Address[] recurseAndTrackAddressesByRecursingLast(Address[] otherContracts, int currentDepth) {
        return recurseAndTrackAddresses(otherContracts, currentDepth, false);
    }

    public static Address[] recurseAndTrackAddressesByRecursingFirst(Address[] otherContracts, int currentDepth) {
        return recurseAndTrackAddresses(otherContracts, currentDepth, true);
    }

    public static Address[] recurseAndTrackAddresses(Address[] otherContracts, int currentDepth, boolean recurseFirst) {
        if (currentDepth < otherContracts.length) {
            Address[] reportForThisContract = null;

            if (!recurseFirst) {
                reportForThisContract = getAddresses();
            }

            byte[] data = ABIEncoder.encodeMethodArguments("recurseAndTrackAddresses", otherContracts, currentDepth + 1, recurseFirst);
            Result result = BlockchainRuntime.call(otherContracts[currentDepth], BigInteger.ZERO, data, BlockchainRuntime.getRemainingEnergy());

            // This way we actually know if something went wrong...
            if (!result.isSuccess()) {
                BlockchainRuntime.revert();
            }

            if (recurseFirst) {
                reportForThisContract = getAddresses();
            }

            Address[] reportForOtherContracts = (Address[]) ABIDecoder.decodeOneObject(result.getReturnData());
            return joinArrays(reportForThisContract, reportForOtherContracts);
        } else {
            return getAddresses();
        }
    }

    /**
     * Returns an array of length 3 of the following addresses in this order:
     *
     *   [ origin, caller, address ]
     */
    private static Address[] getAddresses() {
        Address[] addresses = new Address[3];
        addresses[0] = BlockchainRuntime.getOrigin();
        addresses[1] = BlockchainRuntime.getCaller();
        addresses[2] = BlockchainRuntime.getAddress();
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
