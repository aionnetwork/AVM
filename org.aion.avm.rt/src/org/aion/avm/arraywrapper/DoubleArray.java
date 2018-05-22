package org.aion.avm.arraywrapper;

public class DoubleArray extends Array {

    private double[] underlying;

    public static DoubleArray initArray(int c){
        return new DoubleArray(c);
    }

    public DoubleArray(int c) {
        this.underlying = new double[c];
    }

    public DoubleArray(double[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public double get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, double val) {
        this.underlying[idx] = val;
    }
}
