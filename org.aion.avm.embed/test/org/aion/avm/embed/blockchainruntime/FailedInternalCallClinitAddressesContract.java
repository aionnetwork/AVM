package org.aion.avm.embed.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import avm.Address;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

public class FailedInternalCallClinitAddressesContract {
    private static final Address ORIGIN = Blockchain.getOrigin();
    private static final Address CALLER = Blockchain.getCaller();
    private static final Address CONTRACT = Blockchain.getAddress();

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress")) {
                return ABIEncoder.encodeOneAddressArray(runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress(decoder.decodeOneByteArray(), decoder.decodeOneInteger()));
            } else if (methodName.equals("runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse")) {
                return ABIEncoder.encodeOneAddressArray(runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(decoder.decodeOneByteArray(), decoder.decodeOneInteger()));
            } else if (methodName.equals("recurseAndTrackAddresses")) {
                return ABIEncoder.encodeOneAddressArray(recurseAndTrackAddresses(decoder.decodeOneByteArray(), decoder.decodeOneInteger(), decoder.decodeOneInteger(), decoder.decodeOneBoolean()));
            } else {
                return new byte[0];
            }
        }
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
    @Callable
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
     *   ASSUMPTION: dappBytes is the bytes of THIS dapp.
     *
     *   NOTE: the deepest call in this chain will do a REVERT.
     */
    @Callable
    public static Address[] runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(byte[] dappBytes, int numOtherContracts) {
        return recurseAndTrackAddressesByRecursingLast(dappBytes, numOtherContracts, 0);
    }

    public static Address[] recurseAndTrackAddressesByRecursingFirst(byte[] dappBytes, int numOtherContracts, int currentDepth) {
        return recurseAndTrackAddresses(dappBytes, numOtherContracts, currentDepth, true);
    }

    public static Address[] recurseAndTrackAddressesByRecursingLast(byte[] dappBytes, int numOtherContracts, int currentDepth) {
        return recurseAndTrackAddresses(dappBytes, numOtherContracts, currentDepth, false);
    }

    @Callable
    public static Address[] recurseAndTrackAddresses(byte[] dappBytes, int numOtherContracts, int currentDepth, boolean recurseFirst) {
        if (currentDepth < numOtherContracts) {
            Address[] reportForThisContract = null;

            if (!recurseFirst) {
                reportForThisContract = getAddresses();
            }

            // First, we create the dapp we are going to call into.

            Result createResult = Blockchain.create(BigInteger.ZERO, dappBytes, Blockchain.getRemainingEnergy());

            // This way we actually know if something went wrong...
            if (!createResult.isSuccess()) {
                Blockchain.revert();
            }

            // Grab the address of the newly created dapp.
            Address newDappAddress = new Address(createResult.getReturnData());

            // Now call into the dapp. We assume its code is this same class, so this is 'recursive'.

            ABIStreamingEncoder encoder = new ABIStreamingEncoder();
            byte[] callData = encoder.encodeOneString("recurseAndTrackAddresses")
                .encodeOneByteArray(dappBytes)
                .encodeOneInteger(numOtherContracts)
                .encodeOneInteger(currentDepth + 1)
                .encodeOneBoolean(recurseFirst)
                .toBytes();

            Result callResult = Blockchain.call(newDappAddress, BigInteger.ZERO, callData, Blockchain.getRemainingEnergy());

            // check the revert on the deepest child.
            if (currentDepth == numOtherContracts - 1) {
                if (callResult.isSuccess()) {
                    throw new IllegalStateException("Child of me was supposed to revert but did not! Child: " + newDappAddress);
                }
            } else {
                // not the deepest child, so something actually went wrong...
                if (!callResult.isSuccess()) {
                    Blockchain.revert();
                }
            }

            if (recurseFirst) {
                reportForThisContract = getAddresses();
            }

            // then this child is the deepest and they failed so just return my report.
            if (!callResult.isSuccess()) {
                return reportForThisContract;
            }

            ABIDecoder decoder = new ABIDecoder(callResult.getReturnData());
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
