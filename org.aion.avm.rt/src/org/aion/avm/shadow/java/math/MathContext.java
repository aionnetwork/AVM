package org.aion.avm.shadow.java.math;

import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Object;
import org.aion.avm.shadow.java.lang.String;


public final class MathContext extends Object {

    public static final MathContext avm_UNLIMITED =
            new MathContext(0, RoundingMode.avm_HALF_UP);

    public static final MathContext avm_DECIMAL32 =
            new MathContext(7, RoundingMode.avm_HALF_EVEN);

    public static final MathContext avm_DECIMAL64 =
            new MathContext(16, RoundingMode.avm_HALF_EVEN);

    public static final MathContext avm_DECIMAL128 =
            new MathContext(34, RoundingMode.avm_HALF_EVEN);

    public MathContext(int setPrecision) {
        v = new java.math.MathContext(setPrecision);
        precision = v.getPrecision();
        roundingMode = RoundingMode.avm_valueOf(new String("avm_" + v.getRoundingMode().name()));
        return;
    }

    public MathContext(int setPrecision,
                       RoundingMode setRoundingMode) {
        v = new java.math.MathContext(setPrecision, setRoundingMode.getV());
        precision = v.getPrecision();
        roundingMode = RoundingMode.avm_valueOf(new String("avm_" + v.getRoundingMode().name()));
        return;
    }

    public MathContext(String val) {
        v = new java.math.MathContext(val.getUnderlying());
        precision = v.getPrecision();
        roundingMode = RoundingMode.avm_valueOf(new String("avm_" + v.getRoundingMode().name()));
        return;
    }

    public int avm_getPrecision() {
        return precision;
    }

    public RoundingMode avm_getRoundingMode() {
        return roundingMode;
    }

    public boolean avm_equals(IObject x){
        MathContext mc;
        if (!(x instanceof MathContext))
            return false;
        mc = (MathContext) x;
        return mc.v.equals(v);
    }

    public int avm_hashCode() {
        return precision + roundingMode.hashCode() * 59;
    }

    public String avm_toString() {
        return new String(v.toString());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.math.MathContext v;

    final int precision;

    final RoundingMode roundingMode;

    public java.math.MathContext getV() {
        return v;
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //private void readObject(java.io.ObjectInputStream s)

}
