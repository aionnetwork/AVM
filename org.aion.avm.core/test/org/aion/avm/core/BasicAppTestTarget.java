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
    public static final byte kMethodLowOrderByteArrayHash = 3;
    public static final byte kMethodLowOrderRuntimeHash = 4;
    public static final byte kMethodSwapInputsFromLastCall = 5;
    public static final byte kMethodTestArrayEquality = 6;

    // NOTE:  This use of a static is something we will definitely be changing, later on, once we decide how static and instance state interacts with storage.
    private static byte[] swappingPoint;

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
        case kMethodLowOrderByteArrayHash:
            output = lowOrderByteArrayHash(runtime, input);
            break;
        case kMethodLowOrderRuntimeHash:
            output = lowOrderRuntimeHash(runtime, input);
            break;
        case kMethodSwapInputsFromLastCall:
            output = swapInputs(runtime, input);
            break;
        case kMethodTestArrayEquality:
            output = arrayEquality(runtime, input);
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

    private static byte[] lowOrderByteArrayHash(BlockchainRuntime runtime, byte[] input) {
        return new byte[] {(byte)(0xff & input.hashCode())};
    }

    private static byte[] lowOrderRuntimeHash(BlockchainRuntime runtime, byte[] input) {
        return new byte[] {(byte)(0xff & runtime.hashCode())};
    }

    private static byte[] swapInputs(BlockchainRuntime runtime, byte[] input) {
        byte[] result = swappingPoint;
        swappingPoint = input;
        return result;
    }

    private static byte[] arrayEquality(BlockchainRuntime runtime, byte[] input) {
        byte[] target = new byte[] {5, 6, 7, 8};
        boolean isEqual = target.equals(input);
        byte result = (byte)(isEqual ? 1 : 0);
        return new byte[] { result };
    }
}
