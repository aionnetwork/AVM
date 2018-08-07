package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


public final class Byte extends Object implements Comparable<Byte> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final byte avm_MIN_VALUE = java.lang.Byte.MIN_VALUE;

    public static final byte avm_MAX_VALUE = java.lang.Byte.MAX_VALUE;

    public static final Class<Byte> avm_TYPE = new Class(java.lang.Byte.TYPE);;

    public static String avm_toString(byte b) {
        return Integer.avm_toString((int)b, 10);
    }

    public static Byte avm_valueOf(byte b) {
        return new Byte(b);
    }

    public static byte avm_parseByte(String s, int radix){
        return java.lang.Byte.parseByte(s.getUnderlying(), radix);
    }

    public static byte avm_parseByte(String s) throws NumberFormatException {
        return avm_parseByte(s, 10);
    }

    public static Byte avm_valueOf(String s, int radix)
            throws NumberFormatException {
        return avm_valueOf(avm_parseByte(s, radix));
    }

    public static Byte avm_valueOf(String s) throws NumberFormatException {
        return avm_valueOf(s, 10);
    }

    public static Byte avm_decode(String nm) throws NumberFormatException {
        return new Byte(java.lang.Byte.decode(nm.getUnderlying()).byteValue());
    }

    public Byte(byte v) {
        this.v = v;
    }

    public Byte(String s) throws NumberFormatException {
        this.v = avm_parseByte(s, 10);
    }

    public byte avm_byteValue() {
        return v;
    }

    public short avm_shortValue() {
        return (short) v;
    }

    public int avm_intValue() {
        return (int) v;
    }

    public long avm_longValue() {
        return (long) v;
    }

    public float avm_floatValue() {
        return (float) v;
    }

    public double avm_doubleValue() {
        return (double) v;
    }

    public String avm_toString() {
        return Integer.avm_toString((int) v);
    }

    @Override
    public int avm_hashCode() {
        return Byte.avm_hashCode(v);
    }

    public static int avm_hashCode(byte value) {
        return (int)value;
    }

    public boolean avm_equals(IObject obj) {
        if (obj instanceof Byte) {
            return v == ((Byte)obj).avm_byteValue();
        }
        return false;
    }

    public int avm_compareTo(Byte anotherByte) {
        return avm_compare(this.v, anotherByte.v);
    }

    public static int avm_compare(byte x, byte y) {
        return x - y;
    }

    public static int avm_compareUnsigned(byte x, byte y) {
        return Byte.avm_toUnsignedInt(x) - Byte.avm_toUnsignedInt(y);
    }

    public static int avm_toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    public static long avm_toUnsignedLong(byte x) {
        return ((long) x) & 0xffL;
    }

    public static final int avm_SIZE = java.lang.Byte.SIZE;

    public static final int avm_BYTES = java.lang.Byte.BYTES;

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Byte(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
        lazyLoad();
    }

    private byte v;

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}
