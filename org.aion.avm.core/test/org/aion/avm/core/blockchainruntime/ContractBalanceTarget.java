package org.aion.avm.core.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ContractBalanceTarget {
    private static BigInteger balanceDuringClinit;

    static {
        balanceDuringClinit = BlockchainRuntime.getBalanceOfThisContract();
    }

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("getBalanceOfThisContract")) {
                return ABIEncoder.encodeOneObject(getBalanceOfThisContract());
            } else if(methodName.equals("getBalanceOfThisContractDuringClinit")) {
                return ABIEncoder.encodeOneObject(getBalanceOfThisContractDuringClinit());
            } else {
                return new byte[0];
            }
        }
    }

    /**
     * Returns the BigInteger.toByteArray() representation of the contract balance.
     */
    public static byte[] getBalanceOfThisContract() {
        return BlockchainRuntime.getBalanceOfThisContract().toByteArray();
    }

    /**
     * Returns the BigInteger.toByteArray() representation of the contract balance at the time of
     * running the clinit code.
     */
    public static byte[] getBalanceOfThisContractDuringClinit() {
        return balanceDuringClinit.toByteArray();
    }

}
