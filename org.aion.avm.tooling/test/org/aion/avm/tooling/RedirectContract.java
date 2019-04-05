package org.aion.avm.tooling;

import java.math.BigInteger;
import avm.Address;
import avm.BlockchainRuntime;
import avm.Result;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;

/**
 * A contract whose sole purpose is to re-direct the call (that is, make an internal transaction
 * call into another contract).
 *
 * This is here for the general assistance of tests, so that they can test out internal calls easily.
 *
 * The result of the call is saved and its fields can be accessed via method calls.
 */
public class RedirectContract {

    /**
     * Calls into the contract deployed at the specified address, transferring the specified amount
     * of value and calling the contract with the given ABI-encoded arguments.
     *
     * If the called contract is not SUCCESS this method will revert.
     * Otherwise this method will succeed and will return the data outputted by the contract call.
     */
    @Callable
    public static byte[] callOtherContractAndRequireItIsSuccess(Address addressOfOther, long value, byte[] args) {
        Result result = BlockchainRuntime.call(addressOfOther, BigInteger.valueOf(value), args, BlockchainRuntime.getRemainingEnergy());
        BlockchainRuntime.require(result.isSuccess());
        if (null == result.getReturnData() || 0 == result.getReturnData().length) {
            return null;
        } else {
            // we should never get here since all Callables in RequireTarget are of type void
            ABIDecoder decoder = new ABIDecoder(result.getReturnData());
            return decoder.decodeOneByteArray();
        }
    }

}
