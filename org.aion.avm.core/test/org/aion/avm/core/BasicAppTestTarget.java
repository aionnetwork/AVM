package org.aion.avm.core;

import org.aion.avm.rt.BlockchainRuntime;


/**
 * The target DApp of issue-77.
 * 
 * Note that this entry-point shape is likely going to change, soon, but this allows us to start connecting the pieces
 * while we finish our ABI and deployment message design.
 */
public class BasicAppTestTarget {
    // NOTE:  Even though this is "byte[]" on the user's side, we will call it from the outside as "ByteArray"
    public static byte[] decode(BlockchainRuntime runtime, byte[] input) {
        return input;
    }
}
