package org.aion.avm.userlib.abi;

import org.aion.avm.api.Address;

import java.util.List;
import java.util.Map;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;

/**
 * Note that there are at least 2 versions of the type system which need to be considered to use this and a third
 * type is relevant when interacting with deployed DApps:
 * -ABI Type:  This is the type of the actual method signature including primitives, 1D and 2D primitive array,
 *  Strings, 1D String arrays, Addresses, and 1D Address arrays.
 * -Standard Type:  This is the type of the actual objects being passed around, meaning the ABI types with
 *  primitives replaced with box types (char becomes Character, etc).
 * -Internal Type:  This is the "shadow" type which the AVM uses at runtime.
 *
 * Examples of distinction:
 * ***************************************************
 * * ABI Type  * Standard Type  * Internal Type      *
 * ***************************************************
 * *  String   *  String        *  shadow.String     *
 * *  char     *  Character     *  shadow.Character  *
 * *  char[]   *  char[]        *  CharArray wrapper *
 * ***************************************************
 */
public class ABICodec {
    // TODO:  We will currently limit the buffer to 64 KiB but we must either make it a defined limit or add the ability to grow.
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * Used when decoding the ABI stream:  maps token identifier bytes to ABIToken instances.
     */
    private static final Map<Byte, ABIToken> TOKEN_MAP = new AionMap<>();

    static {
        for(ABIToken token : ABIToken.values()) {
            TOKEN_MAP.put(token.identifier, token);
        }
    }

    /**
     * Fully parses the given input data, returning a list of the Tuples defined by the data.
     * nre
     * @param data ABI-encoded bytes to interpret.
     * @return The list of Tuples found.
     * @throws NullPointerException The data was null.
     * @throws ABIException The data was malformed.
     */
    public static List<Tuple> parseEverything(byte[] data) throws NullPointerException, ABIException {
        if (null == data) {
            throw new NullPointerException();
        }
        try {
            List<Tuple> processedTuples = new AionList<>();

            if (0 == data.length) {
                return processedTuples;
            }

            AionBuffer buffer = AionBuffer.wrap(data);
            // This loop is the top-level, so it can see leaf types, array modifiers, and null modifiers.
            while (buffer.getPosition() != buffer.getLimit()) {
                ABIToken token = TOKEN_MAP.get(buffer.getByte());
                if(null == token) {
                    throw new ABIException("Failed to parse serialized data");
                }

                if (ABIToken.ARRAY == token) {
                    // Pull out the type and length, since they are "part" of this token.
                    ABIToken leafType = TOKEN_MAP.get(buffer.getByte());
                    if(!leafType.isLeafType) {
                        throw new ABIException("Array component type must be a leaf type");
                    }
                    int arrayLength = parseLength(buffer);
                    Tuple array = parseArray(leafType, arrayLength, buffer);
                    processedTuples.add(array);
                } else {
                    Tuple object = parseNonArray(token, buffer);
                    processedTuples.add(object);
                }
            }
            return processedTuples;
        } catch (Throwable t) {
            // If we fail to interpret this data, it is a codec exception.
            throw new ABIException("Failed to parse serialized data", t);
        }
    }

    /**
     * Serializes the given list of tuples as a stream of bytes.
     *
     * @param list The tuples to serialize.
     * @return The serialized data from the list, in the order specified.
     * @throws NullPointerException The list was null.
     * @throws ABIException The tuples in the list were malformed.
     */
    public static byte[] serializeList(List<Tuple> list) throws NullPointerException, ABIException {
        if (null == list) {
            throw new NullPointerException();
        }
        try {
            AionBuffer buffer = AionBuffer.allocate(BUFFER_SIZE);
            for (Tuple elt : list) {
                writeTupleToBuffer(buffer, elt);
            }

            // Convert this into a byte[] of the appropriate size;
            int length = buffer.getPosition();
            byte[] populated = new byte[length];
            System.arraycopy(buffer.getArray(), 0, populated, 0, populated.length);
            return populated;
        } catch (Throwable t) {
            // If we fail to serialize this data, it is a codec exception.
            throw new ABIException("Failed to serialize data", t);
        }
    }


    private static Tuple parseArray(ABIToken leafToken, int arrayLength, AionBuffer buffer) {
        Object[] array = initArray(leafToken, arrayLength);
        // Now, populate the required length.
        for (int i = 0; i < arrayLength; i++) {
            ABIToken token = TOKEN_MAP.get(buffer.getByte());
            Tuple object = parseNonArray(token, buffer);
            array[i] = object.value;
        }
        return new Tuple(array.getClass(), array);
    }

