package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class CharArray2D extends ObjectArray {

    public static CharArray2D initArray(int d0, int d1){
        chargeEnergy(d0 * ArrayElement.REF.getEnergy());
        CharArray2D ret = new CharArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, CharArray.initArray(d1));
        }
        return ret;
    }

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharArray2D_avm_clone);
        lazyLoad();
        return new CharArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new CharArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public boolean equals(Object obj) {
        lazyLoad();
        return obj instanceof CharArray2D && Arrays.equals(this.underlying, ((CharArray2D) obj).underlying);
    }

    @Override
    public String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Internal Helper
    //========================================================

    public CharArray2D(int c) {
        super(c);
    }

    public CharArray2D() {
        super();
    }

    public CharArray2D(Object[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharArray2D_avm_constructor_2);
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public CharArray2D(char[][] src) {
        RuntimeAssertionError.assertTrue(null != src);
        int d0 = src.length;
        this.underlying = new Object[d0];
        for (int i = 0; i < d0; i++){
            this.underlying[i] = new CharArray(src[i]);
        }
    }

    //========================================================
    // Persistent Memory Support
    //========================================================

    public CharArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

}
