package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.*;
import org.aion.avm.shadow.java.lang.Boolean;
import org.aion.avm.shadow.java.lang.Byte;
import org.aion.avm.shadow.java.lang.Float;
import org.aion.avm.shadow.java.lang.Short;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.aion.avm.RuntimeMethodFeeSchedule;

public final class ABIDecoder {
    /* ABI encoding separators */
    public static final char ARRAY_S = '[';
    public static final char ARRAY_E = ']';

    public static final char JAGGED_D_S = '(';
    public static final char JAGGED_D_E = ')';

    public static final char DESCRIPTOR_S = '<';
    public static final char DESCRIPTOR_E = '>';

    public static class MethodCaller {
        public String methodName;
        public String argsDescriptor;
        public IObject[] arguments;

        MethodCaller(String methodName, String argsDescriptor, IObject[] arguments) {
            this.methodName = methodName;
            this.argsDescriptor = argsDescriptor;
            this.arguments  = arguments;
        }
    }

    public static class Descriptor {
        public ABIEncoder.ABITypes type;// elementary types defined for Aion contract ABI
        public int dimension;           // 0: not an array; 1: 1D array; 2: 2D array
        public int size;                // number of components (1D or 2D array)
        public int[] rowSizes;          // 2D array: size of each component
        public int encodedBytes;        // number of bytes of the encoded descriptor

        Descriptor(ABIEncoder.ABITypes type, int dimension, int encodedBytes) {
            this.type = type;
            this.dimension = dimension;
            this.encodedBytes = encodedBytes;
        }

        Descriptor(ABIEncoder.ABITypes type, int dimension, int size, int encodedBytes) {
            this.type = type;
            this.dimension = dimension;
            this.size = size;
            this.encodedBytes = encodedBytes;
        }

        Descriptor(ABIEncoder.ABITypes type, int dimension, int size, int[] rowSizes, int encodedBytes) {
            this.type = type;
            this.dimension = dimension;
            this.size = size;
            this.rowSizes = rowSizes;
            this.encodedBytes = encodedBytes;
        }
    }

    public static class DecodedObjectInfo {
        Object object;      // decoded native object
        IObject iObject;    // decoded shadow/wrapped object
        int endByteOfData;  // the position in the encoded data byte buffer after this object is decoded

        DecodedObjectInfo(Object object, IObject iObject, int endByteOfData) {
            this.object = object;
            this.iObject = iObject;
            this.endByteOfData = endByteOfData;
        }
    }

    /**
     * This class cannot be instantiated.
     */
    private ABIDecoder(){}

    /*
     * Runtime-facing implementation.
     */

    /**
     * Decode the transaction data and invoke the corresponding method of the Dapp class.
     * @param clazz the user space class.
     * @param txData the transaction data that is encoded with the method name and arguments to call with.
     * @return the encoded return data from the method call.
     */
    public static ByteArray avm_decodeAndRunWithClass(org.aion.avm.shadow.java.lang.Class<?> clazz, ByteArray txData) {
        if (txData.getUnderlying() == null || txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is quite high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeAndRunWithClass);
        byte[] result = decodeAndRun(clazz.getRealClass(), txData.getUnderlying(), true);
        return (null != result)
                ? new ByteArray(result)
                : null;
    }

    /**
     * Decode the transaction data and invoke the corresponding method of the object's class.
     * @param obj the user space class object.
     * @param txData the transaction data that is encoded with the method name and arguments to call with.
     * @return the encoded return data from the method call.
     */
    public static ByteArray avm_decodeAndRunWithObject(IObject obj, ByteArray txData) {
        if (txData.getUnderlying() == null || txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is quite high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeAndRunWithObject);
        byte[] result = decodeAndRun(obj, txData.getUnderlying(), false);
        return (null != result)
                ? new ByteArray(result)
                : null;
    }

    /**
     * Decode the transaction data and return the method name.
     * @param txData the transaction data that has the encoded method name to call with.
     * @return the decoded method name.
     */
    public static org.aion.avm.shadow.java.lang.String avm_decodeMethodName(ByteArray txData) {
        if (txData.getUnderlying() == null || txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeMethodName);
        return new org.aion.avm.shadow.java.lang.String(decodeMethodName(txData.getUnderlying()));
    }

    /**
     * Decode the transaction data and return the argument list that is encoded in it.
     * @param txData the transaction data that has the encoded arguments descriptor and arguments.
     * @return an object array that contains all of the arguments.
     */
    public static IObjectArray avm_decodeArguments(ByteArray txData) {
        if (txData.getUnderlying() == null || txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeArguments);
        Object[] result = decodeArguments(txData.getUnderlying());
        return (null != result)
                ? new ObjectArray(result)
                : null;
    }

