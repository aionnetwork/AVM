package org.aion.avm.userlib.abi;

import java.util.List;
import org.aion.avm.api.Address;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.abi.ABICodec.Tuple;

/**
 * Utility class for AVM ABI encoding. This class contains static methods
 * for generating transaction data from method name and arguments.
 */
public final class ABIEncoder {
    /**
     * This class cannot be instantiated.
     */
    private ABIEncoder(){}

    private static final int BYTE_MASK = 0xff;

    /**
     * Encode one object of any type that Aion ABI allows; generate the byte array that contains the descriptor and the encoded data. Null data is encoded as null.
     * @param data one object of any type that Aion ABI allows
     * @return the byte array that contains the argument descriptor and the encoded data.
     * @throws NullPointerException If data is null.
     */
    public static byte[] encodeOneObject(Object data) {
        // temporary: will be changed to encoding NULL of the appropriate type
        if (null == data) {
            return null;
        }
        List<Tuple> list = new AionList<>();
        list.add(new ABICodec.Tuple(data.getClass(), data));
        return ABICodec.serializeList(list);
    }

    /**
     * A utility method to encode the method name and method arguments to call with, according to Aion ABI format.
     * <br>
     * The arguments parameter can behave unexpectedly when receiving multi-dimensional primitive arrays and arrays of objects. In these cases, it is recommended to explicitly cast the arguments into an Object[].
     * @param methodName the method name of the Dapp main class to call with
     * @param arguments the arguments of the corresponding method of Dapp main class to call with
     * @return the encoded byte array that contains the method descriptor, followed by the argument descriptor and encoded arguments, according the Aion ABI format.
     * @throws NullPointerException If methodName or arguments are null (note that, under normal usage, arguments will be empty instead of null).
     */
    public static byte[] encodeMethodArguments(String methodName, Object... arguments) {
        if ((null == methodName) || (null == arguments)) {
            throw new NullPointerException();
        }

        List<ABICodec.Tuple> tuplesToEncode = new AionList<>();
        tuplesToEncode.add(new ABICodec.Tuple(String.class, methodName));
        return encodeArgumentsInList(tuplesToEncode, arguments);
    }

    /**
     * A utility method to encode a list of arguments for deployment.
     * Note that encoding no arguments will return an empty byte[].
     *
     * @param arguments the arguments in the order they should be decoded during deployment
     * @return the encoded byte array that contains the encoded arguments, according the Aion ABI format.
     * @throws NullPointerException If arguments are null (either the array or any specific elements).
     */
    public static byte[] encodeDeploymentArguments(Object... arguments) {
        if (null == arguments) {
            throw new NullPointerException();
        }

        List<ABICodec.Tuple> tuplesToEncode = new AionList<>();
        return encodeArgumentsInList(tuplesToEncode, arguments);
    }

    private static byte[] encodeArgumentsInList(List<ABICodec.Tuple> tuplesToEncode, Object... arguments) {
        for (Object arg : arguments) {
            // We sniff the type, directly, since there are no nulls in this path.
            tuplesToEncode.add(new ABICodec.Tuple(arg.getClass(), arg));
        }
        return ABICodec.serializeList(tuplesToEncode);
    }

