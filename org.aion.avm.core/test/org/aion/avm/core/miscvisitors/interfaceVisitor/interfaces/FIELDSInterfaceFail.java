package org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces;

public interface FIELDSInterfaceFail {
    String outer_str = "outer";

    class FIELDS {
        public static String inner_string = "inner";

        private int f() {
            return 1000;
        }
    }
}
