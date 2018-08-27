package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.shadow.java.lang.Object;

public class Result extends Object {

    private boolean success;

    private ByteArray returnData;

    public Result(boolean success, ByteArray returnData) {
        this.success = success;
        this.returnData = returnData;
    }

    public boolean avm_isSuccess() {
        return success;
    }

    public ByteArray avm_getReturnData() {
        return returnData;
    }

    // compiler-facing
    public boolean isSuccess() {
        return success;
    }

    public byte[] getReturnData() {
        return returnData.getUnderlying();
    }
}
