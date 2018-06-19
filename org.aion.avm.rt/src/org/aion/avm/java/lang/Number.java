package org.aion.avm.java.lang;

public abstract class Number extends Object {
    public abstract int avm_intValue();

    public abstract long avm_longValue();

    public abstract float avm_floatValue();

    public abstract double avm_doubleValue();

    public byte avm_byteValue() {
        return (byte)avm_intValue();
    }

    public short avm_shortValue() {
        return (short)avm_intValue();
    }
}