    private static Object[] initArray(ABIToken type, int arrayLength) {
        switch(type) {
            case A_BYTE:
                return new byte[arrayLength][];
            case A_BOOLEAN:
                return new boolean[arrayLength][];
            case A_CHAR:
                return new char[arrayLength][];
            case A_SHORT:
                return new short[arrayLength][];
            case A_INT:
                return new int[arrayLength][];
            case A_FLOAT:
                return new float[arrayLength][];
            case A_LONG:
                return new long[arrayLength][];
            case A_DOUBLE:
                return new double[arrayLength][];
            case STRING:
                return new String[arrayLength];
            case ADDRESS:
                return new Address[arrayLength];
            default:
                throw new ABIException("Unsupported array type");
        }
    }

    private static Tuple parseNonArray(ABIToken token, AionBuffer buffer) {
        Tuple result = null;
        // This CANNOT be an array but MAY still be null.
        if (ABIToken.NULL == token) {
            ABIToken targetToken = TOKEN_MAP.get(buffer.getByte());
            // Note that the only token types which can be null are the ones which DON'T have a primitive ABI type so verify that here.
            // (this null might actually be an array, so check that case, too).
            if (ABIToken.ARRAY == targetToken) {
                ABIToken trueTargetToken = TOKEN_MAP.get(buffer.getByte());
                // This type must NOT be primitive
                if(isPrimitive(token)) {
                    throw new ABIException("Array has invalid component type");
                }
                // Do some tricks to get the array type.
                Class<?> arrayClass = initArray(trueTargetToken, 0).getClass();
                // Tuples, strictly speaking, should only be instantiated with the standard types (although it shouldn't make a difference, in this case).
                result = new Tuple(arrayClass, null);
            } else {
                if(isPrimitive(token)) {
                    throw new ABIException("Null token described a primitive");
                }
                // Tuples, strictly speaking, should only be instantiated with the standard types (although it shouldn't make a difference, in this case).
                result = new Tuple(targetToken.standardType, null);
            }
        } else {
            // Interpret the meaning of the token and read its data from the buffer (we just know that this can't be the ARRAY token).
            Object parsedData = null;
            if (token.hasSizeField) {
                // This is some kind of array - a string or primitive array.
                int length = parseLength(buffer);
                switch (token) {
                    case STRING: {
                        byte[] data = new byte[length];
                        buffer.get(data);
                        parsedData = new String(data);
                        break;
                    }
                    case A_BYTE: {
                        byte[] data = new byte[length];
                        buffer.get(data);
                        parsedData = data;
                        break;
                    }
                    case A_BOOLEAN: {
                        boolean[] data = new boolean[length];
                        for (int i = 0; i < length; ++i) {
                            data[i] = ((byte)0 != buffer.getByte());
                        }
                        parsedData = data;
                        break;
                    }
                    case A_CHAR: {
                        char[] data = new char[length];
                        for (int i = 0; i < length; ++i) {
                            data[i] = buffer.getChar();
                        }
                        parsedData = data;
                        break;
                    }
                    case A_SHORT: {
                        short[] data = new short[length];
                        for (int i = 0; i < length; ++i) {
                            data[i] = buffer.getShort();
                        }
                        parsedData = data;
                        break;
                    }
                    case A_INT: {
                        int[] data = new int[length];
                        for (int i = 0; i < length; ++i) {
                            data[i] = buffer.getInt();
                        }
                        parsedData = data;
                        break;
                    }
                    case A_LONG: {
                        long[] data = new long[length];
                        for (int i = 0; i < length; ++i) {
                            data[i] = buffer.getLong();
                        }
                        parsedData = data;
                        break;
                    }
                    case A_FLOAT: {
                        float[] data = new float[length];
                        for (int i = 0; i < length; ++i) {
                            data[i] = buffer.getFloat();
                        }
                        parsedData = data;
                        break;
                    }
                    case A_DOUBLE: {
                        double[] data = new double[length];
                        for (int i = 0; i < length; ++i) {
                            data[i] = buffer.getDouble();
                        }
                        parsedData = data;
                        break;
                    }
                    default:
                        throw new ABIException("Unknown array token: " + token);
                }
            } else {
                // This is fixed-size - an address or primitive.
                switch (token) {
                    case ADDRESS: {
                        byte[] data = new byte[Address.LENGTH];
                        buffer.get(data);
                        parsedData = new org.aion.avm.api.Address(data);
                        break;
                    }
                    case BYTE: {
                        parsedData = buffer.getByte();
                        break;
                    }
                    case BOOLEAN: {
                        parsedData = (0 != buffer.getByte());
                        break;
                    }
                    case CHAR: {
                        parsedData = buffer.getChar();
                        break;
                    }
                    case SHORT: {
                        parsedData = buffer.getShort();
                        break;
                    }
                    case INT: {
                        parsedData = buffer.getInt();
                        break;
                    }
                    case LONG: {
                        parsedData = buffer.getLong();
                        break;
                    }
                    case FLOAT: {
                        parsedData = buffer.getFloat();
                        break;
                    }
                    case DOUBLE: {
                        parsedData = buffer.getDouble();
                        break;
                    }
                    default:
                        throw new ABIException("Unknown token: " + token);
                }
            }
            result = new Tuple(token.standardType, parsedData);
        }
        return result;
    }

