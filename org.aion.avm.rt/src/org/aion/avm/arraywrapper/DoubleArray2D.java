package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class DoubleArray2D extends ObjectArray {

    public static DoubleArray2D initArray(int d0, int d1){
        chargeEnergy(d0 * ArrayElement.REF.getEnergy());
        DoubleArray2D ret = new DoubleArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, DoubleArray.initArray(d1));
        }
        return ret;
    }

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.DoubleArray2D_avm_clone + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length());
        lazyLoad();
        return new DoubleArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new DoubleArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
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
    // Internal Helper
    //========================================================

    public DoubleArray2D(int c) {
        super(c);
    }

    public DoubleArray2D() {
        super();
    }

    public DoubleArray2D(Object[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.DoubleArray2D_avm_constructor_2);
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

    //========================================================
    // Persistent Memory Support
    //========================================================

    public DoubleArray2D(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

}
