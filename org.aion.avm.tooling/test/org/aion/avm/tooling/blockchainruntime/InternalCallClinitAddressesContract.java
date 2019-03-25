package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

public class InternalCallClinitAddressesContract {
    private static final Address ORIGIN = BlockchainRuntime.getOrigin();
    private static final Address CALLER = BlockchainRuntime.getCaller();
    private static final Address CONTRACT = BlockchainRuntime.getAddress();

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress")) {
                return ABIEncoder.encodeOneObject(runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress((byte[]) argValues[0], (byte[]) argValues[1], (Integer) argValues[2]));
            } else if (methodName.equals("runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse")) {
                return ABIEncoder.encodeOneObject(runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse((byte[]) argValues[0], (byte[]) argValues[1], (Integer) argValues[2]));
            } else if (methodName.equals("recurseAndTrackAddresses")) {
                return ABIEncoder.encodeOneObject(recurseAndTrackAddresses((byte[]) argValues[0], (byte[]) argValues[1], (Integer) argValues[2], (Integer) argValues[3], (Boolean) argValues[4]));
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
     *   ASSUMPTION: dappBytesFirstHalf and dappBytesSecondHalf when concatenated are the bytes of THIS dapp.
     */
    @Callable
    public static Address[] runInternalCallsAndTrackAddressRecurseThenGrabOwnAddress(byte[] dappBytesFirstHalf, byte[] dappBytesSecondHalf, int numOtherContracts) {
        return recurseAndTrackAddressesByRecursingFirst(dappBytesFirstHalf, dappBytesSecondHalf, numOtherContracts, 0);
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
     */
    @Callable
    public static Address[] runInternalCallsAndTrackAddressGrabOwnAddressThenRecurse(byte[] dappBytesFirstHalf, byte[] dappBytesSecondHalf, int numOtherContracts) {
        return recurseAndTrackAddressesByRecursingLast(dappBytesFirstHalf, dappBytesSecondHalf, numOtherContracts, 0);
    }

    public static Address[] recurseAndTrackAddressesByRecursingFirst(byte[] dappBytesFirstHalf, byte[] dappBytesSecondHalf, int numOtherContracts, int currentDepth) {
        return recurseAndTrackAddresses(dappBytesFirstHalf, dappBytesSecondHalf, numOtherContracts, currentDepth, true);
    }

    public static Address[] recurseAndTrackAddressesByRecursingLast(byte[] dappBytesFirstHalf, byte[] dappBytesSecondHalf, int numOtherContracts, int currentDepth) {
        return recurseAndTrackAddresses(dappBytesFirstHalf, dappBytesSecondHalf, numOtherContracts, currentDepth, false);
    }

    @Callable
    public static Address[] recurseAndTrackAddresses(byte[] dappBytesFirstHalf, byte[] dappBytesSecondHalf, int numOtherContracts, int currentDepth, boolean recurseFirst) {
        if (currentDepth < numOtherContracts) {
            Address[] reportForThisContract = null;

            if (!recurseFirst) {
                reportForThisContract = getAddresses();
            }

            // First, we create the dapp we are going to call into.

            byte[] dappBytes = new byte[dappBytesFirstHalf.length + dappBytesSecondHalf.length];
            for(int i = 0; i < dappBytesFirstHalf.length; i++) {
                dappBytes[i] = dappBytesFirstHalf[i];
            }
            for(int i = 0, j = dappBytesFirstHalf.length; i < dappBytesSecondHalf.length; i++, j++) {
                dappBytes[j] = dappBytesSecondHalf[i];
            }

            Result createResult = BlockchainRuntime.create(BigInteger.ZERO, dappBytes, BlockchainRuntime.getRemainingEnergy());

            // This way we actually know if something went wrong...
            if (!createResult.isSuccess()) {
                BlockchainRuntime.revert();
            }

            // Grab the address of the newly created dapp.
            Address newDappAddress = new Address(createResult.getReturnData());

            // Now call into the dapp. We assume its code is this same class, so this is 'recursive'.
            byte[] callData = ABIEncoder.encodeMethodArguments("recurseAndTrackAddresses", dappBytesFirstHalf, dappBytesSecondHalf, numOtherContracts, currentDepth + 1, recurseFirst);            Result callResult = BlockchainRuntime.call(newDappAddress, BigInteger.ZERO, callData, BlockchainRuntime.getRemainingEnergy());

            // This way we actually know if something went wrong...
            if (!callResult.isSuccess()) {
                BlockchainRuntime.revert();
            }

            if (recurseFirst) {
                reportForThisContract = getAddresses();
            }

            Address[] reportForOtherContracts = (Address[]) ABIDecoder.decodeOneObject(callResult.getReturnData());
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
