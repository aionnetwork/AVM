package org.aion.avm.core;

public class AvmResult {

    public enum  Code {
        SUCCESS, INVALID_JAR, INVALID_CODE, OUT_OF_ENERGY, FAILURE
    }

    /**
     * Return code.
     */
    Code code;

    /**
     * Return data.
     */
    byte[] data;

    /**
     * The remaining energy after execution.
     */
    long energyLeft;

    public AvmResult(Code code, long energyLeft) {
        this.code = code;
        this.energyLeft = energyLeft;
    }
}
