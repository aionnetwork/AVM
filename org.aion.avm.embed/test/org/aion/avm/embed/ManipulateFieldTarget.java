package org.aion.avm.embed;

import org.aion.avm.tooling.abi.Callable;

/**
 * An easy target for the DappManipulator class to try and manipulate its byte array field.
 */
public class ManipulateFieldTarget {
    private static byte[] bytes = new byte[20];

    @Callable
    public static byte[] getField() {
        return bytes;
    }
}
