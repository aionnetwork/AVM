package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class Boolean extends Object implements Comparable<Boolean> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final Boolean avm_TRUE = new Boolean(true);

    public static final Boolean avm_FALSE = new Boolean(false);

    public static final Class<Boolean> avm_TYPE = new Class(java.lang.Boolean.TYPE);

    public Boolean(boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_constructor);
        this.v = b;
    }

    public Boolean(String s) {
        this(avm_parseBoolean(s));
    }

    public static boolean avm_parseBoolean(String s){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_parseBoolean);
        return (s != null) && java.lang.Boolean.parseBoolean(s.getUnderlying());
    }

    public boolean avm_booleanValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_booleanValue);
        return v;
    }

    public static Boolean avm_valueOf(boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_valueOf);
        return b ? avm_TRUE : avm_FALSE;
    }

    public static Boolean avm_valueOf(String s) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_valueOf_1);
        return avm_parseBoolean(s) ? avm_TRUE : avm_FALSE;
    }

    public static String avm_toString(boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_toString);
        return b ? (new String("true")) : (new String("false"));
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_toString_1);
        return v ? (new String("true")) : (new String("false"));
    }

    @Override
    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_hashCode);
        return Boolean.avm_hashCode(v);
    }

    public static int avm_hashCode(boolean value) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_hashCode_1);
        return value ? 1231 : 1237;
    }

    public boolean avm_equals(IObject obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_equals);
        if (obj instanceof Boolean) {
            return v == ((Boolean)obj).avm_booleanValue();
        }
        return false;
    }

    public int avm_compareTo(Boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_compareTo);
        return avm_compare(this.v, b.v);
    }

    public static int avm_compare(boolean x, boolean y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_compare);
        return (x == y) ? 0 : (x ? 1 : -1);
    }

    public static boolean avm_logicalAnd(boolean a, boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_logicalAnd);
        return a && b;
    }

    public static boolean avm_logicalOr(boolean a, boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_logicalOr);
        return a || b;
    }

    public static boolean avm_logicalXor(boolean a, boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Boolean_avm_logicalXor);
        return a ^ b;
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Boolean(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
        lazyLoad();
    }

    private boolean v;

    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof Boolean && this.v == ((Boolean) obj).v;
    }

    @Override
    public java.lang.String toString() {
        return java.lang.Boolean.toString(this.v);
    }

    public boolean getValue() {
        return v;
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static boolean avm_getBoolean(java.lang.String name){}

}
