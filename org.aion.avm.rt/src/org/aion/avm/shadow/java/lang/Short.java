package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


public class Short extends Number {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final short avm_MIN_VALUE = java.lang.Short.MIN_VALUE;

    public static final short avm_MAX_VALUE = java.lang.Short.MAX_VALUE;

    public static final Class<Short> avm_TYPE = new Class(java.lang.Short.TYPE);

    public static String avm_toString(short s) {
        return Integer.avm_toString((int)s, 10);
    }

    public static short avm_parseShort(String s, int radix) throws NumberFormatException {
        return java.lang.Short.parseShort(s.getV(), radix);
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
        return new Short(java.lang.Short.decode(nm.getV()).shortValue());
    }

    public Short(short v) {
        this.v = v;
    }

    public Short(String s) throws NumberFormatException {
        this.v = avm_parseShort(s, 10);
    }

    public byte avm_byteValue() {
        return (byte) v;
    }

    public short avm_shortValue() {
        return v;
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

    public int avm_hashCode() {
        return Short.avm_hashCode(v);
    }

    public static int avm_hashCode(short value) {
        return (int)value;
    }

    public boolean avm_equals(IObject obj) {
        if (obj instanceof Short) {
            return v == ((Short)obj).avm_shortValue();
        }
        return false;
    }

    public int avm_compareTo(Short anotherShort) {
        return avm_compare(this.v, anotherShort.v);
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

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Short(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    private short v;

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}
