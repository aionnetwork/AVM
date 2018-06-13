package org.aion.avm.core.rejection;

import java.util.Set;


public class RejectUnknownArray {
    // "clone()" is special in that it is actually owned by the array type, not strictly inherited from Object.
    public static Set<?>[] rejectedClone(Set<?>[] input) {
        return input.clone();
    }
}
