package org.aion.avm.arraywrapper;

public class CharArray extends Array {

    private char[] underlying;

    public CharArray(char[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public char get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, char val) {
        this.underlying[idx] = val;
    }
}
