package org.aion.avm.java.lang;

public class Integer extends Number {

    public static final int avm_MAX_VALUE = java.lang.Integer.MAX_VALUE;
    public static final int avm_MIN_VALUE = java.lang.Integer.MIN_VALUE;
    public static final int avm_SIZE = java.lang.Integer.SIZE;
    public static final Class<Integer> avm_TYPE = null; // TODO:

    private int i;

    public Integer(int underlying) {
        this.i = underlying;
    }

    public static Integer avm_valueOf(int i) {
        return new Integer(i);
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof Integer && this.i == ((Integer) obj).i;
    }

    @Override
    public java.lang.String toString() {
        return java.lang.Integer.toString(this.i);
    }
}
