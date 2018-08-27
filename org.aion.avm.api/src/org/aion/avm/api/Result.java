package org.aion.avm.api;

public class Result {

    private boolean success;

    private byte[] returnData;

    public Result(boolean success, byte[] returnData) {
        this.success = success;
        this.returnData = returnData;
    }

    public boolean isSuccess() {
        return success;
    }

    public byte[] getReturnData() {
        return returnData;
    }
}
