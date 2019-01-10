package org.aion.avm.core.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

public class InternalCallContractBalanceTarget {
    private static BigInteger balanceDuringClinit;

    static {
        balanceDuringClinit = BlockchainRuntime.getBalanceOfThisContract();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(InternalCallContractBalanceTarget.class, BlockchainRuntime.getData());
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
    public static byte[] createNewContractWithValue(byte[] dappCode, long amountToTransfer) {
        // Create the child contract.
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
                byte[] data = ABIEncoder.encodeMethodArguments("recurseAndGetBalance", otherContracts, currentDepth + 1, targetDappDepth);
                return (byte[]) ABIDecoder.decodeOneObject(BlockchainRuntime.call(otherContracts[currentDepth], BigInteger.ZERO, data, BlockchainRuntime.getRemainingEnergy()).getReturnData());
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

}
