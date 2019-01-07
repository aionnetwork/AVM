package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.aion.avm.RuntimeMethodFeeSchedule;


public final class ABIDecoder {
    /**
     * This class cannot be instantiated.
     */
    private ABIDecoder(){}

    /**
     * Decode the transaction data and invoke the corresponding method of the Dapp class.
     * @param clazz the user space class.
     * @param txData the transaction data that is encoded with the method name and arguments to call with.
     * @return the encoded return data from the method call (never null).
     * @throws NullPointerException If given null clazz or txData.
     */
    public static ByteArray avm_decodeAndRunWithClass(org.aion.avm.shadow.java.lang.Class<?> clazz, ByteArray txData) {
        if ((null == clazz) || (null == txData)) {
            throw new NullPointerException();
        }
        if (txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is quite high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeAndRunWithClass);
        
        return internalDecodeAndRun(clazz.getRealClass(), null, txData.getUnderlying());
    }

    /**
     * Decode the transaction data and invoke the corresponding method of the object's class.
     * @param obj the user space class object.
     * @param txData the transaction data that is encoded with the method name and arguments to call with.
     * @return the encoded return data from the method call.
     * @throws NullPointerException If given null obj or txData.
     */
    public static ByteArray avm_decodeAndRunWithObject(IObject obj, ByteArray txData) {
        if ((null == obj) || (null == txData)) {
            throw new NullPointerException();
        }
        if (txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is quite high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeAndRunWithObject);
        return internalDecodeAndRun(obj.getClass(), obj, txData.getUnderlying());
    }

    /**
     * Decode the transaction data and return the method name.
     * @param txData the transaction data that has the encoded method name to call with.
     * @return the decoded method name.
     * @throws NullPointerException If given null txData.
     */
    public static org.aion.avm.shadow.java.lang.String avm_decodeMethodName(ByteArray txData) {
        if (null == txData) {
            throw new NullPointerException();
        }
        if (txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeMethodName);
        
        // We inline the core "decodeAsShadowObjects" logic here since we are only interested in decoding the first data element.
        // (We will probably deprecate this method, anyway).
        List<ABICodec.Tuple> parsed = ABICodec.parseEverything(txData.getUnderlying());
        if (parsed.size() < 1) {
            throw new ABICodecException("Decoded as " + parsed.size() + " elements");
        }
        if (String.class != parsed.get(0).standardType) {
            throw new ABICodecException("First parsed value not String (method name)");
        }
        // We are exposing this to the user so they will just see the original value, not our internal representation.
        String rawMethodName = (String) parsed.get(0).value;
        // Return the safe instance.
        return (org.aion.avm.shadow.java.lang.String) new org.aion.avm.shadow.java.lang.String(rawMethodName);
    }

    /**
     * Decode the transaction data and return the argument list that is encoded in it.
     * @param txData the transaction data that has the encoded arguments descriptor and arguments.
     * @return an object array that contains all of the arguments.
     * @throws NullPointerException If given null txData.
     */
    public static IObjectArray avm_decodeArguments(ByteArray txData) {
        if (null == txData) {
            throw new NullPointerException();
        }
        if (txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeArguments);
        
        // We inline the core "decodeAsShadowObjects" logic here since we aren't interested in decoding the first data element.
        // (We will probably deprecate this method, anyway).
        List<ABICodec.Tuple> parsed = ABICodec.parseEverything(txData.getUnderlying());
        if (parsed.size() < 1) {
            throw new ABICodecException("Decoded as " + parsed.size() + " elements");
        }
        if (String.class != parsed.get(0).standardType) {
            throw new ABICodecException("First parsed value not String (method name)");
        }
        Object[] argValues = new Object[parsed.size() - 1];
        for (int i = 1; i < parsed.size(); ++i) {
            Object publicValue = parsed.get(i).value;
            // Note that we are returning these objects so we don't do any box-type identity-mapping.
            argValues[i - 1] = ABIStaticState.getSupport().convertToShadowValue(publicValue);
        }
        return new ObjectArray(argValues);
    }

    /**
     * Decode the transaction data that has one object encoded in it.
     * @param txData the transaction data that has one object encoded in it (with the descriptor).
     * @return the decoded object.
     * @throws NullPointerException If given null txData.
     */
    public static IObject avm_decodeOneObject(ByteArray txData){
        if (null == txData) {
            throw new NullPointerException();
        }
        if (txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeOneObject);
        Object[] privateValues = decodeAsShadowObjects(txData.getUnderlying());
        return (IObject) privateValues[0];
    }


