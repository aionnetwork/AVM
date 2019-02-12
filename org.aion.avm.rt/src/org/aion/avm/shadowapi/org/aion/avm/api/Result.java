package org.aion.avm.shadowapi.org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.shadow.java.lang.Object;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class Result extends Object {

    private boolean success;

    private ByteArray returnData;

    public Result(boolean success, ByteArray returnData) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Result_avm_constructor);
        this.success = success;
        this.returnData = returnData;
    }

    public boolean avm_isSuccess() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Result_avm_isSuccess);
        return success;
    }

    public ByteArray avm_getReturnData() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Result_avm_getReturnData);
        return returnData;
    }
}
