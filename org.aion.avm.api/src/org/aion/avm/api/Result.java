package org.aion.avm.api;

/**
 * Represents an cross-call invocation result.
 */
public class Result {

    private boolean success;

    private byte[] returnData;

    /**
     * Creates an instance.
     *
     * @param success    whether the invocation is success or not.
     * @param returnData the return data
     */
    public Result(boolean success, byte[] returnData) {
        this.success = success;
        this.returnData = returnData;
    }

    /**
     * Returns whether the invocation is success or not.
     *
     * @return true if success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the data returned by the invoked dapp.
     *
     * @return a byte array, may be NULL
     */
    public byte[] getReturnData() {
        return returnData;
    }
}
