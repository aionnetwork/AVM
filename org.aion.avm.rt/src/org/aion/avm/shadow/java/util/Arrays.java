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

    public static ByteArray avm_copyOfRange(ByteArray a, int start, int end) {
        return new ByteArray(java.util.Arrays.copyOfRange(a.getUnderlying(), start, end));
    }

    public static void avm_fill(ByteArray a, int fromIndex, int toIndex, byte val) {
        java.util.Arrays.fill(a.getUnderlying(), fromIndex, toIndex, val);
    }
}
