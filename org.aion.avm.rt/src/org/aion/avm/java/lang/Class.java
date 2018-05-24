package org.aion.avm.java.lang;

public class Class<T> extends Object {
    private final java.lang.Class<T> underlying;

    public Class(java.lang.Class<T> underlying) {
        this.underlying = underlying;
    }

    public String avm_getName() {
        return new String(underlying.getName());
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }
}
