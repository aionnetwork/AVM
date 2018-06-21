package org.aion.avm.core;

import org.aion.avm.api.Address;

public class AvmResult {

    public enum  Code {
        SUCCESS, INVALID_TX, INVALID_JAR, INVALID_CODE, INVALID_CALL, OUT_OF_ENERGY, FAILURE
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
    byte[] returnData;

    /**
     * Return the address after deployment.
     */
    Address address;

    public AvmResult(Code code, long energyLeft) {
        this.code = code;
        this.energyLeft = energyLeft;
    }

    public AvmResult(Code code, long energyLeft, byte[] returnData) {
        this.code = code;
        this.energyLeft = energyLeft;
        this.returnData = returnData;
    }

    public AvmResult(Code code, long energyLeft, Address address) {
        this.code = code;
        this.energyLeft = energyLeft;
        this.address = address;
    }

    @Override
    public String toString() {
        return "AvmResult{" +
                "code=" + code +
                ", energyLeft=" + energyLeft +
                '}';
    }
}