    /**
     * Decode the transaction data that has one object encoded in it.
     * @param txData the transaction data that has one object encoded in it (with the descriptor).
     * @return the decoded object.
     */
    public static IObject avm_decodeOneObject(ByteArray txData){
        if (txData.getUnderlying() == null || txData.getUnderlying().length == 0) {
            return null;
        } // do not charge in case of early exit, since the fee is high

        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIDecoder_avm_decodeOneObject);
        Descriptor descriptor = readOneDescriptor(txData.getUnderlying(), 0);
        return decodeOneObjectWithDescriptor(txData.getUnderlying(), descriptor.encodedBytes, descriptor).iObject;
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
        if (txData == null || txData.length == 0) {
            return null;
        }

        return decodeAndRun(clazz, txData, true);
    }
    public static byte[] decodeAndRunWithObject(Object obj, byte[] txData) {
        if (txData == null || txData.length == 0) {
            return null;
        }

        return decodeAndRun(obj, txData, false);
    }

    /*
     * Underlying implementation.
     */

    /** Underlying implementation of {@link #avm_decodeAndRunWithObject(IObject, ByteArray) avm_decodeAndRunWithObject}
     * and {@link #avm_decodeAndRunWithClass(org.aion.avm.shadow.java.lang.Class, ByteArray)}  avm_decodeAndRunWithClass} methods
     * @throws ABICodecException the transaction data cannot be properly decoded, or cannot be converted to the method arguments
     */
    public static byte[] decodeAndRun(Object obj, byte[] txData, boolean isWithClass) {
        if (txData == null || txData.length == 0) {
            return null;
        }

        MethodCaller methodCaller = decode(txData);

        String newMethodName = "avm_" + methodCaller.methodName;
        String newArgDescriptor = methodCaller.argsDescriptor;

        // generate the method descriptor of each main class method, compare to the method selector to select or invalidate the txData
        Method method;
        if (isWithClass) {
            method = matchMethodSelector((Class<?>) obj, newMethodName, newArgDescriptor);
        } else {
            method = matchMethodSelector(obj.getClass(), newMethodName, newArgDescriptor);
        }

        Object ret = null;
        if (Modifier.isStatic(method.getModifiers())) {
            obj = null;
        }
        // all checked exceptions need to be caught here, so the Dapp user doesn't need to declare them when calling into ABI decoder.
        // And the unchecked exceptions are thrown for the DappExecutor to handle them.
        try {
            if (methodCaller.arguments == null) {
                ret = method.invoke(obj);
            }
            else {
                ret = method.invoke(obj, convertArguments(method, methodCaller.arguments));
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new MethodAccessException(e);

        } catch (InvocationTargetException e) {
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
        }

        return (null != ret)
                ? ABIEncoder.encodeOneObject(ret)
                : null;
    }

    /** Underlying implementation of {@link #avm_decodeMethodName(ByteArray) avm_decodeMethodName} method
     * @throws ABICodecException the transaction data cannot be properly decoded, or cannot be converted to the method arguments
     */
    public static String decodeMethodName(byte[] txData) {
        if (txData == null || txData.length == 0) {
            return null;
        }

        String decoded = new String(txData);

        int m1 = decoded.indexOf(DESCRIPTOR_S);

        if (m1 == -1) {
            return decoded;
        } else {
            return decoded.substring(0, m1);
        }
    }

    /** Underlying implementation of {@link #avm_decodeArguments(ByteArray) avm_decodeArguments} method */
    public static Object[] decodeArguments(byte[] data){
        if (data == null || data.length == 0) {
            return null;
        }

        return decode(data).arguments;
    }

    /** Underlying implementation of {@link #avm_decodeOneObject(ByteArray) avm_decodeOneObject} method */
    public static Object decodeOneObject(byte[] data){
        if (data == null || data.length == 0) {
            return null;
        }

        Descriptor descriptor = readOneDescriptor(data, 0);
        return decodeOneObjectWithDescriptor(data, descriptor.encodedBytes, descriptor).object;
    }

    /** A helper method to decode the transaction data into the method caller, which contains the method name, arguments descriptor and arguments.
     * @throws ABICodecException the transaction data cannot be properly decoded, or cannot be converted to the method arguments
     */
    public static MethodCaller decode(byte[] txData) {
        if (txData == null || txData.length == 0) {
            return null;
        }

        String decoded = new String(txData);

        int m1 = decoded.indexOf(DESCRIPTOR_S);
        int m2 = decoded.indexOf(DESCRIPTOR_E);
        if (m1 == -1 && m2 == -1) {
            // no arguments
            return new MethodCaller(decoded, null, null);
        }
        if (m1 == -1 || m2 == -1) {
            throw new ABICodecException("the descriptor in the transaction data is not properly wrapped with \"<>\"");
        }

        String methodName = decoded.substring(0, m1);
        String argsDescriptor = decoded.substring(m1+1, m2);
        int startByteOfData = decoded.substring(0, m2 + 1).getBytes().length;

        IObject[] arguments = decodeArgumentsWithDescriptor(Arrays.copyOfRange(txData, startByteOfData, txData.length), argsDescriptor);

        return new MethodCaller(methodName, argsDescriptor, arguments);
    }

    /**
     * Decode and return the argument list, given the encoded byte array and the arguments descriptor
     * @param data the encoded byte array of the arguments
     * @param argsDescriptor the descriptor of the arguments
     * @return an object array that contains all of the arguments.
     */
    private static IObject[] decodeArgumentsWithDescriptor(byte[] data, String argsDescriptor){
        if (data == null || data.length == 0) {
            return null;
        }

        // read all descriptors
        int encodedBytes = 0;
        List<Descriptor> descriptorList = new ArrayList<>();
        while (encodedBytes < argsDescriptor.getBytes().length) {
            Descriptor descriptor = readOneDescriptor(argsDescriptor.getBytes(), encodedBytes);
            descriptorList.add(descriptor);
            encodedBytes += descriptor.encodedBytes;
        }

        if (descriptorList.size() == 0) {
            return null;
        }

        // decode the arguments
        IObject[] args = new IObject[descriptorList.size()];
        int argIndex = 0;
        int bytes = 0;
        for (Descriptor descriptor: descriptorList) {
            DecodedObjectInfo decodedObjectInfo = decodeOneObjectWithDescriptor(data, bytes, descriptor);
            args[argIndex] = decodedObjectInfo.iObject;
            argIndex ++;
            bytes = decodedObjectInfo.endByteOfData;
        }

        return args;
    }

    /**
     * A helper method to read one argument descriptor from the input data, starting from its index "start".
     * All possible characters are encoded in UTF-8 as one byte for each. So the character index in the string
     * is equal to the byte index in the byte array.
     */
    private static Descriptor readOneDescriptor(byte[] data, int start){
        if (data == null || data.length == 0) {
            return null;
        }

        String decoded = new String(data).substring(start);

        if (decoded.startsWith("[[")) {
            // 2D array
            int[] readNumM = readNumberFromDescriptor(decoded, ARRAY_E, 3);
            int[] readNumN = readNumberFromDescriptor(decoded, ARRAY_E, readNumM[1] + 1);
            int[] rowSizes = new int[readNumN[0]];
            int encodedBytes;
            if (readNumM[0] == 0) {
                // jagged array descriptor format
                int[] readNum = readNumberFromDescriptor(decoded, JAGGED_D_E, readNumN[1] + 2);
                rowSizes[0] = readNum[0];
                for (int i = 1; i < readNumN[0]; i ++) {
                    readNum = readNumberFromDescriptor(decoded, JAGGED_D_E, readNum[1] + 2);
                    rowSizes[i] = readNum[0];
                }
                encodedBytes = readNum[1] + 1;
            } else {
                Arrays.fill(rowSizes, readNumM[0]);
                encodedBytes = readNumN[1] + 1;
            }
            return new Descriptor(ABIEncoder.mapABITypes(decoded.substring(2,3)), 2, readNumN[0], rowSizes, encodedBytes);
        }
        else if (decoded.startsWith("[")) {
            // 1D array
            int[] readNum = readNumberFromDescriptor(decoded, ARRAY_E, 2);
            int encodedBytes = readNum[1] + 1;
            return new Descriptor(ABIEncoder.mapABITypes(decoded.substring(1,2)), 1, readNum[0], encodedBytes);
        }
        else {
            return new Descriptor(ABIEncoder.mapABITypes(decoded.substring(0,1)), 0, 1);
        }
    }

    /**
     * A helper method to read one integer from the array arguments descriptor.
     * @throws ABICodecException the transaction data cannot be properly decoded, or cannot be converted to the method arguments
     */
    private static int[] readNumberFromDescriptor(String argsDescriptor, char stopChar, int startIdx) {
        int[] res = new int[2]; // res[0]: the number encoded as argsDescriptor.substring(startIdx, idxE); res[1]: the index of stopChar in the argsDescriptor
        int idxE = argsDescriptor.indexOf(stopChar, startIdx);
        if (idxE == -1) {
            throw new ABICodecException("array dimension is not properly encoded in the transaction data");
        }

        res[1] = idxE;
        if (argsDescriptor.substring(startIdx, idxE).isEmpty()) {
            res[0] = 0; // may be a jagged array
        }
        else {
            try {
                res[0] = Integer.parseInt(argsDescriptor.substring(startIdx, idxE));
            }
            catch (NumberFormatException e) {
                throw new ABICodecException("array dimension is not properly encoded in the transaction data");
            }
        }
        return res;
    }

    /**
     * A helper method to decode one object from the encoded data stream with the starting index and descriptor.
     */
    private static DecodedObjectInfo decodeOneObjectWithDescriptor(byte[] data, int startByteOfData, Descriptor descriptor){
        if (data == null || data.length == 0) {
            return null;
        }

        if (descriptor.dimension == 0) {
            return descriptor.type.decode(data, startByteOfData);
        }
        else if (descriptor.dimension == 1) {
            return decode1DArray(data, startByteOfData, descriptor);
        }
        else {
            return decode2DArray(data, startByteOfData, descriptor);
        }
    }

    /**
     * A helper method to decode a 1D array from the encode data stream with the start index and descriptor.
     */
    private static DecodedObjectInfo decode1DArray(byte[] data, int startByteOfData, Descriptor descriptor){
        if (data == null || data.length == 0) {
            return null;
        }

        if (descriptor.type == ABIEncoder.ABITypes.avm_String) {
            String s = (new String(Arrays.copyOfRange(data, startByteOfData, data.length))).substring(0, descriptor.size);
            return new DecodedObjectInfo(s, new org.aion.avm.shadow.java.lang.String(s), startByteOfData + s.getBytes().length);
        }

        int endByte = startByteOfData;
        Object[] array = new Object[descriptor.size];
        Descriptor componentDescriptor = new Descriptor(descriptor.type, 0, 1);
        for (int idx = 0; idx < descriptor.size; idx ++) {
            DecodedObjectInfo decodedObjectInfo = decodeOneObjectWithDescriptor(data, endByte, componentDescriptor);
            array[idx] = decodedObjectInfo.object;
            endByte = decodedObjectInfo.endByteOfData;
        }
        return new DecodedObjectInfo(descriptor.type.constructNativeArray(array), descriptor.type.construct1DWrappedArray(array), endByte);
    }

    /**
     * A helper method to decode a 2D array from the encode data stream with the start index and descriptor.
     */
    private static DecodedObjectInfo decode2DArray(byte[] data, int startByteOfData, Descriptor descriptor){
        if (data == null || data.length == 0) {
            return null;
        }

        int endByte = startByteOfData;
        Object[] array = new Object[descriptor.size];
        for (int idx = 0; idx < descriptor.size; idx ++) {
            Descriptor rowDescriptor = new Descriptor(descriptor.type, 1, descriptor.rowSizes[idx], 0);
            DecodedObjectInfo decodedObjectInfo = decode1DArray(data, endByte, rowDescriptor);
            array[idx] = decodedObjectInfo.object;
            endByte = decodedObjectInfo.endByteOfData;
        }

        return new DecodedObjectInfo(descriptor.type.construct2DNativeArray(array), descriptor.type.construct2DWrappedArray(array), endByte);
    }

    /**
     * A helper method to match the method selector with the main-class methods.
     * @throws ABICodecException the transaction data cannot be properly decoded, or cannot be converted to the method arguments
     */
    public static Method matchMethodSelector(Class<?> clazz, String methodName, String argsDescriptor){
        Method[] methods = clazz.getMethods();

        String ARRAY_WRAPPER_PREFIX = PackageConstants.kArrayWrapperDotPrefix;

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if ((parameterTypes == null || parameterTypes.length == 0) && (argsDescriptor==null || argsDescriptor.isEmpty())) {
                    return method;
                }
                if (parameterTypes == null || parameterTypes.length == 0 || argsDescriptor == null || argsDescriptor.isEmpty()) {
                    break;
                }

                boolean matched = true;
                int parIdx = 0;  // parameter index in 'parameterTypes'
                int charIdx = 0; // character index in 'argsDescriptor'

                while (parIdx < parameterTypes.length && charIdx < argsDescriptor.length()) {
                    String pType = parameterTypes[parIdx].getName();

                    // read one descriptor and compare with the method parameter type
                    if (argsDescriptor.charAt(charIdx) == ABIDecoder.ARRAY_S) {
                        if (argsDescriptor.charAt(charIdx + 1) == ABIDecoder.ARRAY_S) {
                            // 2D array
                            // match condition - 1) pType matches with the ABI Type specified by the symbol in the argsDescriptor;
                            // 2) pType is one of the wrapped 2D array types; or, as a special case, the 1D String array.
                            if (Arrays.asList(ABIEncoder.mapABITypes(String.valueOf(argsDescriptor.charAt(charIdx + 2))).identifiers).contains(pType) &&
                                    (pType.startsWith(ARRAY_WRAPPER_PREFIX + "$$") || (pType.contains("L") && pType.contains("String")))) {
                                charIdx = argsDescriptor.indexOf(ABIDecoder.ARRAY_E, charIdx) + 1;
                                charIdx = argsDescriptor.indexOf(ABIDecoder.ARRAY_E, charIdx) + 1;
                                while (charIdx < argsDescriptor.length() && argsDescriptor.charAt(charIdx) == ABIDecoder.JAGGED_D_S) {
                                    charIdx = argsDescriptor.indexOf(ABIDecoder.JAGGED_D_E, charIdx) + 1;
                                }
                                parIdx ++;
                                continue;
                            }
                        }

                        // 1D array
                        // match condition - 1) pType matches with the ABI Type specified by the symbol in the argsDescriptor;
                        // 2) pType is one of the wrapped 1D array types; or, as a special case, the String type.
                        if (Arrays.asList(ABIEncoder.mapABITypes(String.valueOf(argsDescriptor.charAt(charIdx + 1))).identifiers).contains(pType) &&
                                (pType.startsWith(ARRAY_WRAPPER_PREFIX) && !pType.contains("$") ||
                                pType == PackageConstants.kShadowDotPrefix + "java.lang.String")) {
                            charIdx = argsDescriptor.indexOf(ABIDecoder.ARRAY_E, charIdx) + 1;
                            parIdx ++;
                            continue;
                        }
                    }
                    else {
                        // one elementary object
                        if (!pType.startsWith(ARRAY_WRAPPER_PREFIX) &&
                                Arrays.asList(ABIEncoder.mapABITypes(String.valueOf(argsDescriptor.charAt(charIdx))).identifiers).contains(pType)) {
                            charIdx ++;
                            parIdx ++;
                            continue;
                        }
                    }

                    matched = false;
                    break;
                }

                if (!matched || parIdx < parameterTypes.length || charIdx < argsDescriptor.length()) {
                    break;
                }

                return method;
            }
        }
        throw new ABICodecException("cannot find the method to call according to the transaction data");
    }

    /**
     * Convert the method call arguments to match with {@param method}
     * Input arguments are the user space IObject ones; {@param method} is also in the user space. 1D & 2D arrays should already match (both are wrapped).
     * However, the method parameter may be one of Java primitives. In this case, the argument needs to be converted from the shadow one to the primitive.
     * @param method A Java class method.
     * @param arguments the arguments to be converted to match with the method parameter types.
     * @return the converted arguments.
     */
    public static Object[] convertArguments(Method method, IObject[] arguments){
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (arguments.length != parameterTypes.length) {
            return null;
        }

        List<Object> argList = new LinkedList<>(Arrays.asList(arguments));
        for (int index = 0; index < argList.size(); index ++) {
            // replace the original object with the new one
            switch (parameterTypes[index].getName()) {
                case "byte":
                    argList.set(index, ((Byte)argList.get(index)).avm_byteValue());
                    break;
                case "boolean":
                    argList.set(index, ((Boolean)argList.get(index)).avm_booleanValue());
                    break;
                case "short":
                    argList.set(index, ((Short)argList.get(index)).avm_shortValue());
                    break;
                case "int":
                    argList.set(index, ((org.aion.avm.shadow.java.lang.Integer)argList.get(index)).avm_intValue());
                    break;
                case "long":
                    argList.set(index, ((org.aion.avm.shadow.java.lang.Long)argList.get(index)).avm_longValue());
                    break;
                case "float":
                    argList.set(index, ((Float)argList.get(index)).avm_floatValue());
                    break;
                case "double":
                    argList.set(index, ((org.aion.avm.shadow.java.lang.Double)argList.get(index)).avm_doubleValue());
                    break;
                case "char":
                    argList.set(index, ((org.aion.avm.shadow.java.lang.Character)argList.get(index)).avm_charValue());
                    break;
                default:
                    break;
            }
        }

        // return the array list
        return argList.toArray();
    }
}


