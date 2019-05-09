package org.aion.avm.core;

import avm.Address;
import avm.Result;

public class ApiSubclassingTarget {

    private static class SubAddress extends Address {
        public SubAddress() {
            super(null);
        }
    }

    private static class SubResult extends Result {
        public SubResult() {
            super(true, null);
        }
    }

    public static byte[] main() {
        return null;
    }
}
