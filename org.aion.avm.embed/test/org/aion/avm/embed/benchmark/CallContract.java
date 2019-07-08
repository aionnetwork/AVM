package org.aion.avm.embed.benchmark;

import avm.Address;
import avm.Blockchain;
import avm.Result;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

import java.math.BigInteger;


/**
 * Tests cross-contract and reentrant calls.
 * Call "setOtherCallee" to set the downstream address (which may be the receiver, if this should be directly reentrant).  Note that this
 * contract assumes that the target has the same interface.
 * Call "callOtherContract" to begin the cross-call invocation sequence.
 */
public class CallContract {
    private static Address calleeAddress;

    @Callable
    public static boolean callOtherContract(int currentDepth, int targetDepth) {
        // The leaf call is implicitly "true".
        boolean didSucceed = true;
        if (currentDepth < targetDepth) {
            // We will encode directly into the callData buffer and just use it - this buffer must be precisely 30 bytes.
            byte[] callData = new byte[30];
            ABIStreamingEncoder encoder = new ABIStreamingEncoder(callData);
            encoder
                .encodeOneString("callOtherContract")
                .encodeOneInteger(currentDepth + 1)
                .encodeOneInteger(targetDepth);
            Result callResult = Blockchain.call(calleeAddress, BigInteger.ZERO, callData, Blockchain.getRemainingEnergy());
            // If this was a success, just return whatever our child call was.
            didSucceed = callResult.isSuccess()
                    ? new ABIDecoder(callResult.getReturnData()).decodeOneBoolean()
                    : false;
        }
        return didSucceed;
    }

    @Callable
    public static void setOtherCallee(Address callee) {
        calleeAddress = callee;
    }
}
