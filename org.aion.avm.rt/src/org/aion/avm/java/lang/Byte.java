package org.aion.avm.java.lang;

import org.aion.avm.internal.IObject;


public final class Byte extends Object {
    public static final int BYTES = 1;

    public static final Class<Byte> TYPE = null;

    private final byte value;

    public Byte(byte value) {
        this.value = value;
    }

    public static Byte avm_valueOf(byte value) {
        return new Byte(value);
    }

    @Override
    public int avm_hashCode() {
        return (int)this.value;
    }

    @Override
    public boolean avm_equals(IObject obj) {
        boolean isEqual = (this == obj);
        // Note that we want to make sure we are both the same sub-class, since equality is defined per-class.
        if (!isEqual
                && (null != obj)
                && (this.getClass() == obj.getClass())
        ) {
            Byte other = (Byte) obj;
            isEqual = (this.value == other.value);
        }
        return isEqual;
    }

    public byte avm_byteValue() {
        return this.value;
    }
}
