package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

public class InternalCallContractBalanceTarget {
    private static BigInteger balanceDuringClinit;

    static {
        balanceDuringClinit = BlockchainRuntime.getBalanceOfThisContract();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("getBalanceOfDappViaInternalCall")) {
                return ABIEncoder.encodeOneByteArray(getBalanceOfDappViaInternalCall(decoder.decodeOneAddressArray(), decoder.decodeOneInteger()));
            } else if (methodName.equals("createNewContractWithValue")) {
                return ABIEncoder.encodeOneByteArray(createNewContractWithValue(decoder.decodeOneByteArray(), decoder.decodeOneByteArray(), decoder.decodeOneLong()));
            } else if (methodName.equals("recurseAndGetBalance")) {
                return ABIEncoder.encodeOneByteArray(recurseAndGetBalance(decoder.decodeOneAddressArray(), decoder.decodeOneInteger(), decoder.decodeOneInteger()));
            } else if (methodName.equals("getBalanceOfThisContractDuringClinit")) {
                return ABIEncoder.encodeOneByteArray(getBalanceOfThisContractDuringClinit());
            } else {
                return new byte[0];
            }
        }
    }

    /**
     * Returns the BigInteger.toByteArray() representation of the balance of the contract at depth
     */
    public static byte[] getBalanceOfDappViaInternalCall(Address[] otherContracts, int targetDappDepth) {
        return recurseAndGetBalance(otherContracts, 0, targetDappDepth);
    }

    /**
     * This method will deploy a new contract with the supplied code and transfer value to it during
     * that deployment transaction.
     *
     * Returns the address of the newly created contract.
     */
    public static byte[] createNewContractWithValue(byte[] dappBytesFirstHalf, byte[] dappBytesSecondHalf, long amountToTransfer) {
        // Create the child contract.

        byte[] dappCode = new byte[dappBytesFirstHalf.length + dappBytesSecondHalf.length];
        for(int i = 0; i < dappBytesFirstHalf.length; i++) {
            dappCode[i] = dappBytesFirstHalf[i];
        }
        for(int i = 0, j = dappBytesFirstHalf.length; i < dappBytesSecondHalf.length; i++, j++) {
            dappCode[j] = dappBytesSecondHalf[i];
        }

        Result result = BlockchainRuntime.create(BigInteger.valueOf(amountToTransfer), dappCode, BlockchainRuntime.getRemainingEnergy());

        // If the create failed then we revert to propagate this failure upwards.
        if (!result.isSuccess()) {
            BlockchainRuntime.revert();
        }

        return result.getReturnData();
    }

    public static byte[] recurseAndGetBalance(Address[] otherContracts, int currentDepth, int targetDappDepth) {
        if (currentDepth < otherContracts.length) {

            if (currentDepth == targetDappDepth) {
                // I'm the target, so return my balance.
                return BlockchainRuntime.getBalanceOfThisContract().toByteArray();
            } else {
                // I'm not the target, propagate whatever my child returned upwards to my caller.

                byte[] arg0Bytes = ABIEncoder.encodeOneString("recurseAndGetBalance");
                byte[] arg1Bytes = ABIEncoder.encodeOneAddressArray(otherContracts);
                byte[] arg2Bytes = ABIEncoder.encodeOneInteger(currentDepth + 1);
                byte[] arg3Bytes = ABIEncoder.encodeOneInteger(targetDappDepth);
                byte[] data = concatenateArrays(arg0Bytes, arg1Bytes, arg2Bytes, arg3Bytes);

                ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.call(otherContracts[currentDepth], BigInteger.ZERO, data, BlockchainRuntime.getRemainingEnergy()).getReturnData());
                return decoder.decodeOneByteArray();
            }

        } else {
            // I'm the deepest call, return an empty array unless I'm the target, then return my balance.
            return (currentDepth == targetDappDepth) ? BlockchainRuntime.getBalanceOfThisContract().toByteArray() : new byte[0];
        }
    }

    /**
     * Returns the BigInteger.toByteArray() representation of the contract balance at the time of
     * running the clinit code.
     */
    public static byte[] getBalanceOfThisContractDuringClinit() {
        return balanceDuringClinit.toByteArray();
    }

    private static byte[] concatenateArrays(byte[]... arrays) {
        int length = 0;
        for(byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int writtenSoFar = 0;
        for(byte[] array : arrays) {
            System.arraycopy(array, 0, result, writtenSoFar, array.length);
            writtenSoFar += array.length;
        }
        return result;
    }
}
