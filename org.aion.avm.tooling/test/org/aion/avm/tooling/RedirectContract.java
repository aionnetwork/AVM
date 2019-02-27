package org.aion.avm.tooling;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

/**
 * A contract whose sole purpose is to re-direct the call (that is, make an internal transaction
 * call into another contract).
 *
 * This is here for the general assistance of tests, so that they can test out internal calls easily.
 *
 * The result of the call is saved and its fields can be accessed via method calls.
 */
public class RedirectContract {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(RedirectContract.class, BlockchainRuntime.getData());
    }

    /**
     * Calls into the contract deployed at the specified address, transferring the specified amount
     * of value and calling the contract with the given ABI-encoded arguments.
     *
     * If the called contract is not SUCCESS this method will revert.
     * Otherwise this method will succeed and will return the data outputted by the contract call.
     */
    public static byte[] callOtherContractAndRequireItIsSuccess(Address addressOfOther, long value, byte[] args) {
        Result result = BlockchainRuntime.call(addressOfOther, BigInteger.valueOf(value), args, BlockchainRuntime.getRemainingEnergy());
        BlockchainRuntime.require(result.isSuccess());
        return (result.getReturnData() != null) ? (byte[]) ABIDecoder.decodeOneObject(result.getReturnData()) : null;
    }

}
