package org.aion.avm.core;

import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.userlib.AionMap;


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
    public static final byte kMethodSwapInputsFromLastCall = 5;
    public static final byte kMethodTestArrayEquality = 6;
    public static final byte kMethodAllocateObjectArray = 7;
    public static final byte kMethodByteAutoboxing = 8;
    public static final byte kMethodMapPut = 9;
    public static final byte kMethodMapGet = 10;

    // NOTE:  This use of a static is something we will definitely be changing, later on, once we decide how static and instance state interacts with storage.
    private static byte[] swappingPoint;
    private static AionMap<Byte, Byte> longLivedMap = new AionMap<>();

    // NOTE:  Even though this is "byte[]" on the user's side, we will call it from the outside as "ByteArray"
    public static byte[] decode(byte[] input) {
        byte instruction = input[0];
        byte[] output = null;
        switch (instruction) {
        case kMethodIdentity:
            output = identity(input);
            break;
        case kMethodSum:
            output = sum(input);
            break;
        case kMethodLowOrderByteArrayHash:
            output = lowOrderByteArrayHash(input);
            break;
        case kMethodSwapInputsFromLastCall:
            output = swapInputs(input);
            break;
        case kMethodTestArrayEquality:
            output = arrayEquality(input);
            break;
        case kMethodAllocateObjectArray:
            output = allocateObjectArray(input);
            break;
        case kMethodByteAutoboxing:
            output = byteAutoboxing(input);
            break;
        case kMethodMapPut:
            output = mapPut(input);
            break;
        case kMethodMapGet:
            output = mapGet(input);
            break;
        default:
            throw new AssertionError("Unknown instruction");
        }
        return output;
    }

    private static byte[] identity(byte[] input) {
        return input;
    }

    private static byte[] sum(byte[] input) {
        byte total = 0;
        for (int i = 0; i < input.length; ++i) {
            total += input[i];
        }
        return new byte[] {total};
    }

    private static byte[] lowOrderByteArrayHash(byte[] input) {
        return new byte[] {(byte)(0xff & input.hashCode())};
    }

    private static byte[] swapInputs(byte[] input) {
        byte[] result = swappingPoint;
        swappingPoint = input;
        return result;
    }

    private static byte[] arrayEquality(byte[] input) {
        byte[] target = new byte[] {5, 6, 7, 8};
        boolean isEqual = target.equals(input);
        byte result = (byte)(isEqual ? 1 : 0);
        return new byte[] { result };
    }

    private static byte[] allocateObjectArray(byte[] input) {
        // We just want to create the array.
        Object[] array = new Object[] {"test", "two"};
        return new byte[] { (byte)array.length };
    }

    private static byte[] byteAutoboxing(byte[] input) {
        // Take the second byte, auto-boxed, and use that information to build the response.
        Byte value = input[1];
        return new byte[] { (byte)value.hashCode(), value.byteValue() };
    }

    private static byte[] mapPut(byte[] input) {
        byte key = input[1];
        byte value = input[2];
        longLivedMap.put(key, value);
        return new byte[] {value};
    }

    private static byte[] mapGet(byte[] input) {
        byte key = input[1];
        byte value = longLivedMap.get(key);
        return new byte[] {value};
    }
}
