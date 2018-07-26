package org.aion.avm.shadow.java.util;

import org.aion.avm.arraywrapper.ByteArray;

public class Arrays {

    public static int avn_hashCode(ByteArray a) {
        if (a == null) {
            return 0;
        } else {
            return java.util.Arrays.hashCode(a.getUnderlying());
        }
    }

    public static boolean avm_equals(ByteArray a, ByteArray a2) {
        if (a == a2) {
            return true;
        }

        if (a == null || a2 == null) {
            return false;
        }

        return java.util.Arrays.equals(a.getUnderlying(), a2.getUnderlying());
    }
}
