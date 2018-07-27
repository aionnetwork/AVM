package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IHelper;

public abstract class Buffer extends org.aion.avm.shadow.java.lang.Object {

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

    public Buffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final int avm_limit() {
        return v.limit();
    }

    public Buffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public Buffer avm_mark() {
        v = v.mark();
        return this;
    }

    public Buffer avm_reset() {
        v = v.reset();
        return this;
    }

    public Buffer avm_clear() {
        v = v.clear();
        return this;
    }

    public Buffer avm_flip() {
        v = v.flip();
        return this;
    }

    public Buffer avm_rewind() {
        v = v.rewind();
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

    public abstract Buffer avm_slice();

    public abstract Buffer avm_duplicate();

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    java.nio.Buffer v;

    Buffer(java.nio.Buffer underlying){
        v = underlying;
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}
