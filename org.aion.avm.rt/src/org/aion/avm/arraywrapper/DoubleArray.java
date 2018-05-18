package org.aion.avm.arraywrapper;

public class DoubleArray extends Array {

    private double[] underlying;

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
