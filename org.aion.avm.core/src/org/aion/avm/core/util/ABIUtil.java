package org.aion.avm.core.util;

import java.nio.ByteBuffer;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;

/**
 * A utility that helps encode method arguments that is used to call test contracts
 * These are kept here just to avoid duplication.
 */
public class ABIUtil {

    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * A utility method to encode the method name and method arguments to call with, according to Aion ABI format.
     * <br>
     * The arguments parameter can behave unexpectedly when receiving multi-dimensional primitive arrays and arrays of objects. In these cases, it is recommended to explicitly cast the arguments into an Object[].
     * @param methodName the method name of the Dapp main class to call with
     * @param arguments the arguments of the corresponding method of Dapp main class to call with
     * @return the encoded byte array that contains the method descriptor, followed by the argument descriptor and encoded arguments, according to the Aion ABI format.
     * @throws NullPointerException If methodName or arguments are null (note that, under normal usage, arguments will be empty instead of null).
     */
    public static byte[] encodeMethodArguments(String methodName, Object... arguments) {
        ByteBuffer encodedBytes = ByteBuffer.allocate(BUFFER_SIZE);
        if ((null == methodName) || (null == arguments)) {
            throw new NullPointerException();
        }

        encodedBytes.put(encodeOneObject(methodName));
        for (Object arg : arguments) {
            encodedBytes.put(encodeOneObject(arg));
        }

        // Convert this into a byte[] of the appropriate size;
        int length = encodedBytes.position();
        byte[] populated = new byte[length];
        System.arraycopy(encodedBytes.array(), 0, populated, 0, populated.length);
        return populated;
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
        ByteBuffer encodedBytes = ByteBuffer.allocate(BUFFER_SIZE);
        if (null == arguments) {
            throw new NullPointerException();
        }

        for (Object arg : arguments) {
            encodedBytes.put(encodeOneObject(arg));
        }

        // Convert this into a byte[] of the appropriate size;
        int length = encodedBytes.position();
        byte[] populated = new byte[length];
        System.arraycopy(encodedBytes.array(), 0, populated, 0, populated.length);
        return populated;
    }

    /**
     * A utility method to encode a single object.
     *
     * @param data the object to be encoded, must be of an allowed ABI type
     * @return the encoded byte array that contains the encoded argument, according the Aion ABI format.
     * @throws ABIException If data is not of an allowed ABI type
     */
    public static byte[] encodeOneObject(Object data) {
        Class clazz = data.getClass();
        if(clazz == Byte.class) {
            return ABIEncoder.encodeOneByte((byte) data);
        } else if (clazz == Boolean.class) {
            return ABIEncoder.encodeOneBoolean((boolean) data);
        } else if (clazz == Character.class) {
            return ABIEncoder.encodeOneCharacter((char) data);
        } else if (clazz == Short.class) {
            return ABIEncoder.encodeOneShort((short) data);
        } else if (clazz == Integer.class) {
            return ABIEncoder.encodeOneInteger((int) data);
        } else if (clazz == Long.class) {
            return ABIEncoder.encodeOneLong((long) data);
        } else if (clazz == Float.class) {
            return ABIEncoder.encodeOneFloat((float) data);
        } else if (clazz == Double.class) {
            return ABIEncoder.encodeOneDouble((double) data);
        } else if (clazz == byte[].class) {
            return ABIEncoder.encodeOneByteArray((byte[]) data);
        } else if (clazz == boolean[].class) {
            return ABIEncoder.encodeOneBooleanArray((boolean[]) data);
        } else if (clazz == char[].class) {
            return ABIEncoder.encodeOneCharacterArray((char[]) data);
        } else if (clazz == short[].class) {
            return ABIEncoder.encodeOneShortArray((short[]) data);
        } else if (clazz == int[].class) {
            return ABIEncoder.encodeOneIntegerArray((int[]) data);
        } else if (clazz == long[].class) {
            return ABIEncoder.encodeOneLongArray((long[]) data);
        } else if (clazz == float[].class) {
            return ABIEncoder.encodeOneFloatArray((float[]) data);
        } else if (clazz == double[].class) {
            return ABIEncoder.encodeOneDoubleArray((double[]) data);
        } else if (clazz == String.class) {
            return ABIEncoder.encodeOneString((String) data);
        } else if (clazz == org.aion.avm.api.Address.class) {
            return ABIEncoder.encodeOneAddress((org.aion.avm.api.Address) data);
        } else if (clazz == byte[][].class) {
        return ABIEncoder.encodeOne2DByteArray((byte[][]) data);
        } else if (clazz == boolean[][].class) {
            return ABIEncoder.encodeOne2DBooleanArray((boolean[][]) data);
        } else if (clazz == char[][].class) {
            return ABIEncoder.encodeOne2DCharacterArray((char[][]) data);
        } else if (clazz == short[][].class) {
            return ABIEncoder.encodeOne2DShortArray((short[][]) data);
        } else if (clazz == int[][].class) {
            return ABIEncoder.encodeOne2DIntegerArray((int[][]) data);
        } else if (clazz == long[][].class) {
            return ABIEncoder.encodeOne2DLongArray((long[][]) data);
        } else if (clazz == float[][].class) {
            return ABIEncoder.encodeOne2DFloatArray((float[][]) data);
        } else if (clazz == double[][].class) {
            return ABIEncoder.encodeOne2DDoubleArray((double[][]) data);
        } else if (clazz == String[].class) {
            return ABIEncoder.encodeOneStringArray((String[]) data);
        } else if (clazz == org.aion.avm.api.Address[].class) {
            return ABIEncoder.encodeOneAddressArray((org.aion.avm.api.Address[]) data);
        } else {
            throw new ABIException("Unsupported ABI type");
        }
    }
}
