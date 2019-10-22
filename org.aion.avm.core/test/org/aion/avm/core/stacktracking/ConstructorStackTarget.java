package org.aion.avm.core.stacktracking;

public class ConstructorStackTarget {

    public static byte[] main() {
        constructBasicClass(2);
        return null;
    }

    public static class BasicClass {
        private final int integer;
        private final long longVal;
        private final Object object;

        public BasicClass(int i) {
            this(i, -4, "constructor-1");
        }

        public BasicClass(int i, long l) {
            this(i, l, "constructor-2");
        }

        public BasicClass(int i, long l, Object o) {
            this.integer = i;
            this.longVal = l;
            this.object = o;
        }
    }

    public static BasicClass constructBasicClass(int i) {
        BasicClass b1 = new BasicClass(i);
        String s = new String("t");
        BasicClass b2 = new BasicClass(i, i * i + i - (i + 1));
        Object o = new Object();
        return new BasicClass(i, b2.longVal, b1);
    }
}