    private static int parseLength(AionBuffer buffer) {
        // NOTE:  Lengths are big-endian 2-byte shorts.
        short length = buffer.getShort();
        return (int)length;
    }

    private static void writeTupleToBuffer(AionBuffer buffer, Tuple elt) {
        // Note that this Tuple was built from outside so we aren't sure what this type means (leaf type, array type, unknown type, etc) but it should be a standard type.

        if (ABIToken.STANDARD_LEAF_TYPE_MAP.containsKey(elt.standardType)) {
            // This is a direct leaf type (including a primitive array - they are caught here).
            writeNonArrayToBuffer(buffer, elt.standardType, elt.value);
        } else {
            // This might be an array or something unknown.
            Class<?> componentStandardType = getComponentType(elt.standardType);
            if(componentStandardType == null) {
                throw new ABIException("Unknown array type: " + componentStandardType.getName());
            }

            if (ABIToken.STANDARD_LEAF_TYPE_MAP.containsKey(componentStandardType)) {
                // We can write an array token and then the elements.
                Object[] cast = (Object[]) elt.value;
                // (note that it is possible we are seeing a null array so handle that here).
                if (null == cast) {
                    buffer.putByte(ABIToken.NULL.identifier);
                }
                // Now, write the standard array token and leaf type identifier.
                buffer.putByte(ABIToken.ARRAY.identifier);
                buffer.putByte(ABIToken.STANDARD_LEAF_TYPE_MAP.get(componentStandardType).identifier);
                // Proceed with length and contents if it was not null.
                if (null != cast) {
                    writeLength(buffer, cast.length);

                    // Now, walk the elements and handle them.
                    for (Object val : cast) {
                        writeNonArrayToBuffer(buffer, componentStandardType, val);
                    }
                }
            } else {
                // Unknown type - no message, due to uncertainty of portability.
                throw new IllegalArgumentException();
            }
        }
    }

    private static void writeNonArrayToBuffer(AionBuffer buffer, Class<?> standardType, Object value) {
        // Handle the null case, since that goes before the type.
        if (null == value) {
            buffer.putByte(ABIToken.NULL.identifier);
        }

        // Primitive types.
        if (ABIToken.BYTE.standardType == standardType) {
            buffer.putByte(ABIToken.BYTE.identifier);
            buffer.putByte((Byte) value);
        } else if (ABIToken.BOOLEAN.standardType == standardType) {
            buffer.putByte(ABIToken.BOOLEAN.identifier);
            buffer.putByte((byte)((Boolean) value ? 1 : 0));
        } else if (ABIToken.CHAR.standardType == standardType) {
            buffer.putByte(ABIToken.CHAR.identifier);
            buffer.putChar((Character) value);
        } else if (ABIToken.SHORT.standardType == standardType) {
            buffer.putByte(ABIToken.SHORT.identifier);
            buffer.putShort((Short) value);
        } else if (ABIToken.INT.standardType == standardType) {
            buffer.putByte(ABIToken.INT.identifier);
            buffer.putInt((Integer) value);
        } else if (ABIToken.LONG.standardType == standardType) {
            buffer.putByte(ABIToken.LONG.identifier);
            buffer.putLong((Long) value);
        } else if (ABIToken.FLOAT.standardType == standardType) {
            buffer.putByte(ABIToken.FLOAT.identifier);
            buffer.putFloat((Float) value);
        } else if (ABIToken.DOUBLE.standardType == standardType) {
            buffer.putByte(ABIToken.DOUBLE.identifier);
            buffer.putDouble((Double) value);

            // Primitive array types.
        } else if (ABIToken.A_BYTE.standardType == standardType) {
            buffer.putByte(ABIToken.A_BYTE.identifier);
            if (null != value) {
                byte[] cast = (byte[])value;
                writeLength(buffer, cast.length);
                buffer.put(cast);
            }
        } else if (ABIToken.A_BOOLEAN.standardType == standardType) {
            buffer.putByte(ABIToken.A_BOOLEAN.identifier);
            if (null != value) {
                boolean[] cast = (boolean[])value;
                writeLength(buffer, cast.length);
                byte[] ready = new byte[cast.length];
                for (int i = 0; i < ready.length; ++i) {
                    ready[i] = (byte)(cast[i] ? 1 : 0);
                }
                buffer.put(ready);
            }
        } else if (ABIToken.A_CHAR.standardType == standardType) {
            buffer.putByte(ABIToken.A_CHAR.identifier);
            if (null != value) {
                char[] cast = (char[])value;
                writeLength(buffer, cast.length);
                for (char c : cast) {
                    buffer.putChar(c);
                }
            }
        } else if (ABIToken.A_SHORT.standardType == standardType) {
            buffer.putByte(ABIToken.A_SHORT.identifier);
            if (null != value) {
                short[] cast = (short[])value;
                writeLength(buffer, cast.length);
                for (short s : cast) {
                    buffer.putShort(s);
                }
            }
        } else if (ABIToken.A_INT.standardType == standardType) {
            buffer.putByte(ABIToken.A_INT.identifier);
            if (null != value) {
                int[] cast = (int[])value;
                writeLength(buffer, cast.length);
                for (int i : cast) {
                    buffer.putInt(i);
                }
            }
        } else if (ABIToken.A_LONG.standardType == standardType) {
            buffer.putByte(ABIToken.A_LONG.identifier);
            if (null != value) {
                long[] cast = (long[])value;
                writeLength(buffer, cast.length);
                for (long l : cast) {
                    buffer.putLong(l);
                }
            }
        } else if (ABIToken.A_FLOAT.standardType == standardType) {
            buffer.putByte(ABIToken.A_FLOAT.identifier);
            if (null != value) {
                float[] cast = (float[])value;
                writeLength(buffer, cast.length);
                for (float f : cast) {
                    buffer.putFloat(f);
                }
            }
        } else if (ABIToken.A_DOUBLE.standardType == standardType) {
            buffer.putByte(ABIToken.A_DOUBLE.identifier);
            if (null != value) {
                double[] cast = (double[])value;
                writeLength(buffer, cast.length);
                for (double d : cast) {
                    buffer.putDouble(d);
                }
            }

            // Special types
        } else if (ABIToken.STRING.standardType == standardType) {
            buffer.putByte(ABIToken.STRING.identifier);
            if (null != value) {
                byte[] cast = ((String)value).getBytes();
                writeLength(buffer, cast.length);
                buffer.put(cast);
            }
        } else if (ABIToken.ADDRESS.standardType == standardType) {
            buffer.putByte(ABIToken.ADDRESS.identifier);
            if (null != value) {
                byte[] cast = ((org.aion.avm.api.Address)value).unwrap();
                if(Address.LENGTH != cast.length) {
                    throw new ABIException("Address was of unexpected length");
                }
                buffer.put(cast);
            }

        } else {
            // There should be no other kind of case.
            throw new ABIException("Unknown type in encoder");
        }
    }

