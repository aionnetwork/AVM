package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.api.IAvmResultProxy;

public class AvmResult implements IAvmResultProxy {

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

    /**
     * Return the address after deployment.
     */
    Address address;

    public AvmResult(Code code, long energyLeft) {
        this.code = code;
        this.energyLeft = energyLeft;
    }

    public AvmResult(Code code, long energyLeft, Object returnData) {
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
