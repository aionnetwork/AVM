package org.aion.avm.java.lang;

import org.aion.avm.internal.IObject;

public class Short extends Number{

    public static final short avm_MIN_VALUE = java.lang.Short.MIN_VALUE;

    public static final short avm_MAX_VALUE = java.lang.Short.MAX_VALUE;

    // TODO
    public static final Class<Short> avm_TYPE = new Class(java.lang.Short.TYPE);

    public static String avm_toString(short s) {
        return Integer.avm_toString((int)s, 10);
    }

    public static short avm_parseShort(String s, int radix) throws NumberFormatException {
        return java.lang.Short.parseShort(s.getUnderlying(), radix);
    }

    public static short avm_parseShort(String s) throws NumberFormatException {
        return avm_parseShort(s, 10);
    }

    public static Short avm_valueOf(String s, int radix) throws NumberFormatException {
        return avm_valueOf(avm_parseShort(s, radix));
    }

    public static Short avm_valueOf(String s) throws NumberFormatException {
        return avm_valueOf(s, 10);
    }

    public static Short avm_valueOf(short s) {
        return new Short(s);
    }

    public static Short avm_decode(String nm) throws NumberFormatException {
        return new Short(java.lang.Short.decode(nm.getUnderlying()).shortValue());
    }

    private final short value;

    public Short(short value) {
        this.value = value;
    }

    public Short(String s) throws NumberFormatException {
        this.value = avm_parseShort(s, 10);
    }

    public byte avm_byteValue() {
        return (byte)value;
    }

    public short avm_shortValue() {
        return value;
    }

    public int avm_intValue() {
        return (int)value;
    }

    public long avm_longValue() {
        return (long)value;
    }

    public float avm_floatValue() {
        return (float)value;
    }

    public double avm_doubleValue() {
        return (double)value;
    }

    public String avm_toString() {
        return Integer.avm_toString((int)value);
    }

    public int avm_hashCode() {
        return Short.avm_hashCode(value);
    }

    public static int avm_hashCode(short value) {
        return (int)value;
    }

    public boolean avm_equals(IObject obj) {
        if (obj instanceof Short) {
            return value == ((Short)obj).avm_shortValue();
        }
        return false;
    }

    public int avm_compareTo(Short anotherShort) {
        return avm_compare(this.value, anotherShort.value);
    }

    public static int avm_compare(short x, short y) {
        return x - y;
    }

    public static int avm_compareUnsigned(short x, short y) {
        return avm_toUnsignedInt(x) - avm_toUnsignedInt(y);
    }

    public static final int avm_SIZE = java.lang.Short.SIZE;

    public static final int avm_BYTES = java.lang.Short.BYTES;

    public static short avm_reverseBytes(short i){
        return java.lang.Short.reverseBytes(i);
    }

    public static int avm_toUnsignedInt(short x) {
        return ((int) x) & 0xffff;
    }

    public static long avm_toUnsignedLong(short x) {
        return ((long) x) & 0xffffL;
    }
}