    /**
     * Encode one byte; generate the byte array that contains the descriptor and the encoded data.
     * @param data one byte
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneByte(byte data) {
        byte[] result = new byte[Byte.BYTES + 1];
        result[0] = ABIToken.BYTE.identifier;
        result[1] = data;
        return result;
    }

    /**
     * Encode one boolean; generate the byte array that contains the descriptor and the encoded data.
     * @param data one boolean
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneBoolean(boolean data) {
        byte[] result = new byte[Byte.BYTES + 1];
        result[0] = ABIToken.BOOLEAN.identifier;
        result[1] = (byte)(data ? 1 : 0);
        return result;
    }

    /**
     * Encode one character; generate the byte array that contains the descriptor and the encoded data.
     * @param data one character
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneCharacter(char data) {
        byte[] result = new byte[Character.BYTES + 1];
        result[0] = ABIToken.CHAR.identifier;
        result[1]  = (byte) ((data >> 8) & BYTE_MASK);
        result[2] = (byte) (data & BYTE_MASK);
        return result;
    }

    /**
     * Encode one short; generate the byte array that contains the descriptor and the encoded data.
     * @param data one short
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneShort(short data) {
        byte[] result = new byte[Short.BYTES + 1];
        result[0] = ABIToken.SHORT.identifier;
        result[1]  = (byte) ((data >> 8) & BYTE_MASK);
        result[2] = (byte) (data & BYTE_MASK);
        return result;
    }

    /**
     * Encode one integer; generate the byte array that contains the descriptor and the encoded data.
     * @param data one integer
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneInteger(int data) {
        byte[] result = new byte[Integer.BYTES + 1];
        result[0] = ABIToken.INT.identifier;
        result[1]  = (byte) ((data >> 24) & BYTE_MASK);
        result[2]  = (byte) ((data >> 16) & BYTE_MASK);
        result[3]  = (byte) ((data >> 8) & BYTE_MASK);
        result[4] = (byte) (data & BYTE_MASK);
        return result;
    }

    /**
     * Encode one long; generate the byte array that contains the descriptor and the encoded data.
     * @param data one long
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneLong(long data) {
        byte[] result = new byte[Long.BYTES + 1];
        result[0] = ABIToken.LONG.identifier;
        result[1]  = (byte) ((data >> 56) & BYTE_MASK);
        result[2]  = (byte) ((data >> 48) & BYTE_MASK);
        result[3]  = (byte) ((data >> 40) & BYTE_MASK);
        result[4]  = (byte) ((data >> 32) & BYTE_MASK);
        result[5]  = (byte) ((data >> 24) & BYTE_MASK);
        result[6]  = (byte) ((data >> 16) & BYTE_MASK);
        result[7]  = (byte) ((data >> 8) & BYTE_MASK);
        result[8] = (byte) (data & BYTE_MASK);
        return result;
    }

    /**
     * Encode one float; generate the byte array that contains the descriptor and the encoded data.
     * @param data one float
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneFloat(float data) {
        byte[] result = new byte[Float.BYTES + 1];
        int dataBits = Float.floatToRawIntBits(data);
        result[0] = ABIToken.FLOAT.identifier;
        result[1]  = (byte) ((dataBits >> 24) & BYTE_MASK);
        result[2]  = (byte) ((dataBits >> 16) & BYTE_MASK);
        result[3]  = (byte) ((dataBits >> 8) & BYTE_MASK);
        result[4] = (byte) (dataBits & BYTE_MASK);
        return result;
    }

    /**
     * Encode one double; generate the byte array that contains the descriptor and the encoded data.
     * @param data one double
     * @return the byte array that contains the argument descriptor and the encoded data.
     */
    public static byte[] encodeOneDouble(double data) {
        byte[] result = new byte[Double.BYTES + 1];
        long dataBits = Double.doubleToRawLongBits(data);
        result[0] = ABIToken.DOUBLE.identifier;
        result[1]  = (byte) ((dataBits >> 56) & BYTE_MASK);
        result[2]  = (byte) ((dataBits >> 48) & BYTE_MASK);
        result[3]  = (byte) ((dataBits >> 40) & BYTE_MASK);
        result[4]  = (byte) ((dataBits >> 32) & BYTE_MASK);
        result[5]  = (byte) ((dataBits >> 24) & BYTE_MASK);
        result[6]  = (byte) ((dataBits >> 16) & BYTE_MASK);
        result[7]  = (byte) ((dataBits >> 8) & BYTE_MASK);
        result[8] = (byte) (dataBits & BYTE_MASK);
        return result;
    }

