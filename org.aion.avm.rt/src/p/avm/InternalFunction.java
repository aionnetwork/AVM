package p.avm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import i.CodecIdioms;
import i.IObjectDeserializer;
import i.IObjectSerializer;
import i.RuntimeAssertionError;


public final class InternalFunction extends s.java.lang.Object implements s.java.util.function.Function {
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


    // AKI-131: These are only used for serialization support so they are REAL objects, not shadow ones.
    private Class<?> receiver;
    private String methodName;
    private Class<?> parameterType;

    private Method target;

    private InternalFunction(Class<?> receiver, String methodName, Class<?> parameterType) {
        // We call the hidden super-class so this doesn't update our hash code.
        super(null, null, 0);
        this.receiver = receiver;
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.target = createAccessibleMethod(receiver, methodName, parameterType);
    }

    // Deserializer support.
    public InternalFunction(java.lang.Void ignore, int readIndex) {
        super(ignore, readIndex);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(InternalFunction.class, deserializer);
        
        // We write the classes as direct class objects reference but the method name, inline.
        // Note that we can only store the class if it is a shadow class, so unwrap it.
        Object originalReceiver = deserializer.readObject();
        String externalMethodName = CodecIdioms.deserializeString(deserializer);
        Object originalParameter = deserializer.readObject();
        // (remember that the pre-pass always returns null).
        if (null != originalReceiver) {
            Class<?> receiver = ((s.java.lang.Class<?>)originalReceiver).getRealClass();
            // Note that the method name needs a prefix added.
            String methodName = METHOD_PREFIX + externalMethodName;
            Class<?> parameterType = ((s.java.lang.Class<?>)originalParameter).getRealClass();
            
            this.receiver = receiver;
            this.methodName = methodName;
            this.parameterType = parameterType;
            this.target = createAccessibleMethod(receiver, methodName, parameterType);
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(InternalFunction.class, serializer);
        
        // We save the classes as object references and the method name, inline.
        // Note that we can only store the class if it is a shadow class, so unwrap it.
        s.java.lang.Class<?> receiverClass = new s.java.lang.Class<>(this.receiver);
        // Note that we need to strip the prefix from the method.
        String methodName = this.methodName.substring(METHOD_PREFIX.length());
        s.java.lang.Class<?> parameterClass = new s.java.lang.Class<>(this.parameterType);
        
        serializer.writeObject(receiverClass);
        CodecIdioms.serializeString(serializer, methodName);
        serializer.writeObject(parameterClass);
    }

    @Override
    public i.IObject avm_apply(i.IObject input) {
        try {
            return (i.IObject) target.invoke(null, input);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // This would be a problem in our setup - an internal error.
            throw RuntimeAssertionError.unexpected(e);
        } catch (InvocationTargetException e) {
            // We need to unwrap this and re-throw it.
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                // Any failure below us shouldn't be anything other than RuntimeException.
                throw RuntimeAssertionError.unexpected(cause);
            }
        }
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
