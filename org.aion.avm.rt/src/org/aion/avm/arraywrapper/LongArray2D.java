package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;

public class LongArray2D extends ObjectArray {

    public static LongArray2D initArray(int d0, int d1){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        LongArray2D ret = new LongArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, LongArray.initArray(d1));
        }
        return ret;
    }

    public LongArray2D(int c) {
        super(c);
    }

    public LongArray2D() {
        super();
    }

    // Deserializer support.
    public LongArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public IObject avm_clone() {
        lazyLoad();
        return new LongArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        lazyLoad();
        return new LongArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public boolean equals(Object obj) {
        lazyLoad();
        return obj instanceof LongArray2D && Arrays.equals(this.underlying, ((LongArray2D) obj).underlying);
    }

    @Override
    public String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public LongArray2D(Object[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public LongArray2D(long[][] src) {
        RuntimeAssertionError.assertTrue(null != src);
        int d0 = src.length;
        this.underlying = new Object[d0];
        for (int i = 0; i < d0; i++){
            this.underlying[i] = new LongArray(src[i]);
        }
    }

}
