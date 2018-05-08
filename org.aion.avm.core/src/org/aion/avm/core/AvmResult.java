package org.aion.avm.core;

public class AvmResult {

    public enum  Code {
        SUCCESS, OUT_OF_ENERGY, FAILURE
    }

    /**
     * Return code.
     */
    Code code;

    /**
     * Return data.
     */
    byte[] data;
}
