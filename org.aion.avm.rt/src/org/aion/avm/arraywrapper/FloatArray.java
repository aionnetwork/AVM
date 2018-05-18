package org.aion.avm.arraywrapper;

public class FloatArray extends Array {

    private float[] underlying;

    public FloatArray(float[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public float get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, float val) {
        this.underlying[idx] = val;
    }
}
