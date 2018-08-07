package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;

public class FloatArray2D extends ObjectArray {

    public static FloatArray2D initArray(int d0, int d1){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        FloatArray2D ret = new FloatArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, FloatArray.initArray(d1));
        }
        return ret;
    }

    public FloatArray2D(int c) {
        super(c);
    }

    public FloatArray2D() {
        super();
    }

    // Deserializer support.
    public FloatArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public IObject avm_clone() {
        lazyLoad();
        return new FloatArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        lazyLoad();
        return new FloatArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public boolean equals(Object obj) {
        lazyLoad();
        return obj instanceof FloatArray2D && Arrays.equals(this.underlying, ((FloatArray2D) obj).underlying);
    }

    @Override
    public String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public FloatArray2D(Object[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public FloatArray2D(float[][] src) {
        RuntimeAssertionError.assertTrue(null != src);
        int d0 = src.length;
        this.underlying = new Object[d0];
        for (int i = 0; i < d0; i++){
            this.underlying[i] = new FloatArray(src[i]);
        }
    }

}
