package org.aion.kernel;

import org.aion.avm.core.util.Helpers;

public class TransactionResult {

    public enum  Code {
        SUCCESS, INVALID_TX, INVALID_JAR, INVALID_CODE, INVALID_CALL, OUT_OF_ENERGY, FAILURE
    }

    /**
     * The execution result code.
     */
    public Code code;

    /**
     * The remaining energy after execution.
     */
    public long energyLeft;

    /**
     * Return data.
     */
    public byte[] returnData;

    public TransactionResult(Code code, long energyLeft) {
        this.code = code;
        this.energyLeft = energyLeft;
    }

    public TransactionResult(Code code, long energyLeft, byte[] returnData) {
        this.code = code;
        this.energyLeft = energyLeft;
        this.returnData = returnData;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "code=" + code +
                ", energyLeft=" + energyLeft +
                ", returnData=" + Helpers.toHexString(returnData) +
                '}';
    }
}
