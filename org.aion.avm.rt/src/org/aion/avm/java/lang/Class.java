package org.aion.avm.java.lang;

import org.aion.avm.internal.Helper;


public class Class<T> extends Object {
    private final java.lang.Class<T> underlying;

    public Class(java.lang.Class<T> underlying) {
        this.underlying = underlying;
    }

    public String avm_getName() {
        // Note that the class name is a constant so use the wrapper which will intern the instance.
        return Helper.wrapAsString(underlying.getName());
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }
}
