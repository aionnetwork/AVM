package org.aion.avm.tooling;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionMap;


/**
 * The target DApp of issue-77.
 * 
 * Note that this entry-point shape is likely going to change, soon, but this allows us to start connecting the pieces
 * while we finish our ABI and deployment message design.
 */
public class BasicAppTestTarget {
    // NOTE:  This use of a static is something we will definitely be changing, later on, once we decide how static and instance state interacts with storage.
    private static int swappingPoint;
    private static AionMap<Byte, Byte> longLivedMap = new AionMap<>();

    @Callable
    public static byte[] identity(byte[] input) {
        return input;
    }

    @Callable
    public static int sum(byte[] input) {
        int total = 0;
        for (int i = 0; i < input.length; ++i) {
            total += input[i];
        }
        return total;
    }

    @Callable
    public static int swapInputs(int input) {
        int result = swappingPoint;
        swappingPoint = input;
        return result;
    }

    @Callable
    public static boolean arrayEquality(byte[] input) {
        byte[] target = new byte[] {5, 6, 7, 8};
        return target.equals(input);
    }

    @Callable
    public static int allocateObjectArray() {
        // We just want to create the array.
        Object[] array = new Object[] {"test", "two"};
        return array.length;
    }

    @Callable
    public static byte[] byteAutoboxing(byte input) {
        // Take the second byte, auto-boxed, and use that information to build the response.
        Byte value = input;
        return new byte[] { (byte)value.hashCode(), value.byteValue() };
    }

    @Callable
    public static byte mapPut(byte key, byte value) {
        longLivedMap.put(key, value);
        return value;
    }

    @Callable
    public static byte mapGet(byte key) {
        byte value = longLivedMap.get(key);
        return value;
    }
}
