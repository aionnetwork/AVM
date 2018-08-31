package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class ShortArray2D extends ObjectArray {

    public static ShortArray2D initArray(int d0, int d1){
        chargeEnergy(d0 * ArrayElement.REF.getEnergy());
        ShortArray2D ret = new ShortArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, ShortArray.initArray(d1));
        }
        return ret;
    }

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortArray2D_avm_clone);
        lazyLoad();
        return new ShortArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new ShortArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
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
    // Internal Helper
    //========================================================

    public ShortArray2D(int c) {
        super(c);
    }

    public ShortArray2D() {
        super();
    }

    public ShortArray2D(Object[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortArray2D_avm_constructor_2);
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

    //========================================================
    // Persistent Memory Support
    //========================================================

    public ShortArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

}
