package org.aion.avm.core;

import org.aion.avm.rt.BlockchainRuntime;


/**
 * The target DApp of issue-77.
 * 
 * Note that this entry-point shape is likely going to change, soon, but this allows us to start connecting the pieces
 * while we finish our ABI and deployment message design.
 */
public class BasicAppTestTarget {
    public static final byte kMethodIdentity = 1;
    public static final byte kMethodSum = 2;

    // NOTE:  Even though this is "byte[]" on the user's side, we will call it from the outside as "ByteArray"
    public static byte[] decode(BlockchainRuntime runtime, byte[] input) {
        byte instruction = input[0];
        byte[] output = null;
        switch (instruction) {
        case kMethodIdentity:
            output = identity(runtime, input);
            break;
        case kMethodSum:
            output = sum(runtime, input);
            break;
        default:
            throw new AssertionError("Unknown instruction");
        }
        return output;
    }

    private static byte[] identity(BlockchainRuntime runtime, byte[] input) {
        return input;
    }

    private static byte[] sum(BlockchainRuntime runtime, byte[] input) {
        byte total = 0;
        for (int i = 0; i < input.length; ++i) {
            total += input[i];
        }
        return new byte[] {total};
    }
}
