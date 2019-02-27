package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

public class FailedInternalCallAddressesContract {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(FailedInternalCallAddressesContract.class, BlockchainRuntime.getData());
    }

    public static Address[] runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress(Address[] otherContracts) {
        return recurseAndTrackAddresses(otherContracts, 0, true);
    }

    public static Address[] runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(Address[] otherContracts) {
        return recurseAndTrackAddresses(otherContracts, 0, false);
    }

    public static Address[] recurseAndTrackAddresses(Address[] otherContracts, int currentDepth, boolean recurseFirst) {
        if (currentDepth < otherContracts.length) {
            Address[] reportForThisContract = null;

            if (!recurseFirst) {
                reportForThisContract = getAddresses();
            }

            byte[] data = ABIEncoder.encodeMethodArguments("recurseAndTrackAddresses", otherContracts, currentDepth + 1, recurseFirst);
            Result result = BlockchainRuntime.call(otherContracts[currentDepth], BigInteger.ZERO, data, BlockchainRuntime.getRemainingEnergy());

            // check the revert on the deepest child.
            if (currentDepth == otherContracts.length - 1) {
                if (result.isSuccess()) {
                    throw new IllegalStateException("Child of me was supposed to revert but did not! Child: " + otherContracts[currentDepth]);
                }
            } else {
                // not the deepest child, so something actually went wrong...
                if (!result.isSuccess()) {
                    BlockchainRuntime.revert();
                }
            }

            if (recurseFirst) {
                reportForThisContract = getAddresses();
            }

            // then this child is the deepest and they failed so just return my report.
            if (!result.isSuccess()) {
                return reportForThisContract;
            }

            Address[] reportForOtherContracts = (Address[]) ABIDecoder.decodeOneObject(result.getReturnData());
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
