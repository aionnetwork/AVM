package org.aion.avm.shadowapi.avm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.internal.RevertException;
import org.aion.avm.internal.RuntimeAssertionError;


public final class InternalFunction extends org.aion.avm.shadow.java.lang.Object implements org.aion.avm.shadow.java.util.function.Function {
    private static final String METHOD_PREFIX = "avm_";

    public static InternalFunction createFunction(MethodHandles.Lookup lookup, MethodHandle target) {
        // Note that we need to convert this from a MethodHandle to a traditional reflection Method since we need to serialize it
        // and can't access the right MethodHandles.Lookup instance, later on.
        // We do that here, just to statically prove it is working.
        MethodHandleInfo info = lookup.revealDirect(target);
        Class<?> receiver = info.getDeclaringClass();
        String methodName = info.getName();
        MethodType type = info.getMethodType();
        Class<?> parameterType = type.parameterType(0);
        RuntimeAssertionError.assertTrue(methodName.startsWith(METHOD_PREFIX));
        
        return new InternalFunction(receiver, methodName, parameterType);
    }


    private Method target;

    private InternalFunction(Class<?> receiver, String methodName, Class<?> parameterType) {
        // We call the hidden super-class so this doesn't update our hash code.
        super(null, null, 0);
        this.target = createAccessibleMethod(receiver, methodName, parameterType);
    }

    // Deserializer support.
    // TODO(AKI-131): Implement serialization support for lambdas.
    public InternalFunction(java.lang.Void ignore, int readIndex) {
        super(ignore, readIndex);
        this.target = null;
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        // TODO(AKI-131): Implement serialization support for lambdas.
        throw new OutOfEnergyException();
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        // TODO(AKI-131): Implement serialization support for lambdas.
        throw new OutOfEnergyException();
    }

    @Override
    public org.aion.avm.internal.IObject avm_apply(org.aion.avm.internal.IObject input) {
        try {
            return (org.aion.avm.internal.IObject) target.invoke(null, input);
        } catch (Throwable e) {
            // We will treat a failure here as something fatal.
            e.printStackTrace();
            throw new RevertException();
        }
    }

    public org.aion.avm.shadow.java.util.function.Function self() {
        return this;
    }


    private static Method createAccessibleMethod(Class<?> receiver, String methodName, Class<?> parameterType) {
        Method method = null;
        try {
            method = receiver.getDeclaredMethod(methodName, parameterType);
        } catch (NoSuchMethodException  e) {
            // We always have direct access to the user code.
            throw RuntimeAssertionError.unexpected(e);
        }
        method.setAccessible(true);
        return method;
    }
}
