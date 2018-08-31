package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;

import org.aion.avm.RuntimeMethodFeeSchedule;

public abstract class Buffer<B extends java.nio.Buffer> extends org.aion.avm.shadow.java.lang.Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public final int avm_capacity() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_capacity);
        lazyLoad();
        return v.capacity();
    }

    public final int avm_position() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_position);
        lazyLoad();
        return v.position();
    }

    public Buffer<B> avm_position(int newPosition) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_position_1);
        lazyLoad();
        this.v = this.forCasting.cast(this.v.position(newPosition));
        return this;
    }

    public final int avm_limit() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_limit);
        lazyLoad();
        return v.limit();
    }

    public Buffer<B> avm_limit(int newLimit) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_limit_1);
        lazyLoad();
        this.v = this.forCasting.cast(this.v.limit(newLimit));
        return this;
    }

    public Buffer<B> avm_mark() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_mark);
        lazyLoad();
        this.v = this.forCasting.cast(this.v.mark());
        return this;
    }

    public Buffer<B> avm_reset() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_reset);
        lazyLoad();
        this.v = this.forCasting.cast(this.v.reset());
        return this;
    }

    public Buffer<B> avm_clear() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_clear);
        lazyLoad();
        this.v = this.forCasting.cast(this.v.clear());
        return this;
    }

    public Buffer<B> avm_flip() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_flip);
        lazyLoad();
        this.v = this.forCasting.cast(this.v.flip());
        return this;
    }

    public Buffer<B> avm_rewind() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_rewind);
        lazyLoad();
        this.v = this.forCasting.cast(this.v.rewind());
        return this;
    }

    public final int avm_remaining(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_remaining);
        lazyLoad();
        return v.remaining();
    }

    public final boolean avm_hasRemaining() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_hasRemaining);
        lazyLoad();
        return v.hasRemaining();
    }

    public abstract boolean avm_isReadOnly();

    public abstract boolean avm_hasArray();

    public abstract org.aion.avm.shadow.java.lang.Object avm_array();

    public abstract int avm_arrayOffset();

    public abstract boolean avm_isDirect();

    public abstract Buffer<B> avm_slice();

    public abstract Buffer<B> avm_duplicate();

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    B v;
    // Note that this "forCasting" variable is just passed up so we can verify the types we operate in the base class are consistent with those
    // seen in the sub-class.
    Class<B> forCasting;

    protected Buffer(Class<B> forCasting, B underlying){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Buffer_avm_constructor);
        v = underlying;
        this.forCasting = forCasting;
    }

    // Deserializer support.
    public Buffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    // We need to act like a real implementation since our field is actually serialized by our subclasses.
    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(Buffer.class, deserializer);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(Buffer.class, serializer);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}