    /*
     * These 2 methods are mostly just for satisfying compilation of DApp unit tests.
     * Since the unit tests are in "org.aion.avm.core" module, with "import org.aion.avm.api.ABIDecoder", at compilation time,
     * this class is actually referred; instead, in the user space where the real Dapp lives in, the ABIDecoder in "org.aion.avm.api"
     * module (from which the api jar is built) is referred. Thus, at the compilation time, the unit tests need the 2 methods below;
     * while the Dapps do not.
     * 
     * At runtime, most callers have been transformed to call the "avm_*" variants.  Some cases, however, such as the testWallet,
     * expect that they can test this directly (although that assumption may be removed in the future).
     */
    public static byte[] decodeAndRunWithClass(Class<?> clazz, byte[] txData) {
        if ((null == clazz) || (null == txData)) {
            throw new NullPointerException();
        }
        if (txData.length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is quite high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeAndRunWithClass);
        
        ByteArray result = internalDecodeAndRun(clazz, null, txData);
        return (null != result)
                ? result.getUnderlying()
                : null;
    }
    public static byte[] decodeAndRunWithObject(Object obj, byte[] txData) {
        if ((null == obj) || (null == txData)) {
            throw new NullPointerException();
        }
        throw RuntimeAssertionError.unimplemented("Method for compilation only!");
    }

    public static Object decodeOneObject(byte[] response) {
        // We will handle an empty payload as a null.
        // TODO:  Generalize this handling.
        Object result = null;
        if (response.length > 0) {
            List<ABICodec.Tuple> parsed = ABICodec.parseEverything(response);
            RuntimeAssertionError.assertTrue(1 == parsed.size());
            result = parsed.get(0).value;
        }
        return result;
    }

    public static Object[] decodeArguments(byte[] data) {
        if (null == data) {
            throw new NullPointerException();
        }
        throw RuntimeAssertionError.unimplemented("Method for compilation only!");
    }

    public static String decodeMethodName(byte[] data) {
        if (null == data) {
            throw new NullPointerException();
        }
        throw RuntimeAssertionError.unimplemented("Method for compilation only!");
    }


    private static ByteArray internalDecodeAndRun(Class<?> clazz, IObject targetInstance, byte[] inputBytes) throws ABICodecException {
        List<ABICodec.Tuple> parsed = ABICodec.parseEverything(inputBytes);
        // There MUST be at least 1 parsed parameter and the first one MUST be a string (methodName).
        if (parsed.size() < 1) {
            throw new ABICodecException("Decoded as " + parsed.size() + " elements");
        }
        if (String.class != parsed.get(0).standardType) {
            throw new ABICodecException("First parsed value not String (method name)");
        }
        String methodName = ABIStaticState.getSupport().convertToShadowMethodName((String)parsed.get(0).value);
        // Capture the types - skip the first, since that is the method name.
        Class<?>[] argTypes = new Class<?>[parsed.size() - 1];
        for (int i = 1; i < parsed.size(); ++i) {
            Class<?> standardType = parsed.get(i).standardType;
            // Note that our ABI doesn't allow for box types so map those to primitives.
            // Other types might need some special conversion to a "binding type" from an "instantiation type".
            Class<?> abiType = isBoxType(standardType)
                    ? getPrimitiveType(standardType)
                    : ABIStaticState.getSupport().convertToBindingShadowType(standardType);
            argTypes[i - 1] = abiType;
        }
        Method targetMethod = null;
        try {
            targetMethod = clazz.getMethod(methodName, argTypes);
            // Make sure that our static modifier is consistent with how we were used.
            boolean mustBeStatic = (null == targetInstance);
            boolean isStatic = (0 != (Modifier.STATIC & targetMethod.getModifiers()));
            if (mustBeStatic != isStatic) {
                throw new ABICodecException("Invalid static modifier");
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ABICodecException("Method not found", e);
        }
        Object[] argValues = new Object[parsed.size() - 1];
        for (int i = 1; i < parsed.size(); ++i) {
            Object standardValue = parsed.get(i).value;
            Object safeParameterValue = null;
            // Note that we are going to use these values with reflection and the user code cannot accept box type parameters (only primitives) so identity-map box types.
            // Note that box-types must be identity-mapped.
            if (null != standardValue) {
                Class<?> standardClass = standardValue.getClass();
                safeParameterValue = isBoxType(standardClass)
                        ? standardValue
                        : ABIStaticState.getSupport().convertToShadowValue(standardValue);
            }
            argValues[i - 1] = safeParameterValue;
        }
        // Note that the result of the invoke should be a shadow object but may be a box type if reflection generated that due to the user returning a primitive.
        Object result = null;
        try {
            result = targetMethod.invoke(targetInstance, argValues);
        } catch (InvocationTargetException e) {
            // The case of InvocationTargetException is just a normal exception in the user code so we need to decode it.
            Throwable cause = e.getTargetException();

            if (cause instanceof AvmThrowable) {
                throw (AvmThrowable) cause;
            } else if (cause instanceof RuntimeException) {
                throw new UncaughtException(cause);
            } else if (cause instanceof org.aion.avm.exceptionwrapper.java.lang.Throwable) {
                throw new UncaughtException(cause);
            } else {
                RuntimeAssertionError.unexpected(cause);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new ABICodecException("Unusual failure in ABI-originated invoke", e);
        }
        // The return type mostly matters from the perspective of our ABI encoding, but we can safely preserve that.
        Class<?> bindingReturnType = targetMethod.getReturnType();
        // TODO:  Remove this.  IObject return type should NOT be allowed but we will currently interpret it as void.
        boolean isVoidReturn = (void.class == bindingReturnType) || (IObject.class == bindingReturnType);
        byte[] encodedResultBytes = null;
        if (isVoidReturn) {
            // We just handle void return as an empty byte[].
            encodedResultBytes = null;
        } else {
            // Note that we don't want to operate on primitives so get the boxed type, if need be.
            Class<?> boxedReturnType = bindingReturnType.isPrimitive()
                    ? getBoxedType(bindingReturnType)
                    : ABIStaticState.getSupport().mapFromBindingTypeToConcreteType(bindingReturnType);
            // Box types are identity-mapped, so don't pass them into the mapping interface.
            Class<?> publicType = isBoxType(boxedReturnType)
                    ? boxedReturnType
                    : ABIStaticState.getSupport().convertConcreteShadowToStandardType(boxedReturnType);
            
            Object publicValue = null;
            if (null != result) {
                publicValue = isBoxType(result.getClass())
                        ? result
                        : ABIStaticState.getSupport().convertToStandardValue(result);
            }
            encodedResultBytes = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(publicType, publicValue)));
        }
        return (null != encodedResultBytes)
                ? new ByteArray(encodedResultBytes)
                : null;
    }

