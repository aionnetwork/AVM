package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;

import org.aion.avm.RuntimeMethodFeeSchedule;

public abstract class Number extends Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public Number(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
        lazyLoad();
    }

    public Number(){};

    public abstract int avm_intValue();

    public abstract long avm_longValue();

    public abstract float avm_floatValue();

    public abstract double avm_doubleValue();

    public byte avm_byteValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Number_avm_byteValue);
        return (byte)avm_intValue();
    }

    public short avm_shortValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Number_avm_shortValue);
        return (short)avm_intValue();
    }
}
