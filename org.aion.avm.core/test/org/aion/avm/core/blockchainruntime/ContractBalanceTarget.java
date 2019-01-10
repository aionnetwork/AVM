package org.aion.avm.core.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class ContractBalanceTarget {
    private static BigInteger balanceDuringClinit;

    static {
        balanceDuringClinit = BlockchainRuntime.getBalanceOfThisContract();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(ContractBalanceTarget.class, BlockchainRuntime.getData());
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
