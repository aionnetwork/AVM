package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;

public class ShortArray2D extends ObjectArray {

    public static ShortArray2D initArray(int d0, int d1){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        ShortArray2D ret = new ShortArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, ShortArray.initArray(d1));
        }
        return ret;
    }

    public ShortArray2D(int c) {
        super(c);
    }

    public ShortArray2D() {
        super();
    }

    // Deserializer support.
    public ShortArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public IObject avm_clone() {
        lazyLoad();
        return new ShortArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        lazyLoad();
        return new ShortArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public boolean equals(Object obj) {
        lazyLoad();
        return obj instanceof ShortArray2D && Arrays.equals(this.underlying, ((ShortArray2D) obj).underlying);
    }

    @Override
    public String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public ShortArray2D(Object[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public ShortArray2D(short[][] src) {
        RuntimeAssertionError.assertTrue(null != src);
        int d0 = src.length;
        this.underlying = new Object[d0];
        for (int i = 0; i < d0; i++){
            this.underlying[i] = new ShortArray(src[i]);
        }
    }

}
