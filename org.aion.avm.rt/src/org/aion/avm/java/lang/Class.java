package org.aion.avm.java.lang;

import org.aion.avm.internal.IHelper;


public class Class<T> extends Object {
    private final java.lang.Class<T> underlying;

    public Class(java.lang.Class<T> underlying) {
        this.underlying = underlying;
    }

    public String avm_getName() {
        // Note that the class name is a constant so use the wrapper which will intern the instance.
        return IHelper.currentContractHelper.get().externalWrapAsString(underlying.getName());
    }

    public String avm_toString() {
        return null;
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }
}
