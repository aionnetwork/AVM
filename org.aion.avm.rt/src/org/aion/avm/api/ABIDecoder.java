package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Boolean;
import org.aion.avm.shadow.java.lang.Byte;
import org.aion.avm.shadow.java.lang.Float;
import org.aion.avm.shadow.java.lang.Short;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ABIDecoder {
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
        public ABIEncoder.ABITypes type;// elementary types defined for AION contract ABI
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


    /*
     * Runtime-facing implementation.
     */
    public static ByteArray avm_decodeAndRun(IObject obj, ByteArray txData) throws InvalidTxDataException{
        byte[] result = decodeAndRun(obj, txData.getUnderlying());
        return (null != result)
                ? new ByteArray(result)
                : null;
    }

    public static MethodCaller avm_decode(ByteArray txData) throws InvalidTxDataException{
        return decode(txData.getUnderlying());
    }

    public static ObjectArray avm_decodeArguments(ByteArray txData) throws InvalidTxDataException{
        Object[] result = decodeArguments(txData.getUnderlying());
        return (null != result)
                ? new ObjectArray(result)
                : null;
    }

    public static IObject avm_decodeOneObject(ByteArray txData) throws InvalidTxDataException{
        Descriptor descriptor = readOneDescriptor(txData.getUnderlying(), 0);
        return decodeOneObjectWithDescriptor(txData.getUnderlying(), descriptor.encodedBytes, descriptor).iObject;
    }


    /*
     * Underlying implementation.
     */

    public static byte[] decodeAndRun(Object obj, byte[] txData) throws InvalidTxDataException{
        MethodCaller methodCaller = decode(txData);

        String newMethodName = "avm_" + methodCaller.methodName;
        String newArgDescriptor = methodCaller.argsDescriptor;

        // generate the method descriptor of each main class method, compare to the method selector to select or invalidate the txData
        Method method = matchMethodSelector(obj.getClass(), newMethodName, newArgDescriptor);

        Object ret;
        if (Modifier.isStatic(method.getModifiers())) {
            obj = null;
        }
        try {
            if (methodCaller.arguments == null) {
                ret = method.invoke(obj);
            }
            else {
                ret = method.invoke(obj, convertArguments(method, methodCaller.arguments));
            }
        } catch (Exception e) {
            throw new InvalidTxDataException();
        }

        return (null != ret)
                ? ABIEncoder.encodeOneObject(ret)
                : null;
    }

    /**
     * Decoded arguments are the user space IObject ones.
     * @param txData
     * @return
     * @throws InvalidTxDataException
     */
    public static MethodCaller decode(byte[] txData) throws InvalidTxDataException{
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
            throw new InvalidTxDataException();
        }

        String methodName = decoded.substring(0, m1);
        String argsDescriptor = decoded.substring(m1+1, m2);
        int startByteOfData = decoded.substring(0, m2 + 1).getBytes().length;

        IObject[] arguments = decodeArgumentsWithDescriptor(Arrays.copyOfRange(txData, startByteOfData, txData.length), argsDescriptor);

        return new MethodCaller(methodName, argsDescriptor, arguments);
    }

    private static IObject[] decodeArgumentsWithDescriptor(byte[] data, String argsDescriptor) throws InvalidTxDataException{
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

    public static Object[] decodeArguments(byte[] data) throws InvalidTxDataException{
        return decode(data).arguments;
    }

    /**
     * Return the native object
     * @param data
     * @return
     * @throws InvalidTxDataException
     */
    public static Object decodeOneObject(byte[] data) throws InvalidTxDataException{
        Descriptor descriptor = readOneDescriptor(data, 0);
        return decodeOneObjectWithDescriptor(data, descriptor.encodedBytes, descriptor).object;
    }

    /**
     * A helper method to read one argument descriptor from the input data, starting from its index "start".
     * All possible characters are encoded in UTF-8 as one byte for each. So the character index in the string
     * is equal to the byte index in the byte array.
     */
    private static Descriptor readOneDescriptor(byte[] data, int start) throws InvalidTxDataException{
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
     * A helper method to read one number from the array arguments descriptor.
     */
    private static int[] readNumberFromDescriptor(String argsDescriptor, char stopChar, int startIdx) throws InvalidTxDataException {
        int[] res = new int[2]; // res[0]: the number encoded as argsDescriptor.substring(startIdx, idxE); res[1]: the index of stopChar in the argsDescriptor
        int idxE = argsDescriptor.indexOf(stopChar, startIdx);
        if ( idxE == -1) {
            throw new InvalidTxDataException();
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
                throw new InvalidTxDataException();
            }
        }
        return res;
    }

    private static DecodedObjectInfo decodeOneObjectWithDescriptor(byte[] data, int startByteOfData, Descriptor descriptor) throws InvalidTxDataException{
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

    private static DecodedObjectInfo decode1DArray(byte[] data, int startByteOfData, Descriptor descriptor) throws InvalidTxDataException{
        int endByte = startByteOfData;
        Object[] array = new Object[descriptor.size];
        Descriptor componentDescriptor = new Descriptor(descriptor.type, 0, 1);
        for (int idx = 0; idx < descriptor.size; idx ++) {
            DecodedObjectInfo decodedObjectInfo = decodeOneObjectWithDescriptor(data, endByte, componentDescriptor);
            array[idx] = decodedObjectInfo.object;
            endByte = decodedObjectInfo.endByteOfData;
        }
        return new DecodedObjectInfo(descriptor.type.constructNativeArray(array), descriptor.type.constructWrappedArray(array), endByte);
    }

    private static DecodedObjectInfo decode2DArray(byte[] data, int startByteOfData, Descriptor descriptor) throws InvalidTxDataException{
        int endByte = startByteOfData;
        Object[][] array = new Object[descriptor.size][];
        for (int idx = 0; idx < descriptor.size; idx ++) {
            DecodedObjectInfo decodedObjectInfo = decode1DArray(data, endByte, descriptor);
            array[idx] = (Object[]) decodedObjectInfo.object;
            endByte = decodedObjectInfo.endByteOfData;
        }
        return new DecodedObjectInfo(array, new ObjectArray(array), endByte); //TODO - constructWrapped2DArray after the 2D wrappers are available
    }

    /**
     * A helper method to match the method selector with the main-class methods.
     */
    public static Method matchMethodSelector(Class<?> clazz, String methodName, String argsDescriptor) throws InvalidTxDataException{
        Method[] methods = clazz.getMethods();

        // We only allow Java primitive types or 1D/2D array of the primitive types in the parameter list.
        Map<Character, String[]> elementaryTypesMap = new HashMap<>();
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_BYTE.symbol,      new String[]{"B", "byte", "ByteArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_BOOLEAN.symbol,   new String[]{"Z", "boolean", "ByteArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_CHAR.symbol,      new String[]{"C", "char", "CharArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_SHORT.symbol,     new String[]{"S", "short", "ShortArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_INT.symbol,       new String[]{"I", "int", "IntArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_FLOAT.symbol,     new String[]{"F", "float", "FloatArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_LONG.symbol,      new String[]{"J", "long", "LongArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_DOUBLE.symbol,    new String[]{"D", "double", "DoubleArray"});
        elementaryTypesMap.put(ABIEncoder.ABITypes.avm_ADDRESS.symbol,   ABIEncoder.ABITypes.avm_ADDRESS.identifiers);

        String ARRAY_WRAPPER_PREFIX = "org.aion.avm.arraywrapper.";

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if ((parameterTypes == null || parameterTypes.length == 0) && (argsDescriptor==null || argsDescriptor.isEmpty())) {
                    return method;
                }
                if (argsDescriptor == null || argsDescriptor.isEmpty()) {
                    break;
                }

                boolean matched = true;
                int parIdx = 0;

                for (int idx = 0; idx < argsDescriptor.length(); idx++) {
                    if (argsDescriptor.charAt(idx) == ABIDecoder.ARRAY_S) {
                        String pType = parameterTypes[parIdx].getName();
                        if (pType.charAt(0) == '[') {
                            pType = pType.substring(1);
                        } else if (pType.startsWith(ARRAY_WRAPPER_PREFIX)) {
                            pType = pType.substring(ARRAY_WRAPPER_PREFIX.length());
                        } else {
                            matched = false;
                            break;
                        }

                        if (argsDescriptor.length() - idx < 2) {
                            matched = false;
                            break;
                        }

                        char eType;
                        if (argsDescriptor.charAt(++idx) == ABIDecoder.ARRAY_S) {
                            if (pType.charAt(0) == '$' && pType.charAt(1) == '$') {
                                pType = pType.substring(2);
                            }
                            else {
                                matched = false;
                                break;
                            }
                            eType = argsDescriptor.charAt(++idx);
                            idx = argsDescriptor.indexOf(ABIDecoder.ARRAY_E, idx);
                        }
                        else {
                            eType = argsDescriptor.charAt(idx);
                        }
                        idx = argsDescriptor.indexOf(ABIDecoder.ARRAY_E, idx);

                        if (pType.charAt(0) == 'L') {
                            pType = pType.substring(1);
                        }

                        if (!(Arrays.asList(elementaryTypesMap.get(eType)).contains(pType))) {
                            matched = false;
                            break;
                        }
                    }
                    else {
                        if (!(Arrays.asList(elementaryTypesMap.get(argsDescriptor.charAt(idx))).contains(parameterTypes[parIdx].getName()))) {
                            matched = false;
                            break;
                        }
                    }
                    if (!matched) {
                        break;
                    }
                    else {
                        parIdx ++;
                        if (parIdx == parameterTypes.length) {
                            break;
                        }
                    }
                }
                if (matched && parIdx == parameterTypes.length) {
                    return method;
                }
            }
        }
        return null;
    }


    /**
     * Convert the method call arguments to match with {@param method}
     * Input arguments are the user space IObject ones; {@param method} is also in the user space. 1D & 2D arrays should already match (both are wrapped)
     * However, the method parameter may be one of Java primitives. In this case, the argument needs to be converted from the shadow one to the primitive.
     *
     * @param method
     * @param arguments
     * @return
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


