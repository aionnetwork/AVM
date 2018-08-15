package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;

public class DoubleArray extends Array {

    private double[] underlying;

    /**
     * Static DoubleArray factory
     *
     * After instrumentation, NEWARRAY bytecode (with double as type) will be replaced by a INVOKESTATIC to
     * this method.
     *
     * @param size Size of the double array
     *
     * @return New empty double array wrapper
     */
    public static DoubleArray initArray(int size){
        chargeEnergy(size * ArrayElement.DOUBLE.getEnergy());
        return new DoubleArray(size);
    }

    @Override
    public int length() {
        lazyLoad();
        return this.underlying.length;
    }

    public double get(int idx) {
        lazyLoad();
        return this.underlying[idx];
    }

    public void set(int idx, double val) {
        lazyLoad();
        this.underlying[idx] = val;
    }

    @Override
    public IObject avm_clone() {
        lazyLoad();
        return new DoubleArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new DoubleArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Internal Helper
    //========================================================

    public DoubleArray(int c) {
        this.underlying = new double[c];
    }

    public DoubleArray(double[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public double[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    @Override
    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (double[]) u;
    }

    @Override
    public java.lang.Object getUnderlyingAsObject(){
        lazyLoad();
        return underlying;
    }

    @Override
    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }

    //========================================================
    // Persistent Memory Support
    //========================================================

    public DoubleArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(DoubleArray.class, deserializer);

        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new double[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = Double.longBitsToDouble(deserializer.readLong());
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(DoubleArray.class, serializer);

        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeLong(Double.doubleToLongBits(this.underlying[i]));
        }
    }
}
