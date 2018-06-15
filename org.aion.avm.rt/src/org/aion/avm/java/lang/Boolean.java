package org.aion.avm.java.lang;

public class Boolean extends Object {

    public static final Boolean avm_TRUE = new Boolean(true);

    public static final Boolean avm_FALSE = new Boolean(false);

    private boolean b;

    public Boolean(boolean b) {
        this.b = b;
    }

    public static Boolean avm_valueOf(boolean b) {
        return b ? avm_TRUE : avm_FALSE;
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof Boolean && this.b == ((Boolean) obj).b;
    }

    @Override
    public java.lang.String toString() {
        return java.lang.Boolean.toString(this.b);
    }
}
