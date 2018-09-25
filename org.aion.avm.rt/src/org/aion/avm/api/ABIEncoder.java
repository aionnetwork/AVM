package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.ABICodecException;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.shadow.java.lang.Double;
import org.aion.avm.shadow.java.lang.Float;
import org.aion.avm.shadow.java.lang.Integer;
import org.aion.avm.shadow.java.lang.Long;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public final class ABIEncoder {
    public enum ABITypes{
        avm_BYTE    ('B', 1, new String[]{"B", "byte", "java.lang.Byte", PackageConstants.kShadowDotPrefix + "java.lang.Byte", PackageConstants.kArrayWrapperDotPrefix + "ByteArray", PackageConstants.kArrayWrapperDotPrefix + "ByteArray2D", "[B", "[[B"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                byte[][] nativeArray = new byte[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = (byte[]) data[i];
                }
                return new ByteArray2D(nativeArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                byte[][] array = new byte[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (byte[])data[i];
                }
                return array;
            }
        },
        avm_BOOLEAN ('Z', 1, new String[]{"Z", "boolean", "java.lang.Boolean", PackageConstants.kShadowDotPrefix + "java.lang.Boolean", "[Z", "[[Z"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                byte[][] bytes = new byte[data.length][];
                for (int i = 0; i < data.length; i++) {
                    bytes[i] = new byte[((boolean[]) data[i]).length];
                    for (int j = 0; j < ((boolean[]) data[i]).length; j++) {
                        bytes[i][j] = (byte) ((((boolean[])data[i])[j]) ? 1 : 0);
                    }
                }
                return new ByteArray2D(bytes);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                boolean[][] array = new boolean[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (boolean[])data[i];
                }
                return array;
            }
        },
        avm_CHAR    ('C', 0, new String[]{"C", "char", "java.lang.Character", PackageConstants.kShadowDotPrefix + "java.lang.Character", PackageConstants.kArrayWrapperDotPrefix + "CharArray", PackageConstants.kArrayWrapperDotPrefix + "CharArray2D", "[C", "[[C"}) { // variable length
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                char[][] nativeArray = new char[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = (char[]) data[i];
                }
                return new CharArray2D(nativeArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                char[][] array = new char[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (char[])data[i];
                }
                return array;
            }
        },
        avm_SHORT   ('S', 2, new String[]{"S", "short", "java.lang.Short", PackageConstants.kShadowDotPrefix + "java.lang.Short", PackageConstants.kArrayWrapperDotPrefix + "ShortArray", PackageConstants.kArrayWrapperDotPrefix + "ShortArray2D", "[S", "[[S"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                short[][] nativeArray = new short[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = (short[]) data[i];
                }
                return new ShortArray2D(nativeArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                short[][] array = new short[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (short[])data[i];
                }
                return array;
            }
        },
        avm_INT     ('I', 4, new String[]{"I", "int", "java.lang.Integer", PackageConstants.kShadowDotPrefix + "java.lang.Integer", PackageConstants.kArrayWrapperDotPrefix + "IntArray", PackageConstants.kArrayWrapperDotPrefix + "IntArray2D", "[I", "[[I"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                int[][] nativeArray = new int[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = (int[]) data[i];
                }
                return new IntArray2D(nativeArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                int[][] array = new int[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (int[])data[i];
                }
                return array;
            }
        },
        avm_LONG    ('L', 8, new String[]{"L", "long", "java.lang.Long", PackageConstants.kShadowDotPrefix + "java.lang.Long", PackageConstants.kArrayWrapperDotPrefix + "LongArray", PackageConstants.kArrayWrapperDotPrefix + "LongArray2D", "[J", "[[J"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                long[][] nativeArray = new long[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = (long[]) data[i];
                }
                return new LongArray2D(nativeArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                long[][] array = new long[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (long[])data[i];
                }
                return array;
            }
        },
        avm_FLOAT   ('F', 4, new String[]{"F", "float", "java.lang.Float", PackageConstants.kShadowDotPrefix + "PackageConstants.kShadowDotPrefix + \"java.lang.Float", PackageConstants.kArrayWrapperDotPrefix + "FloatArray", PackageConstants.kArrayWrapperDotPrefix + "FloatArray2D", "[F", "[[F"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                float[][] nativeArray = new float[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = (float[]) data[i];
                }
                return new FloatArray2D(nativeArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                float[][] array = new float[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (float[])data[i];
                }
                return array;
            }
        },
        avm_DOUBLE  ('D', 8, new String[]{"D", "double", "java.lang.Double", PackageConstants.kShadowDotPrefix + "java.lang.Double", PackageConstants.kArrayWrapperDotPrefix + "DoubleArray", PackageConstants.kArrayWrapperDotPrefix + "DoubleArray2D", "[D", "[[D"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
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
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                double[][] nativeArray = new double[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = (double[]) data[i];
                }
                return new DoubleArray2D(nativeArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                double[][] array = new double[data.length][];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (double[])data[i];
                }
                return array;
            }
        },
        avm_ADDRESS ('A', Address.avm_LENGTH, new String[]{"A", PackageConstants.kApiDotPrefix + "Address"}) {
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
            public Array construct1DWrappedArray(Object[] data) {
                return null;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                return null;
            }
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                return null;
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                return null;
            }
        },
        avm_String  ('T', 0, new String[]{"T", "java.lang.String", PackageConstants.kShadowDotPrefix + "java.lang.String", "[Ljava.lang.String;", PackageConstants.kArrayWrapperDotPrefix + "$L" + PackageConstants.kShadowDotPrefix + "java.lang.String"}) {
            @Override
            public byte[] encode(Object data) {
                return ((String) data).getBytes();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return ((org.aion.avm.shadow.java.lang.String) data).avm_getBytes().getUnderlying();
            }
            @Override
            public byte[][] encode1DArray(Object data) {
                return null;
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                return null;
            }
            @Override
            public Array construct1DWrappedArray(Object[] data) {
                return null;
            }
            @Override
            public Object constructNativeArray(Object[] data) {
                return null;
            }
            @Override
            public ObjectArray construct2DWrappedArray(Object[] data) {
                return null;
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
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

        /**
         * Encode one piece of the corresponding raw ABI-elementary-type data into a byte array.
         * @param data - one piece of the corresponding raw data, of the ABI elementary types, namely the native Java types or 'Address'
         * @return the encoded byte array, containing the encoded data only, without the descriptor
         */
        public abstract byte[] encode(Object data);

        /**
         * Encode one piece of the corresponding shadowed ABI-elementary-type data into a byte array.
         * @param data one piece of the shadowed ABI-elementary-type data, namely the corresponding 'org.aion.avm.shadow.java.lang.*' types or 'Address'
         * @return the encoded byte array, containing the encoded data only, without the descriptor
         */
        public abstract byte[] encodeShadowType(Object data);

        /**
         * Encode a 1D array of the raw data; generate the descriptor and encoded data. Not applicable to 'avm_ADDRESS'.
         * @param data a 1D array of the corresponding native java type
         * @return a 2-element 2D byte array, of which the first one is the descriptor, the second the encoded data
         */
        public abstract byte[][] encode1DArray(Object data);

        /**
         * Decode one piece of the ABI elementary type data, read from the 'startIndex' in the byte stream/array.
         * @param data the encoded byte stream/array
         * @param startIndex the starting byte index of the input encoded byte array to read from, inclusive
         * @return the 'DecodedObjectInfo', containing the decoded native object, the decoded shadow/wrapped object and the position in the encoded data byte buffer after this object is decoded.
         */
        public abstract ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex);

        /**
         * Construct the 1D array wrapper object from the native 1D array in "Object[]" type. Not applicable to 'avm_ADDRESS'.
         * @param data a 1D 'Object' array, which contains elements of the corresponding native Java type
         * @return the 1D array wrapper object
         */
        public abstract Array construct1DWrappedArray(Object[] data);

        /**
         * Construct the 1D array of corresponding Java native type from the 1D array in "Object[]" type. Not applicable to 'avm_ADDRESS'.
         * @param data a 1D 'Object' array, which contains elements of the corresponding native java type
         * @return the 1D array of corresponding Java native type
         */
        public abstract Object constructNativeArray(Object[] data);

        /**
         * Construct the 2D array wrapper object from the native Java 2D array. Not applicable to 'avm_ADDRESS'.
         * @param data a 2D Java native array
         * @return the 2D array wrapper object
         */
        public abstract ObjectArray construct2DWrappedArray(Object[] data);

        /**
         * Construct the 2D array of corresponding Java native type from the 2D array in "Object[]" type. Not applicable to 'avm_ADDRESS'.
         * @param data a 2D 'Object' array, which contains elements of the corresponding native java type
         * @return the 2D array of corresponding Java native type
         */
        public abstract Object construct2DNativeArray(Object[] data);
    }

    private static Map<String, ABITypes> ABITypesMap = null;

    /**
     * This class cannot be instantiated.
     */
    private ABIEncoder(){}

    /*
     * Runtime-facing implementation.
     */
    public static ByteArray avm_encodeMethodArguments(org.aion.avm.shadow.java.lang.String methodName, ObjectArray arguments)  {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ABIEncoder_avm_encodeMethodArguments);
        return new ByteArray(encodeMethodArguments(methodName.toString(), arguments.getUnderlying()));
    }

    public static ByteArray avm_encodeOneObject(IObject data) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ABIEncoder_avm_encodeOneObject);
        return new ByteArray(encodeOneObject(data));
    }


    /*
     * Underlying implementation.
     */

    /**
     * An utility method to encode the method name and method arguments to call with, according to Aion ABI format. Both method name and the arguments can be null if needed.
     * @param methodName the method name of the Dapp main class to call with.
     * @param arguments the arguments of the corresponding method of Dapp main class to call with.
     * @return the encoded byte array that contains the method descriptor, followed by the argument descriptor and encoded arguments, according the Aion ABI format.
     */
    public static byte[] encodeMethodArguments(String methodName, Object... arguments)  {
        if (arguments == null) {
            return methodName.getBytes();
        }

        // encode each argument
        byte[][][] encodedData = new byte[arguments.length][][];
        int numOfBytes = 0;
        for (int idx = 0; idx < arguments.length; idx++) {
            encodedData[idx] = encodeOneObjectAndDescriptor(arguments[idx]);
            if (encodedData[idx] != null) {
                numOfBytes += encodedData[idx][0].length + encodedData[idx][1].length;
            }
        }

        byte[] ret = new byte[(methodName + "<>").getBytes().length + numOfBytes];

        // copy the method name
        int pos = (methodName + "<").getBytes().length;
        System.arraycopy((methodName + "<").getBytes(), 0, ret, 0, pos);

        // copy the descriptors
        for (int idx = 0; idx < arguments.length; idx ++) {
            if (encodedData[idx] != null) {
                System.arraycopy(encodedData[idx][0], 0, ret, pos, encodedData[idx][0].length);
                pos += encodedData[idx][0].length;
            }
        }
        System.arraycopy(">".getBytes(), 0, ret, pos, ">".getBytes().length);
        pos += ">".getBytes().length;

        // copy the encoded data
        for (int idx = 0; idx < arguments.length; idx ++) {
            if (encodedData[idx] != null) {
                System.arraycopy(encodedData[idx][1], 0, ret, pos, encodedData[idx][1].length);
                pos += encodedData[idx][1].length;
            }
        }
        return ret;
    }

    /**
     * Encode one object of any type that Aion ABI allows; generate the byte array that contains the descriptor and the encoded data.
     * @param data one object of any type that Aion ABI allows
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneObject(Object data) {
        byte[][] encoded = encodeOneObjectAndDescriptor(data);
        byte[] ret = new byte[encoded[0].length + encoded[1].length];
        System.arraycopy(encoded[0], 0, ret, 0, encoded[0].length);
        System.arraycopy(encoded[1], 0, ret, encoded[0].length, encoded[1].length);
        return ret;
    }

    /**
     * Encode one object of any type that Aion ABI allows; generate the 2-element 2D byte array, of which the first byte array contains the descriptor,
     * and the second the encoded data.
     * @param data one object of any type that Aion ABI allows.
     * @return the 2-element 2D byte array, of which the first byte array contains the descriptor and the second the encoded data.
     */
    public static byte[][] encodeOneObjectAndDescriptor(Object data) {
        if (data == null) {
            return null;
        }
        String className = data.getClass().getName();

        if (className.startsWith(PackageConstants.kArrayWrapperDotPrefix + "") && className.endsWith("2D")) {
            // data is a 2D array
            return encode2DArray((ObjectArray)data, mapABITypes(className));
        }
        else {
            ABITypes type = mapABITypes(className);

            if (className.startsWith(PackageConstants.kArrayWrapperDotPrefix + "")) {
                // data is an 1D array
                return type.encode1DArray(((Array)data).getUnderlyingAsObject());
            }
            else if (className.startsWith("[[")) {
                return encode2DArray(type.construct2DWrappedArray((Object[]) data), type);
            }
            else if (className.startsWith("[")) {
                return type.encode1DArray(data);
            }
            else {
                // data should not be an array
                byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data

                if (type == ABITypes.avm_String) {
                    int length = className.startsWith(PackageConstants.kShadowDotPrefix) ?
                            ((org.aion.avm.shadow.java.lang.String) data).avm_length() :
                            ((String) data).length();
                    ret[0] = ("[" + type.symbol + length + "]").getBytes();
                } else {
                    ret[0] = Character.toString(type.symbol).getBytes();
                }

                if (className.startsWith(PackageConstants.kShadowDotPrefix + "java.lang.")) {
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

    /**
     * Encode a 2D wrapped array (PackageConstants.kArrayWrapperDotPrefix + "ObjectArray" class object); generate the 2-element 2D byte array,
     * of which the first byte array contains the descriptor and the second the encoded data.
     * @param data the PackageConstants.kArrayWrapperDotPrefix + "ObjectArray" class object
     * @param type the ABI type of the 2D array elements
     * @return the 2-element 2D byte array, of which the first byte array contains the descriptor and the second the encoded data.
     */
    public static byte[][] encode2DArray(ObjectArray data, ABITypes type) {
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

    /**
     * Return the corresponding ABI type of the given identifier, if it matches with one of the identifiers of the ABI type.
     * @param identifier a string that may be the class name, Java class file field descriptor, or the ABI symbol. See the {@link ABITypes} class.
     * @return the corresponding ABI type.
     * @throws ABICodecException the transaction data cannot be properly decoded, or cannot be converted to the method arguments
     */
    public static ABITypes mapABITypes(String identifier) {
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
            throw new ABICodecException("data type is not compatible to Aion ABI types");
        }
        return ABITypesMap.get(identifier);
    }
}