    private static Object[] decodeAsShadowObjects(byte[] inputBytes) {
        List<ABICodec.Tuple> parsed = ABICodec.parseEverything(inputBytes);
        Object[] argValues = new Object[parsed.size()];
        for (int i = 0; i < parsed.size(); ++i) {
            Object publicValue = parsed.get(i).value;
            // Values from this path are returned to the user code so we must allow box types to be converted to shadow objects.
            argValues[i] = ABIStaticState.getSupport().convertToShadowValue(publicValue);
        }
        return argValues;
    }

    private static Class<?> getPrimitiveType(Class<?> boxedType) {
        Class<?> primitive = null;
        if (Byte.class == boxedType) {
            primitive = byte.class;
        } else if (Boolean.class == boxedType) {
            primitive = boolean.class;
        } else if (Short.class == boxedType) {
            primitive = short.class;
        } else if (Character.class == boxedType) {
            primitive = char.class;
        } else if (Integer.class == boxedType) {
            primitive = int.class;
        } else if (Float.class == boxedType) {
            primitive = float.class;
        } else if (Long.class == boxedType) {
            primitive = long.class;
        } else if (Double.class == boxedType) {
            primitive = double.class;
        } else {
            throw RuntimeAssertionError.unreachable("This path is only for boxed types");
        }
        return primitive;
    }

    private static Class<?> getBoxedType(Class<?> primitiveType) {
        Class<?> boxed = null;
        if (byte.class == primitiveType) {
            boxed = Byte.class;
        } else if (boolean.class == primitiveType) {
            boxed = Boolean.class;
        } else if (short.class == primitiveType) {
            boxed = Short.class;
        } else if (char.class == primitiveType) {
            boxed = Character.class;
        } else if (int.class == primitiveType) {
            boxed = Integer.class;
        } else if (float.class == primitiveType) {
            boxed = Float.class;
        } else if (long.class == primitiveType) {
            boxed = Long.class;
        } else if (double.class == primitiveType) {
            boxed = Double.class;
        } else {
            throw RuntimeAssertionError.unreachable("This path is only for primitive types");
        }
        return boxed;
    }

    private static boolean isBoxType(Class<?> clazz) {
        return (false
            || (Byte.class == clazz)
            || (Boolean.class == clazz)
            || (Short.class == clazz)
            || (Character.class == clazz)
            || (Integer.class == clazz)
            || (Float.class == clazz)
            || (Long.class == clazz)
            || (Double.class == clazz)
        );
    }
}
