package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IHelper;


public abstract class Buffer<B extends java.nio.Buffer> extends org.aion.avm.shadow.java.lang.Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public final int avm_capacity() {
        return v.capacity();
    }

    public final int avm_position() {
        return v.position();
    }

    public Buffer<B> avm_position(int newPosition) {
        this.v = this.forCasting.cast(this.v.position(newPosition));
        return this;
    }

    public final int avm_limit() {
        return v.limit();
    }

    public Buffer<B> avm_limit(int newLimit) {
        this.v = this.forCasting.cast(this.v.limit(newLimit));
        return this;
    }

    public Buffer<B> avm_mark() {
        this.v = this.forCasting.cast(this.v.mark());
        return this;
    }

    public Buffer<B> avm_reset() {
        this.v = this.forCasting.cast(this.v.reset());
        return this;
    }

    public Buffer<B> avm_clear() {
        this.v = this.forCasting.cast(this.v.clear());
        return this;
    }

    public Buffer<B> avm_flip() {
        this.v = this.forCasting.cast(this.v.flip());
        return this;
    }

    public Buffer<B> avm_rewind() {
        this.v = this.forCasting.cast(this.v.rewind());
        return this;
    }

    public final int avm_remaining(){
        return v.remaining();
    }

    public final boolean avm_hasRemaining() {
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
        v = underlying;
        this.forCasting = forCasting;
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}
