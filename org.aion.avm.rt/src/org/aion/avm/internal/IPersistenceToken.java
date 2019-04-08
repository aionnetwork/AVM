package org.aion.avm.internal;


/**
 * NOTE:  Temporarily used as a wrapper for the readIndex so we don't need to update all the constructors
 * in the shadow JCL and generated code in the same commit which completely changes persistence
 * functionality.
 * This split keeps this commit more focused.
 * That said, this in-between point is never meant to be long-lived.
 */
public class IPersistenceToken {
    public final int readIndex;
    public IPersistenceToken(int readIndex) {
        this.readIndex = readIndex;
    }
}
