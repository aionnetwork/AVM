package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.shadow.java.lang.Object;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class Result extends Object {

    private boolean success;

    private ByteArray returnData;

    public Result(boolean success, ByteArray returnData) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Result_avm_constructor);
        this.success = success;
        this.returnData = returnData;
    }

    public boolean avm_isSuccess() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Result_avm_isSuccess);
        return success;
    }

    public ByteArray avm_getReturnData() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Result_avm_getReturnData);
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
