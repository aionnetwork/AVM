package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;

public class FloatArray2D extends ObjectArray {

    public static FloatArray2D initArray(int d0, int d1){
        IHelper.currentContractHelper.get().externalChargeEnergy(d0 * ArrayElement.REF.getEnergy());
        FloatArray2D ret = new FloatArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, FloatArray.initArray(d1));
        }
        return ret;
    }

    @Override
    public IObject avm_clone() {
        lazyLoad();
        return new FloatArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new FloatArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
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
    // Internal Helper
    //========================================================

    public FloatArray2D(int c) {
        super(c);
    }

    public FloatArray2D() {
        super();
    }

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

    //========================================================
    // Persistent Memory Support
    //========================================================

    public FloatArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

}
