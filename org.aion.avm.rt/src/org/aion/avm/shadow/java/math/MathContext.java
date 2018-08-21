package org.aion.avm.shadow.java.math;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadow.java.lang.Object;
import org.aion.avm.shadow.java.lang.String;


public final class MathContext extends Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

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
    }

    public MathContext(int setPrecision,
                       RoundingMode setRoundingMode) {
        v = new java.math.MathContext(setPrecision, setRoundingMode.getUnderlying());
    }

    public MathContext(String val) {
        v = new java.math.MathContext(val.getUnderlying());
    }

    public int avm_getPrecision() {
        return this.v.getPrecision();
    }

    public RoundingMode avm_getRoundingMode() {
        return RoundingMode.avm_valueOf(new String(this.v.getRoundingMode().name()));
    }

    public boolean avm_equals(IObject x){
        MathContext mc;
        if (!(x instanceof MathContext))
            return false;
        mc = (MathContext) x;
        return mc.v.equals(v);
    }

    public int avm_hashCode() {
        RoundingMode roundingMode = RoundingMode.avm_valueOf(new String(this.v.getRoundingMode().name()));
        return this.v.getPrecision() + roundingMode.hashCode() * 59;
    }

    public String avm_toString() {
        return new String(v.toString());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.math.MathContext v;

    public java.math.MathContext getUnderlying() {
        return v;
    }

    // Deserializer support.
    public MathContext(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
        lazyLoad();
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //private void readObject(java.io.ObjectInputStream s)

}
