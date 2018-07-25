package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Double;
import org.aion.avm.shadow.java.lang.Float;
import org.aion.avm.shadow.java.lang.Integer;
import org.aion.avm.shadow.java.lang.Long;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ABIEncoder{
    public enum ABITypes{
        avm_BYTE    ('B', 1, new String[]{"B", "byte", "java.lang.Byte", "org.aion.avm.shadow.java.lang.Byte", "org.aion.avm.arraywrapper.ByteArray", "[B", "[[B"}) {
            @Override
            public byte[] encode(Object data) {
                return new byte[]{(byte)data};
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Byte)data).avm_byteValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data

                // encoded data bytes
                ret[1] = (byte[]) data;
                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(ret[1].length) + "]").getBytes();

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                return new ABIDecoder.DecodedObjectInfo(data[startIndex], new org.aion.avm.shadow.java.lang.Byte(data[startIndex]), startIndex + 1);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                ByteArray array = new ByteArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (byte)data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                byte[] array = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (byte)data[i];
                }
                return array;
            }
        },
        avm_BOOLEAN ('Z', 1, new String[]{"Z", "boolean", "java.lang.Boolean", "org.aion.avm.shadow.java.lang.Boolean", "[Z", "[[Z"}) {
            @Override
            public byte[] encode(Object data) {
                return new byte[]{(byte) (((java.lang.Boolean)data) ? 1 : 0)};
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Boolean)data).avm_booleanValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data
                boolean[] rawData = (boolean[]) data;

                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes();
                // encoded data bytes
                ret[1] = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    ret[1][i] = (byte) ((rawData[i]) ? 1 : 0);
                }

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                return new ABIDecoder.DecodedObjectInfo(data[startIndex] != 0, new org.aion.avm.shadow.java.lang.Boolean(data[startIndex] != 0), startIndex + 1);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                ByteArray array = new ByteArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (byte)data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                boolean[] array = new boolean[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (boolean)data[i];
                }
                return array;
            }
        },
        avm_CHAR    ('C', 0, new String[]{"C", "char", "java.lang.Character", "org.aion.avm.shadow.java.lang.Character", "org.aion.avm.arraywrapper.CharArray", "[C", "[[C"}) { // variable length
            @Override
            public byte[] encode(Object data) {
                return Character.toString((char)data).getBytes();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Character)data).avm_charValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data
                char[] rawData = (char[]) data;

                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes();
                // encoded data bytes
                ret[1] = String.valueOf(rawData).getBytes();

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                char c = (new String(Arrays.copyOfRange(data, startIndex, data.length))).charAt(0);
                return new ABIDecoder.DecodedObjectInfo(c, new org.aion.avm.shadow.java.lang.Character(c), startIndex + String.valueOf(c).getBytes().length);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                CharArray array = new CharArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (char)data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                char[] array = new char[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (char)data[i];
                }
                return array;
            }
        },
        avm_SHORT   ('S', 2, new String[]{"S", "short", "java.lang.Short", "org.aion.avm.shadow.java.lang.Short", "org.aion.avm.arraywrapper.ShortArray", "[S", "[[S"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(2).putShort((short)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Short)data).avm_shortValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data
                short[] rawData = (short[]) data;

                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes();
                // encoded data bytes
                ret[1] = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, ret[1], i, bytes);
                }

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                short decoded = ByteBuffer.allocate(2).put(Arrays.copyOfRange(data, startIndex, startIndex + 2)).getShort(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new org.aion.avm.shadow.java.lang.Short(decoded), startIndex + 2);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                ShortArray array = new ShortArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (short)data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                short[] array = new short[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (short)data[i];
                }
                return array;
            }
        },
        avm_INT     ('I', 4, new String[]{"I", "int", "java.lang.Integer", "org.aion.avm.shadow.java.lang.Integer", "org.aion.avm.arraywrapper.IntArray", "[I", "[[I"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(4).putInt((int)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Integer)data).avm_intValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data
                int[] rawData = (int[]) data;

                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes();
                // encoded data bytes
                ret[1] = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, ret[1], i, bytes);
                }

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                int decoded = ByteBuffer.allocate(4).put(Arrays.copyOfRange(data, startIndex, startIndex + 4)).getInt(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new Integer(decoded), startIndex + 4);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                IntArray array = new IntArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (int)data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                int[] array = new int[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (int)data[i];
                }
                return array;
            }
        },
        avm_LONG    ('L', 8, new String[]{"L", "long", "java.lang.Long", "org.aion.avm.shadow.java.lang.Long", "org.aion.avm.arraywrapper.LongArray", "[J", "[[J"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(8).putLong((long)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Long)data).avm_longValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data
                long[] rawData = (long[]) data;

                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes();
                // encoded data bytes
                ret[1] = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, ret[1], i, bytes);
                }

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                long decoded = ByteBuffer.allocate(8).put(Arrays.copyOfRange(data, startIndex, startIndex + 8)).getLong(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new Long(decoded), startIndex + 8);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                LongArray array = new LongArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (long)data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                long[] array = new long[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (long)data[i];
                }
                return array;
            }
        },
        avm_FLOAT   ('F', 4, new String[]{"F", "float", "java.lang.Float", "org.aion.avm.shadow.java.lang.Float", "org.aion.avm.arraywrapper.FloatArray", "[F", "[[F"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(4).putFloat((float)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Float)data).avm_floatValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data
                float[] rawData = (float[]) data;

                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes();
                // encoded data bytes
                ret[1] = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, ret[1], i, bytes);
                }

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                float decoded = ByteBuffer.allocate(4).put(Arrays.copyOfRange(data, startIndex, startIndex + 4)).getFloat(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new Float(decoded), startIndex + 4);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                FloatArray array = new FloatArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (float) data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                float[] array = new float[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (float)data[i];
                }
                return array;
            }
        },
        avm_DOUBLE  ('D', 8, new String[]{"D", "double", "java.lang.Double", "org.aion.avm.shadow.java.lang.Double", "org.aion.avm.arraywrapper.DoubleArray", "[D", "[[D"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(8).putDouble((double)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Double)data).avm_doubleValue());
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data
                double[] rawData = (double[]) data;

                // descriptor bytes
                ret[0] = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes();
                // encoded data bytes
                ret[1] = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, ret[1], i, bytes);
                }

                return ret;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                double decoded = ByteBuffer.allocate(8).put(Arrays.copyOfRange(data, startIndex, startIndex + 8)).getDouble(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new Double(decoded), startIndex + 8);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                DoubleArray array = new DoubleArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (double)data[i]);
                }
                return array;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                double[] array = new double[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (double)data[i];
                }
                return array;
            }
        },
        avm_ADDRESS ('A', Address.avm_LENGTH, new String[]{"A", "org.aion.avm.api.Address"}) {
            @Override
            public byte[] encode(Object data) {
                return ((Address)data).unwrap();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(data);
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                return null;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                Address decoded = new Address(Arrays.copyOfRange(data, startIndex, startIndex + Address.avm_LENGTH));
                return new ABIDecoder.DecodedObjectInfo(decoded, decoded, startIndex + Address.avm_LENGTH);
            }
            @Override
            public Array constructWrappedArray(Object[] data) {
                return null;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                return null;
            }
        };

        public final char symbol;
        public final int  bytes;
        public final String[] identifiers;

        ABITypes(char symbol, int bytes, String[] identifiers) {
            this.symbol = symbol;
            this.bytes  = bytes;
            this.identifiers = identifiers;
        }

        public abstract byte[] encode(Object data);
        public abstract byte[] encodeShadowType(Object data);
        public abstract byte[][] encode1DArray(Object data);
        public abstract ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex);
        public abstract Array constructWrappedArray(Object[] data);
        public abstract Object constructNativeArray(Object[] data);
    }

    private static Map<String, ABITypes> ABITypesMap = null;

    /*
     * Runtime-facing implementation.
     */
    public static ByteArray avm_encodeMethodArguments(org.aion.avm.shadow.java.lang.String methodName, ObjectArray arguments)  throws InvalidTxDataException {
        return new ByteArray(encodeMethodArguments(methodName.toString(), arguments.getUnderlying()));
    }

    public static ByteArray avm_encodeOneObject(IObject data) throws InvalidTxDataException {
        return new ByteArray(encodeOneObject(data));
    }


    /*
     * Underlying implementation.
     */
    public static byte[] encodeMethodArguments(String methodName, Object... arguments)  throws InvalidTxDataException {
        if (arguments == null) {
            return methodName.getBytes();
        }

        // encode each argument
        byte[][][] encodedData = new byte[arguments.length][][];
        int numOfBytes = 0;
        for (int idx = 0; idx < arguments.length; idx++) {
            encodedData[idx] = encodeOneObjectAndDescriptor(arguments[idx]);
            numOfBytes += encodedData[idx][0].length + encodedData[idx][1].length;
        }

        byte[] ret = new byte[(methodName + "<>").getBytes().length + numOfBytes];

        // copy the method name
        int pos = (methodName + "<").getBytes().length;
        System.arraycopy((methodName + "<").getBytes(), 0, ret, 0, pos);

        // copy the descriptors
        for (int idx = 0; idx < arguments.length; pos += encodedData[idx][0].length, idx ++) {
            System.arraycopy(encodedData[idx][0], 0, ret, pos, encodedData[idx][0].length);
        }
        System.arraycopy(">".getBytes(), 0, ret, pos, ">".getBytes().length);
        pos += ">".getBytes().length;

        // copy the encoded data
        for (int idx = 0; idx < arguments.length; pos += encodedData[idx][1].length, idx ++) {
            System.arraycopy(encodedData[idx][1], 0, ret, pos, encodedData[idx][1].length);
        }
        return ret;
    }

    public static byte[] encodeOneObject(Object data) throws InvalidTxDataException {
        byte[][] encoded = encodeOneObjectAndDescriptor(data);
        byte[] ret = new byte[encoded[0].length + encoded[1].length];
        System.arraycopy(encoded[0], 0, ret, 0, encoded[0].length);
        System.arraycopy(encoded[1], 0, ret, encoded[0].length, encoded[1].length);
        return ret;
    }

    public static byte[][] encodeOneObjectAndDescriptor(Object data) throws InvalidTxDataException {
        String className = data.getClass().getName();

        if (className.equals("org.aion.avm.arraywrapper.ObjectArray")) {
            // data is a 2D array
            return encode2DArray((ObjectArray)data,
                    mapABITypes(((ObjectArray)data).getUnderlying().getClass().getName()));
        }
        else {
            ABITypes type = mapABITypes(className);
            if (type == null) {
                throw new InvalidTxDataException();
            }

            if (className.startsWith("org.aion.avm.arraywrapper.")) {
                // data is an 1D array
                return type.encode1DArray(((Array)data).getUnderlyingAsObject());
                //return encode1DArray((Array)data, type);
            }
            else if (className.startsWith("[[")) {
                return null;//TODO - implement this when 2D array wrappers are ready
            }
            else if (className.startsWith("[")) {
                return type.encode1DArray(data);
            }
            else {
                // data should not be an array
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data

                ret[0] = Character.toString(type.symbol).getBytes();
                if (className.startsWith("org.aion.avm.shadow.java.lang.")) {
                    ret[1] = type.encodeShadowType(data);
                } else {
                    // "java.lang.*" in this case. e.g. java.lang.[Integer|Byte|Boolean|Character|Short|Long|Float|Double]
                    // This method is also used by ABIDecoder.decodeAndRun(), which can pass in the data returned by
                    // method.invoke(), and this data can be of one of these "java.lang.*" types.
                    ret[1] = type.encode(data);
                }
                return ret;
            }
        }
    }

    public static byte[][] encode2DArray(ObjectArray data, ABITypes type) throws InvalidTxDataException {
        byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data

        Object[] underlying = data.getUnderlying();
        int totalSize = 0;

        // descriptor bytes
        String argumentDescriptor = "[[" + type.symbol + "]" + String.valueOf(underlying.length) + "]";
        for (int i = 0; i < underlying.length; i ++) {
            argumentDescriptor += "(" + String.valueOf(((Array)underlying[i]).length()) + ")";
            totalSize += ((Array)underlying[i]).length();
        }
        ret[0] = argumentDescriptor.getBytes();

        // encoded data bytes
        if (type == ABITypes.avm_CHAR) {
            String dataS = "";
            for (Object charArray : underlying) {
                dataS += String.valueOf(((CharArray)charArray).getUnderlying());
            }
            ret[1] = dataS.getBytes();
        }
        else {
            ret[1] = new byte[totalSize * type.bytes];
            int i = 0;
            for (Object array : underlying) {
                for (int idx = 0; idx < ((Array)array).length(); i += type.bytes, idx ++) {
                    System.arraycopy(type.encode(((Array)array).getAsObject(idx)), 0, ret[1], i, type.bytes);
                }
            }
        }

        return ret;
    }

    public static ABITypes mapABITypes(String identifier) throws InvalidTxDataException {
        // create the map, if not yet
        if (ABITypesMap == null) {
            ABITypesMap = new HashMap<>();
            for (ABITypes abiTypes : ABITypes.values()) {
                for (String id : abiTypes.identifiers) {
                    ABITypesMap.put(id, abiTypes);
                }
            }
        }

        // return the type
        if (!ABITypesMap.containsKey(identifier)) {
            throw new InvalidTxDataException();
        }
        return ABITypesMap.get(identifier);
    }
}