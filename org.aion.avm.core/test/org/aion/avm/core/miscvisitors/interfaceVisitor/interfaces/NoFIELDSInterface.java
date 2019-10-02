package org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces;

import org.aion.avm.core.miscvisitors.interfaceVisitor.SampleObj;

public interface NoFIELDSInterface {
    SampleObj obj = new SampleObj();
    String outer_string = obj.toString();

    class FIELDS2 {
        public static int i = 1;

        public static class FIELDS {
            public static int i = 4;
        }
    }

    class FIELDSA {
        public static int i = 2;
    }

    class FIELDS__A {
        public static int i = 3;
    }
}
