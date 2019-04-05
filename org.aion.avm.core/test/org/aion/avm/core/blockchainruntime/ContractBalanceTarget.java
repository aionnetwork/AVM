package org.aion.avm.core.blockchainruntime;

import java.math.BigInteger;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ContractBalanceTarget {
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
            if (methodName.equals("getBalanceOfThisContract")) {
                return ABIEncoder.encodeOneByteArray(getBalanceOfThisContract());
            } else if(methodName.equals("getBalanceOfThisContractDuringClinit")) {
                return ABIEncoder.encodeOneByteArray(getBalanceOfThisContractDuringClinit());
            } else {
                return new byte[0];
            }
        }
    }

    /**
     * Returns the BigInteger.toByteArray() representation of the contract balance.
     */
    public static byte[] getBalanceOfThisContract() {
        return Blockchain.getBalanceOfThisContract().toByteArray();
    }

    /**
     * Returns the BigInteger.toByteArray() representation of the contract balance at the time of
     * running the clinit code.
     */
    public static byte[] getBalanceOfThisContractDuringClinit() {
        return balanceDuringClinit.toByteArray();
    }

}
