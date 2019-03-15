package org.aion.avm.userlib.abi;

import java.util.List;
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
        for (Object arg : arguments) {
            // We sniff the type, directly, since there are no nulls in this path.
            tuplesToEncode.add(new ABICodec.Tuple(arg.getClass(), arg));
        }
        return ABICodec.serializeList(tuplesToEncode);
    }
}
