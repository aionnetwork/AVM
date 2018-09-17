package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class LongArray2D extends ObjectArray {

    public static LongArray2D initArray(int d0, int d1){
        chargeEnergy(d0 * ArrayElement.REF.getEnergy());
        LongArray2D ret = new LongArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, LongArray.initArray(d1));
        }
        return ret;
    }

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.LongArray2D_avm_clone + 5 * length());
        lazyLoad();
        return new LongArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new LongArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
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
    // Internal Helper
    //========================================================

    public LongArray2D(int c) {
        super(c);
    }

    public LongArray2D() {
        super();
    }

    public LongArray2D(Object[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.LongArray2D_avm_constructor_2);
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

    //========================================================
    // Persistent Memory Support
    //========================================================

    public LongArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

}
