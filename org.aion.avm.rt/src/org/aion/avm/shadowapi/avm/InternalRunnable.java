package org.aion.avm.shadowapi.avm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.aion.avm.internal.CodecIdioms;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RevertException;
import org.aion.avm.internal.RuntimeAssertionError;


public final class InternalRunnable extends org.aion.avm.shadow.java.lang.Object implements org.aion.avm.shadow.java.lang.Runnable {
    private static final String METHOD_PREFIX = "avm_";

    public static InternalRunnable createRunnable(MethodHandles.Lookup lookup, MethodHandle target) {
        // Note that we need to convert this from a MethodHandle to a traditional reflection Method since we need to serialize it
        // and can't access the right MethodHandles.Lookup instance, later on.
        // We do that here, just to statically prove it is working.
        MethodHandleInfo info = lookup.revealDirect(target);
        Class<?> receiver = info.getDeclaringClass();
        String methodName = info.getName();
        RuntimeAssertionError.assertTrue(methodName.startsWith(METHOD_PREFIX));
        
        return new InternalRunnable(receiver, methodName);
    }


    // AKI-131: These are only used for serialization support so they are REAL objects, not shadow ones.
    private Class<?> receiver;
    private String methodName;

    private Method target;

    private InternalRunnable(Class<?> receiver, String methodName) {
        // We call the hidden super-class so this doesn't update our hash code.
        super(null, null, 0);
        this.receiver = receiver;
        this.methodName = methodName;
        this.target = createAccessibleMethod(receiver, methodName);
    }

    // Deserializer support.
    public InternalRunnable(java.lang.Void ignore, int readIndex) {
        super(ignore, readIndex);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(InternalRunnable.class, deserializer);
        
        // We write the class as a direct class object reference but the method name, inline.
        // Note that we can only store the class if it is a shadow class, so unwrap it.
        Object original = deserializer.readObject();
        String externalMethodName = CodecIdioms.deserializeString(deserializer);
        // (remember that the pre-pass always returns null).
        if (null != original) {
            Class<?> clazz = ((org.aion.avm.shadow.java.lang.Class<?>)original).getRealClass();
            // Note that the method name needs a prefix added.
            String methodName = METHOD_PREFIX + externalMethodName;
            
            this.receiver = clazz;
            this.methodName = methodName;
            this.target = createAccessibleMethod(clazz, methodName);
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(InternalRunnable.class, serializer);
        
        // We save the receiver class as an object reference and the method name, inline.
        // Note that we can only store the class if it is a shadow class, so unwrap it.
        org.aion.avm.shadow.java.lang.Class<?> clazz = new org.aion.avm.shadow.java.lang.Class<>(this.receiver);
        // Note that we need to strip the prefix from the method.
        String methodName = this.methodName.substring(METHOD_PREFIX.length());
        
        serializer.writeObject(clazz);
        CodecIdioms.serializeString(serializer, methodName);
    }

    @Override
    public void avm_run() {
        try {
            target.invoke(null);
        } catch (Throwable e) {
            // We will treat a failure here as something fatal.
            e.printStackTrace();
            throw new RevertException();
        }
    }


    private static Method createAccessibleMethod(Class<?> receiver, String methodName) {
        Method method = null;
        try {
            method = receiver.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException  e) {
            // We always have direct access to the user code.
            throw RuntimeAssertionError.unexpected(e);
        }
        method.setAccessible(true);
        return method;
    }
}
