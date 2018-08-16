package org.aion.avm.core;


/**
 * issue-156:  Target of NonStaticInnerClassTest.
 */
public class NonStaticInnerClassTarget {
    public class Inner {
        private long foo;
        public Inner(Inner another) {
            if (null != another) {
                another.foo = 42L;
            }
        }
        public class Deeper {
            public long readParent() {
                return foo;
            }
        }
    }
}
