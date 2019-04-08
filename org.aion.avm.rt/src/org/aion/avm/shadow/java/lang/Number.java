package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.RuntimeMethodFeeSchedule;

public abstract class Number extends Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public Number(java.lang.Void ignore, int readIndex) {
        super(ignore, readIndex);
    }

    public Number(){};

    public abstract int avm_intValue();

    public abstract long avm_longValue();

    public abstract float avm_floatValue();

    public abstract double avm_doubleValue();

    public byte avm_byteValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Number_avm_byteValue);
        return (byte)avm_intValue();
    }

    public short avm_shortValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Number_avm_shortValue);
        return (short)avm_intValue();
    }
}
