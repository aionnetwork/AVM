package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;

public class DoubleArray2D extends ObjectArray {

    public static DoubleArray2D initArray(int d0, int d1){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        DoubleArray2D ret = new DoubleArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, DoubleArray.initArray(d1));
        }
        return ret;
    }

    public DoubleArray2D(int c) {
        super(c);
    }

    public DoubleArray2D() {
        super();
    }

    // Deserializer support.
    public DoubleArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public IObject avm_clone() {
        lazyLoad();
        return new DoubleArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        lazyLoad();
        return new DoubleArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public boolean equals(Object obj) {
        lazyLoad();
        return obj instanceof DoubleArray2D && Arrays.equals(this.underlying, ((DoubleArray2D) obj).underlying);
    }

    @Override
    public String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public DoubleArray2D(Object[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public DoubleArray2D(double[][] src) {
        RuntimeAssertionError.assertTrue(null != src);
        int d0 = src.length;
        this.underlying = new Object[d0];
        for (int i = 0; i < d0; i++){
            this.underlying[i] = new DoubleArray(src[i]);
        }
    }

}
