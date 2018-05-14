package org.aion.avm.java.lang;

public class String extends Object {
    private final java.lang.String underlying;
    
    public String(java.lang.String underlying) {
        this.underlying = underlying;
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }
}
