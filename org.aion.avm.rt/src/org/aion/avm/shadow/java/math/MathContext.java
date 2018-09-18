package org.aion.avm.shadow.java.math;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.Object;
import org.aion.avm.shadow.java.lang.String;

import org.aion.avm.RuntimeMethodFeeSchedule;

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
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_constructor);
        v = new java.math.MathContext(setPrecision);
    }

    public MathContext(int setPrecision,
                       RoundingMode setRoundingMode) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_constructor_1);
        v = new java.math.MathContext(setPrecision, setRoundingMode.getUnderlying());
    }

    public MathContext(String val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_constructor_2);
        v = new java.math.MathContext(val.getUnderlying());
    }

    public int avm_getPrecision() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_getPrecision);
        return this.v.getPrecision();
    }

    public RoundingMode avm_getRoundingMode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_getRoundingMode);
        return RoundingMode.avm_valueOf(new String(this.v.getRoundingMode().name()));
    }

    public boolean avm_equals(IObject x){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_equals);
        MathContext mc;
        if (!(x instanceof MathContext))
            return false;
        mc = (MathContext) x;
        return mc.v.equals(v);
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_hashCode);
        RoundingMode roundingMode = RoundingMode.avm_valueOf(new String(this.v.getRoundingMode().name()));
        return this.v.getPrecision() + roundingMode.hashCode() * 59;
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.MathContext_avm_toString);
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
    public MathContext(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
        lazyLoad();
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(MathContext.class, deserializer);
        
        // We store this as the precision (int) and the RoundingMode (stub).
        int precision = deserializer.readInt();
        RoundingMode mode = (RoundingMode)deserializer.readStub();
        this.v = new java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.avm_name().getUnderlying()));
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(String.class, serializer);
        
        // We store this as the precision (int) and the RoundingMode (stub).
        serializer.writeInt(this.v.getPrecision());
        RoundingMode roundingMode = RoundingMode.avm_valueOf(new String(this.v.getRoundingMode().name()));
        serializer.writeStub(roundingMode);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //private void readObject(java.io.ObjectInputStream s)

}
