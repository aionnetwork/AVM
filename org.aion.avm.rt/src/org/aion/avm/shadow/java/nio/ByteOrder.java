package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;

import org.aion.avm.RuntimeMethodFeeSchedule;

public final class ByteOrder extends org.aion.avm.shadow.java.lang.Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final ByteOrder avm_BIG_ENDIAN
            = new ByteOrder(java.nio.ByteOrder.BIG_ENDIAN);

    public static final ByteOrder avm_LITTLE_ENDIAN
            = new ByteOrder(java.nio.ByteOrder.LITTLE_ENDIAN);

    public org.aion.avm.shadow.java.lang.String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteOrder_avm_toString);
        return new org.aion.avm.shadow.java.lang.String(v.toString());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.nio.ByteOrder v;

    private ByteOrder(java.nio.ByteOrder underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteOrder_avm_constructor);
        this.v = underlying;
    }

    public java.nio.ByteOrder getV() {
        return v;
    }

    public static ByteOrder lookupForConstant(java.nio.ByteOrder underlying) {
        ByteOrder result = null;
        if (java.nio.ByteOrder.BIG_ENDIAN == underlying) {
            result = avm_BIG_ENDIAN;
        } else if (java.nio.ByteOrder.LITTLE_ENDIAN == underlying) {
            result = avm_LITTLE_ENDIAN;
        } else {
            // There are only 2 instances of this so we need to match one of them.
            RuntimeAssertionError.unreachable("No matching ByteOrder constant found");
        }
        return result;
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static ByteOrder nativeOrder()
}
