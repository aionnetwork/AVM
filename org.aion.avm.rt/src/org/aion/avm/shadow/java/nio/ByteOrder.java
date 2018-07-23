package org.aion.avm.shadow.java.nio;


public final class ByteOrder {


    public static final ByteOrder avm_BIG_ENDIAN
            = new ByteOrder(java.nio.ByteOrder.BIG_ENDIAN);

    public static final ByteOrder avm_LITTLE_ENDIAN
            = new ByteOrder(java.nio.ByteOrder.LITTLE_ENDIAN);

    public static ByteOrder avm_nativeOrder() {
        return new ByteOrder(java.nio.ByteOrder.nativeOrder());
    }

    public org.aion.avm.shadow.java.lang.String avm_toString() {
        return new org.aion.avm.shadow.java.lang.String(v.toString());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.nio.ByteOrder v;

    ByteOrder(java.nio.ByteOrder underlying) {
        this.v = underlying;
    }

    public java.nio.ByteOrder getV() {
        return v;
    }
}
