package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;

public class CharArray2D extends ObjectArray {

    public static CharArray2D initArray(int d0, int d1){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        CharArray2D ret = new CharArray2D(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, CharArray.initArray(d1));
        }
        return ret;
    }

    public CharArray2D(int c) {
        super(c);
    }

    public CharArray2D() {
        super();
    }

    // Deserializer support.
    public CharArray2D(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public IObject avm_clone() {
        lazyLoad();
        return new CharArray2D(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        lazyLoad();
        return new CharArray2D(Arrays.copyOf(underlying, underlying.length));
    }

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
    // Methods below are used by runtime and test code only!
    //========================================================

    public CharArray2D(Object[] underlying) {
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

}
