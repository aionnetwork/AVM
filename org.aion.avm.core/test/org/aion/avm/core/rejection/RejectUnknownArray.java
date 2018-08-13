package org.aion.avm.core.rejection;

import java.util.HashSet;


public class RejectUnknownArray {
    // "clone()" is special in that it is actually owned by the array type, not strictly inherited from Object.
    public static HashSet<?>[] rejectedClone(HashSet<?>[] input) {
        return input.clone();
    }
}
