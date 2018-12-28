package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aion.avm.RuntimeMethodFeeSchedule;


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
        byte[] serialized = ABICodec.serializeList(tuplesToEncode);
        return new ByteArray(serialized);
    }

    public static ByteArray avm_encodeOneObject(IObject data) {
        if (null == data) {
            throw new NullPointerException();
        }
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIEncoder_avm_encodeOneObject);
        
        Object privateData = ABIStaticState.getSupport().convertToStandardValue(data);
        byte[] serialized = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(privateData.getClass(), privateData)));
        return new ByteArray(serialized);
    }


    /**
     * An utility method to encode the method name and method arguments to call with, according to Aion ABI format. Both method name and the arguments can be null if needed.
     * @param methodName the method name of the Dapp main class to call with.
     * @param arguments the arguments of the corresponding method of Dapp main class to call with.
     * @return the encoded byte array that contains the method descriptor, followed by the argument descriptor and encoded arguments, according the Aion ABI format.
     * @throws NullPointerException If methodName or arguments are null (note that, under normal usage, arguments will be empty instead of null).
     */
    public static byte[] encodeMethodArguments(String methodName, Object... arguments)  {
        if ((null == methodName) || (null == arguments)) {
            throw new NullPointerException();
        }

        List<ABICodec.Tuple> tuplesToEncode = new ArrayList<>();
        tuplesToEncode.add(new ABICodec.Tuple(String.class, methodName));
        for (Object arg : arguments) {
            // We sniff the type, directly, since there are no nulls in this path.
            tuplesToEncode.add(new ABICodec.Tuple(arg.getClass(), arg));
        }
        return ABICodec.serializeList(tuplesToEncode);
    }

    /**
     * Encode one object of any type that Aion ABI allows; generate the byte array that contains the descriptor and the encoded data.
     * @param data one object of any type that Aion ABI allows
     * @return the byte array that contains the argument descriptor and the encoded data.
     * @throws NullPointerException If data is null.
     */
    public static byte[] encodeOneObject(Object data) {
        if (null == data) {
            throw new NullPointerException();
        }
        return ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(data.getClass(), data)));
    }
}
