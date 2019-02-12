package org.aion.avm.shadowapi.org.aion.avm.api;

import org.aion.avm.RuntimeMethodFeeSchedule;
import org.aion.avm.abi.internal.ABIException;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.abi.internal.ABICodec;
import org.aion.avm.internal.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class ABIEncoder {
    /**
     * This class cannot be instantiated.
     */
    private ABIEncoder(){}

    public static ByteArray avm_encodeMethodArguments(org.aion.avm.shadow.java.lang.String methodName, IObjectArray arguments)  {
        if ((null == methodName) || (null == arguments)) {
            throw new NullPointerException();
        }
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIEncoder_avm_encodeMethodArguments);
        // This entry-point is called from _inside_ the DApp, meaning that we are using internal types.  Convert those to public types.
        // NOTE:  This entry-point does NOT support passing null arguments.
        List<ABICodec.Tuple> tuplesToEncode = new ArrayList<>();
        tuplesToEncode.add(new ABICodec.Tuple(String.class, ABIStaticState.getSupport().convertToStandardValue(methodName)));
        for (int i = 0; i < arguments.length(); ++i) {
            IObject arg = (IObject) arguments.get(i);
            Object privateArg = ABIStaticState.getSupport().convertToStandardValue(arg);
            // We sniff the type, directly, since there are no nulls in this path.
            tuplesToEncode.add(new ABICodec.Tuple(privateArg.getClass(), privateArg));
        }

        try {
            byte[] serialized = ABICodec.serializeList(tuplesToEncode);
            return new ByteArray(serialized);
        } catch (ABIException e) {
            throw new ABICodecException(e.getMessage());
        }
    }

    public static ByteArray avm_encodeOneObject(IObject data) {
        if (null == data) {
            throw new NullPointerException();
        }
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIEncoder_avm_encodeOneObject);
        
        Object privateData = ABIStaticState.getSupport().convertToStandardValue(data);

        try {
            byte[] serialized = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(privateData.getClass(), privateData)));
            return new ByteArray(serialized);
        } catch (ABIException e) {
            throw new ABICodecException(e.getMessage());
        }
    }
}
