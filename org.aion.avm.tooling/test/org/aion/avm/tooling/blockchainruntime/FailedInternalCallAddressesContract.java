package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

public class FailedInternalCallAddressesContract {

    @Callable
    public static Address[] runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress(Address[] otherContracts) {
        return recurseAndTrackAddresses(otherContracts, 0, true);
    }

    @Callable
    public static Address[] runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(Address[] otherContracts) {
        return recurseAndTrackAddresses(otherContracts, 0, false);
    }

    @Callable
    public static Address[] recurseAndTrackAddresses(Address[] otherContracts, int currentDepth, boolean recurseFirst) {
        if (currentDepth < otherContracts.length) {
            Address[] reportForThisContract = null;

            if (!recurseFirst) {
                reportForThisContract = getAddresses();
            }

            ABIStreamingEncoder encoder = new ABIStreamingEncoder();
            byte[] data = encoder.encodeOneString("recurseAndTrackAddresses")
                .encodeOneAddressArray(otherContracts)
                .encodeOneInteger(currentDepth + 1)
                .encodeOneBoolean(recurseFirst)
                .toBytes();

            Result result = Blockchain.call(otherContracts[currentDepth], BigInteger.ZERO, data, Blockchain.getRemainingEnergy());

            // check the revert on the deepest child.
            if (currentDepth == otherContracts.length - 1) {
                if (result.isSuccess()) {
                    throw new IllegalStateException("Child of me was supposed to revert but did not! Child: " + otherContracts[currentDepth]);
                }
            } else {
                // not the deepest child, so something actually went wrong...
                if (!result.isSuccess()) {
                    Blockchain.revert();
                }
            }

            if (recurseFirst) {
                reportForThisContract = getAddresses();
            }

            // then this child is the deepest and they failed so just return my report.
            if (!result.isSuccess()) {
                return reportForThisContract;
            }

            ABIDecoder decoder = new ABIDecoder(result.getReturnData());
            Address[] reportForOtherContracts = decoder.decodeOneAddressArray();
            return joinArrays(reportForThisContract, reportForOtherContracts);
        } else {
            Blockchain.revert();
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
        addresses[0] = Blockchain.getOrigin();
        addresses[1] = Blockchain.getCaller();
        addresses[2] = Blockchain.getAddress();
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
