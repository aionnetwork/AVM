package org.aion.avm.java.lang;
import org.aion.avm.arraywrapper.ByteArray;

public class String extends Object {
    private final java.lang.String underlying;

    public String(String str) {
        // TODO: is this correct?
        this.underlying = str.underlying;
    }

    public String(java.lang.String underlying) {
        this.underlying = underlying;
    }

    public int avm_hashCode() {
        // In the case of string, we want to use the actual hashcode.
        return this.underlying.hashCode();
    }

    public ByteArray avm_getBytes() {
        // TODO: Add shadow underlying
        return new ByteArray(underlying.getBytes());
    }

    public String avm_toString() {
        return this;
    }

    public int avm_length() {
        return this.underlying.length();
    }

    @Override
    public int hashCode() {
        // We probably want a consistent hashCode answer, for strings, since they are data-defined.
        return this.underlying.hashCode();
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }

}
