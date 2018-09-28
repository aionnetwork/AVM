package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class ByteArray2D extends ObjectArray {

    public static ByteArray2D initArray(int d0, int d1){
        chargeEnergy(d0 * ArrayElement.REF.getEnergy());
        ByteArray2D ret = new ByteArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, ByteArray.initArray(d1));
        }
        return ret;
    }

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteArray2D_avm_clone + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length());
        lazyLoad();
        return new ByteArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new ByteArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public boolean equals(Object obj) {
        lazyLoad();
        return obj instanceof ByteArray2D && Arrays.equals(this.underlying, ((ByteArray2D) obj).underlying);
    }

    @Override
    public String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Internal Helper
    //========================================================

    public ByteArray2D(int c) {
        super(c);
    }

    public ByteArray2D() {
        super();
    }

    public ByteArray2D(Object[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteArray2D_avm_constructor_2);
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public ByteArray2D(byte[][] src) {
        RuntimeAssertionError.assertTrue(null != src);
        int d0 = src.length;
        this.underlying = new Object[d0];
        for (int i = 0; i < d0; i++){
            this.underlying[i] = new ByteArray(src[i]);
        }
    }

    //========================================================
    // Persistent Memory Support
    //========================================================

    public ByteArray2D(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

}