    /**
     * Encode one byte array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one byte array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_BYTE
     */
    public static byte[] encodeOneByteArray(byte[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_BYTE.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length + Short.BYTES + 1];
            result[0] = ABIToken.A_BYTE.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            System.arraycopy(data, 0, result, 3, data.length);
        }
        return result;
    }

    /**
     * Encode one boolean array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one boolean array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_BOOLEAN
     */
    public static byte[] encodeOneBooleanArray(boolean[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_BOOLEAN.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length + Short.BYTES + 1];
            result[0] = ABIToken.A_BOOLEAN.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            for (int i = 0, j = 3; i < data.length; i++, j++) {
                result[j] = (byte)(data[i] ? 1 : 0);
            }
        }
        return result;
    }

    /**
     * Encode one character array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one character array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_CHAR
     */
    public static byte[] encodeOneCharacterArray(char[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_CHAR.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length * Character.BYTES + Short.BYTES + 1];
            result[0] = ABIToken.A_CHAR.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            for (int i = 0, j = 3; i < data.length; i++, j+=Character.BYTES) {
                result[j]  = (byte) ((data[i] >> 8) & BYTE_MASK);
                result[j+1]  = (byte) (data[i] & BYTE_MASK);
            }
        }
        return result;
    }

    /**
     * Encode one short array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one short array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_SHORT
     */
    public static byte[] encodeOneShortArray(short[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_SHORT.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length * Short.BYTES + Short.BYTES + 1];
            result[0] = ABIToken.A_SHORT.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            for (int i = 0, j = 3; i < data.length; i++, j+=Short.BYTES) {
                result[j]  = (byte) ((data[i] >> 8) & BYTE_MASK);
                result[j+1]  = (byte) (data[i] & BYTE_MASK);
            }
        }
        return result;
    }

    /**
     * Encode one integer array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one integer array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_INT
     */
    public static byte[] encodeOneIntegerArray(int[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_INT.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length * Integer.BYTES + Short.BYTES + 1];
            result[0] = ABIToken.A_INT.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            for (int i = 0, j = 3; i < data.length; i++, j+=Integer.BYTES) {
                result[j]  = (byte) ((data[i] >> 24) & BYTE_MASK);
                result[j+1]  = (byte) ((data[i] >> 16) & BYTE_MASK);
                result[j+2]  = (byte) ((data[i] >> 8) & BYTE_MASK);
                result[j+3] = (byte) (data[i] & BYTE_MASK);
            }
        }
        return result;
    }

    /**
     * Encode one long array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one long array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_LONG
     */
    public static byte[] encodeOneLongArray(long[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_LONG.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length * Long.BYTES + Short.BYTES + 1];
            result[0] = ABIToken.A_LONG.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            for (int i = 0, j = 3; i < data.length; i++, j+=Long.BYTES) {
                result[j]  = (byte) ((data[i] >> 56) & BYTE_MASK);
                result[j+1]  = (byte) ((data[i] >> 48) & BYTE_MASK);
                result[j+2]  = (byte) ((data[i] >> 40) & BYTE_MASK);
                result[j+3]  = (byte) ((data[i] >> 32) & BYTE_MASK);
                result[j+4]  = (byte) ((data[i] >> 24) & BYTE_MASK);
                result[j+5]  = (byte) ((data[i] >> 16) & BYTE_MASK);
                result[j+6]  = (byte) ((data[i] >> 8) & BYTE_MASK);
                result[j+7]  = (byte) (data[i] & BYTE_MASK);
            }
        }
        return result;
    }

    /**
     * Encode one float array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one float array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_FLOAT
     */
    public static byte[] encodeOneFloatArray(float[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_FLOAT.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length * Float.BYTES + Short.BYTES + 1];
            result[0] = ABIToken.A_FLOAT.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            for (int i = 0, j = 3; i < data.length; i++, j+=Float.BYTES) {
                int dataBits = Float.floatToRawIntBits(data[i]);
                result[j]  = (byte) ((dataBits >> 24) & BYTE_MASK);
                result[j+1]  = (byte) ((dataBits >> 16) & BYTE_MASK);
                result[j+2]  = (byte) ((dataBits >> 8) & BYTE_MASK);
                result[j+3]  = (byte) (dataBits & BYTE_MASK);
            }
        }
        return result;
    }

    /**
     * Encode one double array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one double array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by A_DOUBLE
     */
    public static byte[] encodeOneDoubleArray(double[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.A_DOUBLE.identifier;
        } else {
            if (data.length > Short.MAX_VALUE) {
                throw new ABIException("Array length must fit in 2 bytes");
            }
            result = new byte[data.length * Double.BYTES + Short.BYTES + 1];
            result[0] = ABIToken.A_DOUBLE.identifier;
            result[1] = (byte) ((data.length >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length & BYTE_MASK);
            for (int i = 0, j = 3; i < data.length; i++, j+=Double.BYTES) {
                long dataBits = Double.doubleToRawLongBits(data[i]);
                result[j]  = (byte) ((dataBits >> 56) & BYTE_MASK);
                result[j+1]  = (byte) ((dataBits >> 48) & BYTE_MASK);
                result[j+2]  = (byte) ((dataBits >> 40) & BYTE_MASK);
                result[j+3]  = (byte) ((dataBits >> 32) & BYTE_MASK);
                result[j+4]  = (byte) ((dataBits >> 24) & BYTE_MASK);
                result[j+5]  = (byte) ((dataBits >> 16) & BYTE_MASK);
                result[j+6]  = (byte) ((dataBits >> 8) & BYTE_MASK);
                result[j+7]  = (byte) (dataBits & BYTE_MASK);
            }
        }
        return result;
    }

    /**
     * Encode one string; generate the byte array that contains the descriptor and the encoded data.
     * @param data one string
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by STRING
     */
    public static byte[] encodeOneString(String data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.STRING.identifier;
        } else {
            byte[] stringBytes = data.getBytes();
            result = new byte[stringBytes.length + Short.BYTES + 1];
            result[0] = ABIToken.STRING.identifier;
            result[1] = (byte) ((data.length() >> 8) & BYTE_MASK);
            result[2] = (byte) (data.length() & BYTE_MASK);
            System.arraycopy(stringBytes, 0, result, 3, stringBytes.length);
        }
        return result;
    }

    /**
     * Encode one address; generate the byte array that contains the descriptor and the encoded data.
     * @param data one address
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the two identifiers: NULL, followed by ADDRESS
     */
    public static byte[] encodeOneAddress(Address data) {
        byte[] result;
        if (null == data) {
            result = new byte[2];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ADDRESS.identifier;
        } else {
            byte[] addressBytes = data.unwrap();
            if(Address.LENGTH != addressBytes.length) {
                throw new ABIException("Address was of unexpected length");
            }
            result = new byte[Address.LENGTH + 1];
            result[0] = ABIToken.ADDRESS.identifier;
            System.arraycopy(addressBytes, 0, result, 1, addressBytes.length);
        }
        return result;
    }

    /**
     * Encode one 2D byte array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D byte array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_BYTE
     */
    public static byte[] encodeOne2DByteArray(byte[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_BYTE.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneByteArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_BYTE.identifier);
        }
        return result;
    }

    /**
     * Encode one 2D boolean array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D boolean array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_BOOLEAN
     */
    public static byte[] encodeOne2DBooleanArray(boolean[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_BOOLEAN.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneBooleanArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_BOOLEAN.identifier);
        }
        return result;
    }

    /**
     * Encode one 2D character array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D character array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_CHAR
     */
    public static byte[] encodeOne2DCharacterArray(char[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_CHAR.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneCharacterArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_CHAR.identifier);
        }
        return result;
    }

    /**
     * Encode one 2D short array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D short array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_SHORT
     */
    public static byte[] encodeOne2DShortArray(short[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_SHORT.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneShortArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_SHORT.identifier);
        }
        return result;
    }

    /**
     * Encode one 2D integer array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D integer array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_INT
     */
    public static byte[] encodeOne2DIntegerArray(int[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_INT.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneIntegerArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_INT.identifier);
        }
        return result;
    }

    /**
     * Encode one 2D float array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D float array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_FLOAT
     */
    public static byte[] encodeOne2DFloatArray(float[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_FLOAT.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneFloatArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_FLOAT.identifier);
        }
        return result;
    }

    /**
     * Encode one 2D long array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D long array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_LONG
     */
    public static byte[] encodeOne2DLongArray(long[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_LONG.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneLongArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_LONG.identifier);
        }
        return result;
    }

    /**
     * Encode one 2D double array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one 2D double array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by A_DOUBLE
     */
    public static byte[] encodeOne2DDoubleArray(double[][] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.A_DOUBLE.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneDoubleArray(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.A_DOUBLE.identifier);
        }
        return result;
    }

    /**
     * Encode one string array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one string array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by STRING
     */
    public static byte[] encodeOneStringArray(String[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.STRING.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneString(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.STRING.identifier);
        }
        return result;
    }

    /**
     * Encode one address array; generate the byte array that contains the descriptor and the encoded data.
     * @param data one address array
     * @return the byte array that contains the argument descriptor and the encoded data.
     * Null is encoded as the three identifiers: NULL, followed by ARRAY, followed by ADDRESS
     */
    public static byte[] encodeOneAddressArray(Address[] data) {
        byte[] result;
        if (null == data) {
            result = new byte[3];
            result[0] = ABIToken.NULL.identifier;
            result[1] = ABIToken.ARRAY.identifier;
            result[2] = ABIToken.ADDRESS.identifier;
        } else {
            int length = Short.BYTES + 2;
            byte[][] encodedArrays = new byte[data.length][];
            for (int i = 0; i < data.length; i++) {
                encodedArrays[i] = encodeOneAddress(data[i]);
                length += encodedArrays[i].length;
            }
            result = flatten2DEncoding(encodedArrays, length, ABIToken.ADDRESS.identifier);
        }
        return result;
    }

    private static byte[] flatten2DEncoding(byte[][] encodedArrays, int lengthOfEncodedResult, byte identifier) {
        byte[] result = new byte[lengthOfEncodedResult];
        result[0] = ABIToken.ARRAY.identifier;
        result[1] = identifier;
        result[2] = (byte) ((encodedArrays.length >> 8) & BYTE_MASK);
        result[3] = (byte) (encodedArrays.length & BYTE_MASK);
        int destPos = 4;
        for (int i = 0; i < encodedArrays.length; i++) {
            System.arraycopy(encodedArrays[i], 0, result, destPos, encodedArrays[i].length);
            destPos += encodedArrays[i].length;
        }
        return result;
    }
}
