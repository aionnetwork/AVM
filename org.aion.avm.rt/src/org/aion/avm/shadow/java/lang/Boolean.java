package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


public class Boolean extends Object implements Comparable<Boolean> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final Boolean avm_TRUE = new Boolean(true);

    public static final Boolean avm_FALSE = new Boolean(false);

    public static final Class<Boolean> avm_TYPE = new Class(java.lang.Boolean.TYPE);

    public Boolean(boolean b) {
        this.v = b;
    }

    public Boolean(String s) {
        this(avm_parseBoolean(s));
    }

    public static boolean avm_parseBoolean(String s){
        return (s != null) && java.lang.Boolean.parseBoolean(s.getV());
    }

    public boolean avm_booleanValue() {
        return v;
    }

    public static Boolean avm_valueOf(boolean b) {
        return b ? avm_TRUE : avm_FALSE;
    }

    public static Boolean avm_valueOf(String s) {
        return avm_parseBoolean(s) ? avm_TRUE : avm_FALSE;
    }

    public static String avm_toString(boolean b) {
        return b ? (new String("true")) : (new String("false"));
    }

    public String avm_toString() {
        return v ? (new String("true")) : (new String("false"));
    }

    @Override
    public int avm_hashCode() {
        return Boolean.avm_hashCode(v);
    }

    public static int avm_hashCode(boolean value) {
        return value ? 1231 : 1237;
    }

    public boolean avm_equals(IObject obj) {
        if (obj instanceof Boolean) {
            return v == ((Boolean)obj).avm_booleanValue();
        }
        return false;
    }

    public int avm_compareTo(Boolean b) {
        return avm_compare(this.v, b.v);
    }

    public static int avm_compare(boolean x, boolean y) {
        return (x == y) ? 0 : (x ? 1 : -1);
    }

    public static boolean avm_logicalAnd(boolean a, boolean b) {
        return a && b;
    }

    public static boolean avm_logicalOr(boolean a, boolean b) {
        return a || b;
    }

    public static boolean avm_logicalXor(boolean a, boolean b) {
        return a ^ b;
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Boolean(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
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

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static boolean avm_getBoolean(java.lang.String name){}

}
