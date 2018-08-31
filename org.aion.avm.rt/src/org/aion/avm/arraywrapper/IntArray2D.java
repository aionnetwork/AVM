package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class IntArray2D extends ObjectArray {

    public static IntArray2D initArray(int d0, int d1){
        chargeEnergy(d0 * ArrayElement.REF.getEnergy());
        IntArray2D ret = new IntArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, IntArray.initArray(d1));
        }
        return ret;
    }

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.IntArray2D_avm_clone);
        lazyLoad();
        return new IntArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new IntArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public boolean equals(Object obj) {
        lazyLoad();
        return obj instanceof IntArray2D && Arrays.equals(this.underlying, ((IntArray2D) obj).underlying);
    }

    @Override
    public String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Internal Helper
    //========================================================

    public IntArray2D(int c) {
        super(c);
    }

    public IntArray2D() {
        super();
    }

    public IntArray2D(Object[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.IntArray2D_avm_constructor_2);
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public IntArray2D(int[][] src) {
        RuntimeAssertionError.assertTrue(null != src);
        int d0 = src.length;
        this.underlying = new Object[d0];
        for (int i = 0; i < d0; i++){
            this.underlying[i] = new IntArray(src[i]);
        }
    }

    //========================================================
    // Persistent Memory Support
    //========================================================

    public IntArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }


}