    private static void writeLength(AionBuffer buffer, int length) {
        // NOTE:  Lengths are big-endian 2-byte shorts.
        if(length > (int)Short.MAX_VALUE) {
            throw new ABIException("Length must be 2 bytes");
        }
        buffer.putShort((short)length);
    }


    public static class Tuple {
        // We want to use the type of the value we have here, so that is always a standard type (note that this is required in the case of null).
        public final Class<?> standardType;
        // We store the value _as_ the public type, converting it only in the decoder, if required.
        public final Object value;
        public Tuple(Class<?> standardType, Object value) {
            // Note that the given type MUST be one of the defined standard types (or array of one of those types) and value must be null or of that type.
            boolean isValidType = ABIToken.STANDARD_LEAF_TYPE_MAP.containsKey(standardType)
                || isValidArray(standardType);
            boolean isInstanceOfType = (null == value) || (value.getClass() == standardType);
            if (!(isValidType && isInstanceOfType)) {
                // No message since this part of the spec.
                throw new IllegalArgumentException();
            }
            this.standardType = standardType;
            this.value = value;
        }
        @Override
        public String toString() {
            return standardType.getName() + "(" + this.value + ")";
        }
    }

    private static boolean isValidArray(Class type) {
        return getComponentType(type) != null;
    }

    private static Class getComponentType(Class type) {
        if(type.equals(byte[][].class)) {
            return byte[].class;
        }
        if(type.equals(boolean[][].class)) {
            return boolean[].class;
        }
        if(type.equals(char[][].class)) {
            return char[].class;
        }
        if(type.equals(short[][].class)) {
            return short[].class;
        }
        if(type.equals(int[][].class)) {
            return int[].class;
        }
        if(type.equals(long[][].class)) {
            return long[].class;
        }
        if(type.equals(float[][].class)) {
            return float[].class;
        }
        if(type.equals(double[][].class)) {
            return double[].class;
        }
        if(type.equals(String[].class)) {
            return String.class;
        }
        if(type.equals(Address[].class)) {
            return Address.class;
        }
        return null;
    }

    private static boolean isPrimitive(ABIToken typeToken) {
        switch (typeToken) {
            case BYTE:
                return true;
            case BOOLEAN:
                return true;
            case CHAR:
                return true;
            case SHORT:
                return true;
            case INT:
                return true;
            case LONG:
                return true;
            case FLOAT:
                return true;
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }
}
