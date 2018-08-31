package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class Short extends Number {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final short avm_MIN_VALUE = java.lang.Short.MIN_VALUE;

    public static final short avm_MAX_VALUE = java.lang.Short.MAX_VALUE;

    public static final Class<Short> avm_TYPE = new Class(java.lang.Short.TYPE);

    public static String avm_toString(short s) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_toString);
        return Integer.avm_toString((int)s, 10);
    }

    public static short avm_parseShort(String s, int radix) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_parseShort);
        return java.lang.Short.parseShort(s.getUnderlying(), radix);
    }

    public static short avm_parseShort(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_parseShort_1);
        return avm_parseShort(s, 10);
    }

    public static Short avm_valueOf(String s, int radix) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_valueOf);
        return avm_valueOf(avm_parseShort(s, radix));
    }

    public static Short avm_valueOf(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_valueOf_1);
        return avm_valueOf(s, 10);
    }

    public static Short avm_valueOf(short s) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_valueOf_2);
        return new Short(s);
    }

    public static Short avm_decode(String nm) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_decode);
        return new Short(java.lang.Short.decode(nm.getUnderlying()).shortValue());
    }

    public Short(short v) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_constructor);
        this.v = v;
    }

    public Short(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_constructor_1);
        this.v = avm_parseShort(s, 10);
    }

    public byte avm_byteValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_byteValue);
        return (byte) v;
    }

    public short avm_shortValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_shortValue);
        return v;
    }

    public int avm_intValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_intValue);
        return (int) v;
    }

    public long avm_longValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_longValue);
        return (long) v;
    }

    public float avm_floatValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_floatValue);
        return (float) v;
    }

    public double avm_doubleValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_doubleValue);
        return (double) v;
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_toString_1);
        return Integer.avm_toString((int) v);
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_hashCode);
        return Short.avm_hashCode(v);
    }

    public static int avm_hashCode(short value) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_hashCode_1);
        return (int)value;
    }

    public boolean avm_equals(IObject obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_equals);
        if (obj instanceof Short) {
            return v == ((Short)obj).avm_shortValue();
        }
        return false;
    }

    public int avm_compareTo(Short anotherShort) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_compareTo);
        return avm_compare(this.v, anotherShort.v);
    }

    public static int avm_compare(short x, short y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_compare);
        return x - y;
    }

    public static int avm_compareUnsigned(short x, short y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_compareUnsigned);
        return avm_toUnsignedInt(x) - avm_toUnsignedInt(y);
    }

    public static final int avm_SIZE = java.lang.Short.SIZE;

    public static final int avm_BYTES = java.lang.Short.BYTES;

    public static short avm_reverseBytes(short i){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_reverseBytes);
        return java.lang.Short.reverseBytes(i);
    }

    public static int avm_toUnsignedInt(short x) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_toUnsignedInt);
        return ((int) x) & 0xffff;
    }

    public static long avm_toUnsignedLong(short x) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Short_avm_toUnsignedLong);
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
