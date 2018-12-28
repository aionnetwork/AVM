package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.RuntimeMethodFeeSchedule;


public final class ABIEncoder {
    private static IABISupport ARRAY_FACTORY;

    /**
     * MUST be called to initialize the encoder's ability to create higher-dimension arrays before it is used.
     * This must only be called once.
     * Ideally, this is called by the same mechanism which is forcing the initialization of the shadow class library.
     * @param arrayFactory The factory to install (must NOT be null).
     */
    public static void initializeArrayFactory(IABISupport arrayFactory) {
        // Verify that we were only called once and that we weren't given null.
        RuntimeAssertionError.assertTrue(null == ARRAY_FACTORY);
        RuntimeAssertionError.assertTrue(null != arrayFactory);
        ARRAY_FACTORY = arrayFactory;
    }

    /**
     * This is provided for the convenience of some tests which need to synthesize incoming ABI-encoded data.
     * In the future, it might be removed if we can find a better way to share or abstract the factory.
     * 
     * @return The factory instance associated with the ABIEncoder class.
     */
    public static IABISupport testingFactoryAccess() {
        return ARRAY_FACTORY;
    }

    public enum ABITypes{
        avm_BYTE    ('B', 1, new String[]{"B", "byte", "java.lang.Byte", PackageConstants.kShadowDotPrefix + "java.lang.Byte",
                PackageConstants.kArrayWrapperDotPrefix + "ByteArray", PackageConstants.kArrayWrapperDotPrefix + "$$B", "[B", "[[B"}) {
            @Override
            public byte[] encode(Object data) {
                return new byte[]{(byte)data};
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Byte)data).avm_byteValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                // encoded data bytes
                byte[] encodedData = (byte[]) data;
                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(encodedData.length) + "]").getBytes(StandardCharsets.UTF_8);

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                return new ABIDecoder.DecodedObjectInfo(data[startIndex], new org.aion.avm.shadow.java.lang.Byte(data[startIndex]), startIndex + this.bytes);
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
                return ARRAY_FACTORY.construct2DByteArray(nativeArray);
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
        avm_BOOLEAN ('Z', 1, new String[]{"Z", "boolean", "java.lang.Boolean", PackageConstants.kShadowDotPrefix + "java.lang.Boolean",
                PackageConstants.kArrayWrapperDotPrefix + "BooleanArray", PackageConstants.kArrayWrapperDotPrefix + "$$Z", "[Z", "[[Z"}) {
            @Override
            public byte[] encode(Object data) {
                return new byte[]{(byte) (((java.lang.Boolean)data) ? 1 : 0)};
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Boolean)data).avm_booleanValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                boolean[] rawData = (boolean[]) data;

                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes(StandardCharsets.UTF_8);
                // encoded data bytes
                byte[] encodedData = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    encodedData[i] = (byte) ((rawData[i]) ? 1 : 0);
                }

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                return new ABIDecoder.DecodedObjectInfo(data[startIndex] != 0, new org.aion.avm.shadow.java.lang.Boolean(data[startIndex] != 0), startIndex + this.bytes);
            }
            @Override
            public Array construct1DWrappedArray(Object[] data) {
                BooleanArray array = new BooleanArray(data.length);
                for (int i = 0; i < data.length; i++) {
                    array.set(i, (boolean)data[i]);
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
                boolean[][] nativeArray = new boolean[data.length][];
                for (int i = 0; i < data.length; i++) {
                    nativeArray[i] = new boolean[((boolean[]) data[i]).length];
                    for (int j = 0; j < ((boolean[]) data[i]).length; j++) {
                        nativeArray[i][j] = ((boolean[])data[i])[j];
                    }
                }
                return ARRAY_FACTORY.construct2DBooleanArray(nativeArray);
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
        avm_CHAR('C', Character.BYTES, new String[]{"C", "char", "java.lang.Character", PackageConstants.kShadowDotPrefix + "java.lang.Character",
                PackageConstants.kArrayWrapperDotPrefix + "CharArray", PackageConstants.kArrayWrapperDotPrefix + "$$C", "[C", "[[C"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(this.bytes).putChar((char)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Character)data).avm_charValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                char[] rawData = (char[]) data;

                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes(StandardCharsets.UTF_8);
                // encoded data bytes
                byte[] encodedData = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, encodedData, i, bytes);
                }

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                char decoded = ByteBuffer.allocate(this.bytes).put(Arrays.copyOfRange(data, startIndex, startIndex + this.bytes)).getChar(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new org.aion.avm.shadow.java.lang.Character(decoded), startIndex + this.bytes);
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
                return ARRAY_FACTORY.construct2DCharArray(nativeArray);
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
        avm_SHORT('S', Short.BYTES, new String[]{"S", "short", "java.lang.Short", PackageConstants.kShadowDotPrefix + "java.lang.Short",
                PackageConstants.kArrayWrapperDotPrefix + "ShortArray", PackageConstants.kArrayWrapperDotPrefix + "$$S", "[S", "[[S"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(this.bytes).putShort((short)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Short)data).avm_shortValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                short[] rawData = (short[]) data;

                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes(StandardCharsets.UTF_8);
                // encoded data bytes
                byte[] encodedData = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, encodedData, i, bytes);
                }

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                short decoded = ByteBuffer.allocate(this.bytes).put(Arrays.copyOfRange(data, startIndex, startIndex + this.bytes)).getShort(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new org.aion.avm.shadow.java.lang.Short(decoded), startIndex + this.bytes);
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
                return ARRAY_FACTORY.construct2DShortArray(nativeArray);
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
        avm_INT('I', Integer.BYTES, new String[]{"I", "int", "java.lang.Integer", PackageConstants.kShadowDotPrefix + "java.lang.Integer",
                PackageConstants.kArrayWrapperDotPrefix + "IntArray", PackageConstants.kArrayWrapperDotPrefix + "$$I", "[I", "[[I"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(this.bytes).putInt((int)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Integer)data).avm_intValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                int[] rawData = (int[]) data;

                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes(StandardCharsets.UTF_8);
                // encoded data bytes
                byte[] encodedData = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, encodedData, i, bytes);
                }

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                int decoded = ByteBuffer.allocate(this.bytes).put(Arrays.copyOfRange(data, startIndex, startIndex + this.bytes)).getInt(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new org.aion.avm.shadow.java.lang.Integer(decoded), startIndex + this.bytes);
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
                return ARRAY_FACTORY.construct2DIntArray(nativeArray);
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
        avm_LONG('L', Long.BYTES, new String[]{"L", "long", "java.lang.Long", PackageConstants.kShadowDotPrefix + "java.lang.Long",
                PackageConstants.kArrayWrapperDotPrefix + "LongArray", PackageConstants.kArrayWrapperDotPrefix + "$$J", "[J", "[[J"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(this.bytes).putLong((long)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Long)data).avm_longValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                long[] rawData = (long[]) data;

                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes(StandardCharsets.UTF_8);
                // encoded data bytes
                byte[] encodedData = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, encodedData, i, bytes);
                }

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                long decoded = ByteBuffer.allocate(this.bytes).put(Arrays.copyOfRange(data, startIndex, startIndex + this.bytes)).getLong(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new org.aion.avm.shadow.java.lang.Long(decoded), startIndex + this.bytes);
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
                return ARRAY_FACTORY.construct2DLongArray(nativeArray);
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
        avm_FLOAT('F', Float.BYTES, new String[]{"F", "float", "java.lang.Float", PackageConstants.kShadowDotPrefix + "java.lang.Float",
                PackageConstants.kArrayWrapperDotPrefix + "FloatArray", PackageConstants.kArrayWrapperDotPrefix + "$$F", "[F", "[[F"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(this.bytes).putFloat((float)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Float)data).avm_floatValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                float[] rawData = (float[]) data;

                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes(StandardCharsets.UTF_8);
                // encoded data bytes
                byte[] encodedData = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, encodedData, i, bytes);
                }

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                float decoded = ByteBuffer.allocate(this.bytes).put(Arrays.copyOfRange(data, startIndex, startIndex + this.bytes)).getFloat(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new org.aion.avm.shadow.java.lang.Float(decoded), startIndex + this.bytes);
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
                return ARRAY_FACTORY.construct2DFloatArray(nativeArray);
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
        avm_DOUBLE('D', Double.BYTES, new String[]{"D", "double", "java.lang.Double", PackageConstants.kShadowDotPrefix + "java.lang.Double",
                PackageConstants.kArrayWrapperDotPrefix + "DoubleArray", PackageConstants.kArrayWrapperDotPrefix + "$$D", "[D", "[[D"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(this.bytes).putDouble((double)data).array();
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return encode(((org.aion.avm.shadow.java.lang.Double)data).avm_doubleValue());
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
                double[] rawData = (double[]) data;

                // descriptor bytes
                byte[] descriptor = ("[" + symbol + String.valueOf(rawData.length) + "]").getBytes(StandardCharsets.UTF_8);
                // encoded data bytes
                byte[] encodedData = new byte[rawData.length * bytes];
                for (int i = 0, idx = 0; idx < rawData.length; i += bytes, idx ++) {
                    System.arraycopy(encode(rawData[idx]), 0, encodedData, i, bytes);
                }

                return new EncodedObject(descriptor, encodedData);
            }
            @Override
            public ABIDecoder.DecodedObjectInfo decode(byte[] data, int startIndex) {
                double decoded = ByteBuffer.allocate(this.bytes).put(Arrays.copyOfRange(data, startIndex, startIndex + this.bytes)).getDouble(0);
                return new ABIDecoder.DecodedObjectInfo(decoded, new org.aion.avm.shadow.java.lang.Double(decoded), startIndex + this.bytes);
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
                return ARRAY_FACTORY.construct2DDoubleArray(nativeArray);
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
            public EncodedObject encode1DArray(Object data) {
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
        avm_String  ('T', 0, new String[]{"T", "java.lang.String", PackageConstants.kShadowDotPrefix + "java.lang.String",
                "[Ljava.lang.String;", PackageConstants.kArrayWrapperDotPrefix + "$Ljava.lang.String",
                PackageConstants.kArrayWrapperDotPrefix + "$L" + PackageConstants.kShadowDotPrefix + "java.lang.String",
                PackageConstants.kArrayWrapperDotPrefix + "interface._Ljava.lang.String",
                PackageConstants.kArrayWrapperDotPrefix + "interface._L" + PackageConstants.kShadowDotPrefix + "java.lang.String"}) {
            @Override
            public byte[] encode(Object data) {
                return ((String) data).getBytes(StandardCharsets.UTF_8);
            }
            @Override
            public byte[] encodeShadowType(Object data) {
                return ((org.aion.avm.shadow.java.lang.String) data).avm_getBytes().getUnderlying();
            }
            @Override
            public EncodedObject encode1DArray(Object data) {
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
                // this method actually constructs a 1D wrapped String array, which means,
                // 1) the type of it is "org.aion.avm.arraywrapper.$Lorg.aion.avm.shadow.java.lang.String";
                // 2) the type of each element is "org.aion.avm.shadow.java.lang.String"
                org.aion.avm.shadow.java.lang.String[] shadowArray = new org.aion.avm.shadow.java.lang.String[data.length];
                for (int i = 0; i < data.length; i++) {
                    shadowArray[i] = new org.aion.avm.shadow.java.lang.String((String) data[i]);
                }
                return ARRAY_FACTORY.construct1DStringArray(shadowArray);
            }
            @Override
            public Object construct2DNativeArray(Object[] data) {
                String[] array = new String[data.length];
                for (int i = 0; i < data.length; i++) {
                    array[i] = (String)data[i];
                }
                return array;
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
         * @return The encoded data.
         */
        public abstract EncodedObject encode1DArray(Object data);

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

    private static final Map<String, ABITypes> ABITypesMap;
    static {
        // create the map
        Map<String, ABITypes> map = new HashMap<>();
        for (ABITypes abiTypes : ABITypes.values()) {
            for (String id : abiTypes.identifiers) {
                map.put(id, abiTypes);
            }
        }
        ABITypesMap = Collections.unmodifiableMap(map);
    }

    /**
     * This class cannot be instantiated.
     */
    private ABIEncoder(){}

    /*
     * Runtime-facing implementation.
     */
    public static ByteArray avm_encodeMethodArguments(org.aion.avm.shadow.java.lang.String methodName, IObjectArray arguments)  {
        if ((null == methodName) || (null == arguments)) {
            throw new NullPointerException();
        }
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIEncoder_avm_encodeMethodArguments);
        return new ByteArray(encodeMethodArguments(methodName.toString(), ((ObjectArray) arguments).getUnderlying()));
    }

    public static ByteArray avm_encodeOneObject(IObject data) {
        if (null == data) {
            throw new NullPointerException();
        }
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.ABIEncoder_avm_encodeOneObject);
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
     * @throws NullPointerException If methodName or arguments are null (note that, under normal usage, arguments will be empty instead of null).
     */
    public static byte[] encodeMethodArguments(String methodName, Object... arguments)  {
        if ((null == methodName) || (null == arguments)) {
            throw new NullPointerException();
        }

        // encode each argument
        EncodedObject[] encodedData = new EncodedObject[arguments.length];
        int numOfBytes = 0;
        for (int idx = 0; idx < arguments.length; idx++) {
            encodedData[idx] = encodeOneObjectAndDescriptor(arguments[idx]);
            if (encodedData[idx] != null) {
                numOfBytes += encodedData[idx].descriptor.length + encodedData[idx].encodedData.length;
            }
        }

        byte[] ret = new byte[(methodName + "<>").getBytes(StandardCharsets.UTF_8).length + numOfBytes];

        // copy the method name
        int pos = (methodName + "<").getBytes(StandardCharsets.UTF_8).length;
        System.arraycopy((methodName + "<").getBytes(StandardCharsets.UTF_8), 0, ret, 0, pos);

        // copy the descriptors
        for (int idx = 0; idx < arguments.length; idx ++) {
            if (encodedData[idx] != null) {
                System.arraycopy(encodedData[idx].descriptor, 0, ret, pos, encodedData[idx].descriptor.length);
                pos += encodedData[idx].descriptor.length;
            }
        }
        System.arraycopy(">".getBytes(StandardCharsets.UTF_8), 0, ret, pos, ">".getBytes(StandardCharsets.UTF_8).length);
        pos += ">".getBytes(StandardCharsets.UTF_8).length;

        // copy the encoded data
        for (int idx = 0; idx < arguments.length; idx ++) {
            if (encodedData[idx] != null) {
                System.arraycopy(encodedData[idx].encodedData, 0, ret, pos, encodedData[idx].encodedData.length);
                pos += encodedData[idx].encodedData.length;
            }
        }
        return ret;
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
        EncodedObject encoded = encodeOneObjectAndDescriptor(data);
        byte[] ret = new byte[encoded.descriptor.length + encoded.encodedData.length];
        System.arraycopy(encoded.descriptor, 0, ret, 0, encoded.descriptor.length);
        System.arraycopy(encoded.encodedData, 0, ret, encoded.descriptor.length, encoded.encodedData.length);
        return ret;
    }

    /**
     * Encode one object of any type that Aion ABI allows; generate the 2-element 2D byte array, of which the first byte array contains the descriptor,
     * and the second the encoded data.
     * @param data one object of any type that Aion ABI allows.
     * @return The encoded data.
     */
    private static EncodedObject encodeOneObjectAndDescriptor(Object data) {
        String className = data.getClass().getName();
        ABITypes type = mapABITypes(className);

        if (className.startsWith(PackageConstants.kArrayWrapperDotPrefix + "$$")
            || className.contentEquals(PackageConstants.kArrayWrapperDotPrefix + "$Ljava.lang.String")
            || className.contentEquals(PackageConstants.kArrayWrapperDotPrefix + "$L" + PackageConstants.kShadowDotPrefix + "java.lang.String")) {
            // data is a 2D array, or a 1D String array
            return encode2DArray((ObjectArray)data, type);
        }
        else if (className.startsWith(PackageConstants.kArrayWrapperDotPrefix)) {
            // data is a 1D array
            return type.encode1DArray(((Array)data).getUnderlyingAsObject());
        }
        else if (className.startsWith("[[") || className.contentEquals("[Ljava.lang.String;")) {
            // data is a native 2D array, or a native 1D String array
            return encode2DArray(type.construct2DWrappedArray((Object[]) data), type);
        }
        else if (className.startsWith("[")) {
            // data is a native 1D array
            return type.encode1DArray(data);
        }
        else {
            // data should not be an array
            byte[] descriptor = null;

            if (type == ABITypes.avm_String) {
                int length = className.startsWith(PackageConstants.kShadowDotPrefix) ?
                        ((org.aion.avm.shadow.java.lang.String) data).avm_length() :
                        ((String) data).length();
                descriptor = ("[" + type.symbol + length + "]").getBytes(StandardCharsets.UTF_8);
            } else {
                descriptor = Character.toString(type.symbol).getBytes(StandardCharsets.UTF_8);
            }

            byte[] encodedData = null;
            if (className.startsWith(PackageConstants.kShadowDotPrefix + "java.lang.")) {
                encodedData = type.encodeShadowType(data);
            } else {
                // "java.lang.*" in this case. e.g. java.lang.[Integer|Byte|Boolean|Character|Short|Long|Float|Double]
                // This method is also used by ABIDecoder.decodeAndRun(), which can pass in the data returned by
                // method.invoke(), and this data can be of one of these "java.lang.*" types.
                encodedData = type.encode(data);
            }
            return new EncodedObject(descriptor, encodedData);
        }
    }

    /**
     * Encode a 2D wrapped array (PackageConstants.kArrayWrapperDotPrefix + "ObjectArray" class object); generate the 2-element 2D byte array,
     * of which the first byte array contains the descriptor and the second the encoded data.
     * @param data the PackageConstants.kArrayWrapperDotPrefix + "ObjectArray" class object
     * @param type the ABI type of the 2D array elements
     * @return The encoded data.
     */
    private static EncodedObject encode2DArray(ObjectArray data, ABITypes type) {
        Object[] underlying = data.getUnderlying();

        String argumentDescriptor = "[[" + type.symbol + "]" + String.valueOf(underlying.length) + "]";
        if (type == ABITypes.avm_String) {
            // descriptor bytes
            for (int i = 0; i < underlying.length; i ++) {
                argumentDescriptor += "(" + String.valueOf(((org.aion.avm.shadow.java.lang.String)underlying[i]).getUnderlying().length()) + ")";
            }

            // encoded data bytes
            String dataS = "";
            for (Object string : underlying) {
                dataS += string;
            }

            return new EncodedObject(argumentDescriptor.getBytes(StandardCharsets.UTF_8), dataS.getBytes(StandardCharsets.UTF_8));
        } else {
            int totalSize = 0;
            // descriptor bytes
            for (int i = 0; i < underlying.length; i++) {
                argumentDescriptor += "(" + String.valueOf(((Array) underlying[i]).length()) + ")";
                totalSize += ((Array) underlying[i]).length();
            }

            // encoded data bytes
            byte[] encodedData = new byte[totalSize * type.bytes];
            int i = 0;
            for (Object array : underlying) {
                for (int idx = 0; idx < ((Array) array).length(); i += type.bytes, idx++) {
                    System.arraycopy(type.encode(((Array) array).getAsObject(idx)), 0, encodedData, i, type.bytes);
                }
            }

            return new EncodedObject(argumentDescriptor.getBytes(StandardCharsets.UTF_8), encodedData);
        }
    }

    /**
     * Return the corresponding ABI type of the given identifier, if it matches with one of the identifiers of the ABI type.
     * @param identifier a string that may be the class name, Java class file field descriptor, or the ABI symbol. See the {@link ABITypes} class.
     * @return the corresponding ABI type.
     * @throws ABICodecException the transaction data cannot be properly decoded, or cannot be converted to the method arguments
     * @throws NullPointerException If identifier is null.
     */
    public static ABITypes mapABITypes(String identifier) {
        if (null == identifier) {
            throw new NullPointerException();
        }
        // return the type
        if (!ABITypesMap.containsKey(identifier)) {
            throw new ABICodecException("data type is not compatible to Aion ABI types");
        }
        return ABITypesMap.get(identifier);
    }


    /**
     * Describes a single object, in the encoded ABI format:  a descriptor describing the type and the serialized data.
     */
    private static class EncodedObject {
        public final byte[] descriptor;
        public final byte[] encodedData;
        
        public EncodedObject(byte[] descriptor, byte[] encodedData) {
            // Note that we never encode something null or "empty".
            RuntimeAssertionError.assertTrue(descriptor.length > 0);
            RuntimeAssertionError.assertTrue(encodedData.length > 0);
            
            this.descriptor = descriptor;
            this.encodedData = encodedData;
        }
    }
}
