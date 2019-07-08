package org.aion.avm.embed.benchmark;

import org.aion.avm.tooling.abi.Callable;


/**
 * Tests the cost of relatively large graph storage.
 */
public class StorageLargeDataContract {
    private static Object[] objectArray;

    @Callable
    public static void writeToObjectArray(int objectArraySize, int intArraySize) {
        objectArray = new Object[objectArraySize];
        for (int i = 0; i < objectArray.length; i++) {
            objectArray[i] = new int[intArraySize];
        }
    }
}
