package org.aion.avm.arraywrapper;

public class FloatArray extends Array {

    private float[] underlying;

    public static FloatArray initArray(int c){
        return new FloatArray(c);
    }

    public FloatArray(int c) {
        this.underlying = new float[c];
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
