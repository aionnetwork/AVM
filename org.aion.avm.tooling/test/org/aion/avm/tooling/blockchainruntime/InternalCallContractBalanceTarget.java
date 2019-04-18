package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import avm.Address;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

public class InternalCallContractBalanceTarget {
    private static BigInteger balanceDuringClinit;

    static {
        balanceDuringClinit = Blockchain.getBalanceOfThisContract();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("getBalanceOfDappViaInternalCall")) {
                return ABIEncoder.encodeOneByteArray(getBalanceOfDappViaInternalCall(decoder.decodeOneAddressArray(), decoder.decodeOneInteger()));
            } else if (methodName.equals("createNewContractWithValue")) {
                return ABIEncoder.encodeOneByteArray(createNewContractWithValue(decoder.decodeOneByteArray(), decoder.decodeOneLong()));
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
    public static byte[] createNewContractWithValue(byte[] dappBytes, long amountToTransfer) {
        // Create the child contract.

        Result result = Blockchain.create(BigInteger.valueOf(amountToTransfer), dappBytes, Blockchain.getRemainingEnergy());

        // If the create failed then we revert to propagate this failure upwards.
        if (!result.isSuccess()) {
            Blockchain.revert();
        }

        return result.getReturnData();
    }

    public static byte[] recurseAndGetBalance(Address[] otherContracts, int currentDepth, int targetDappDepth) {
        if (currentDepth < otherContracts.length) {

            if (currentDepth == targetDappDepth) {
                // I'm the target, so return my balance.
                return Blockchain.getBalanceOfThisContract().toByteArray();
            } else {
                // I'm not the target, propagate whatever my child returned upwards to my caller.

                ABIStreamingEncoder encoder = new ABIStreamingEncoder();
                byte[] data = encoder.encodeOneString("recurseAndGetBalance")
                    .encodeOneAddressArray(otherContracts)
                    .encodeOneInteger(currentDepth + 1)
                    .encodeOneInteger(targetDappDepth)
                    .toBytes();

                ABIDecoder decoder = new ABIDecoder(Blockchain.call(otherContracts[currentDepth], BigInteger.ZERO, data, Blockchain.getRemainingEnergy()).getReturnData());
                return decoder.decodeOneByteArray();
            }

        } else {
            // I'm the deepest call, return an empty array unless I'm the target, then return my balance.
            return (currentDepth == targetDappDepth) ? Blockchain.getBalanceOfThisContract().toByteArray() : new byte[0];
        }
    }

    /**
     * Returns the BigInteger.toByteArray() representation of the contract balance at the time of
     * running the clinit code.
     */
    public static byte[] getBalanceOfThisContractDuringClinit() {
        return balanceDuringClinit.toByteArray();
    }
}
