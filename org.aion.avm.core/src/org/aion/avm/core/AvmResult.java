package org.aion.avm.core;

public class AvmResult {

    public enum  Code {
        SUCCESS, INVALID_JAR, INVALID_CODE, INVALID_CALL, OUT_OF_ENERGY, FAILURE
    }

    /**
     * The execution result code.
     */
    Code code;

    /**
     * The remaining energy after execution.
     */
    long energyLeft;

    /**
     * Return data.
     */
    Object returnData;

    public AvmResult(Code code, long energyLeft) {
        this.code = code;
        this.energyLeft = energyLeft;
    }

    public AvmResult(Code code, long energyLeft, Object returnData) {
        this.code = code;
        this.energyLeft = energyLeft;
        this.returnData = returnData;
    }

    @Override
    public String toString() {
        return "AvmResult{" +
                "code=" + code +
                ", energyLeft=" + energyLeft +
                '}';
    }
}
