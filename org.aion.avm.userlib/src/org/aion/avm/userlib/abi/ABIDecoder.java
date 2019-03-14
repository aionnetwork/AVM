package org.aion.avm.userlib.abi;

import java.util.List;
import org.aion.avm.userlib.abi.ABICodec.Tuple;

/**
 * Utility class for AVM ABI decoding. This class contains static methods
 * for parsing transaction data and invoking corresponding methods.
 */
public class ABIDecoder {
    /**
     * This class cannot be instantiated.
     */
    private ABIDecoder(){}

    private static List<ABICodec.Tuple> checkAndParse(byte[] txData) {
        if (null == txData) {
            throw new NullPointerException();
        }
        if (txData.length == 0) {
            return null;
        }
        List<ABICodec.Tuple> parsed = ABICodec.parseEverything(txData);

        if (parsed.size() < 1) {
            throw new ABIException("Decoded as " + parsed.size() + " elements");
        }
        if (String.class != parsed.get(0).standardType) {
            throw new ABIException("First parsed value not String (method name)");
        }
        return parsed;
    }

    /**
     * Decode the transaction data and return the method name.
     * @param txData the transaction data that has the encoded method name to call with.
     * @return the decoded method name.
     */
    public static String decodeMethodName(byte[] txData) {
        List<ABICodec.Tuple> parsed = checkAndParse(txData);
        if(null == parsed) {
            return null;
        }
        return (String) parsed.get(0).value;
    }

    /**
     * Decode the transaction data and return the argument list that is encoded in it.
     * @param txData the transaction data that has the encoded arguments descriptor and arguments.
     * @return an object array that contains all of the arguments.
     */
    public static Object[] decodeArguments(byte[] txData) {
        List<ABICodec.Tuple> parsed = checkAndParse(txData);
        if(null == parsed) {
            return null;
        }
        Object[] argValues = new Object[parsed.size() - 1];
        for (int i = 1; i < parsed.size(); ++i) {
            argValues[i - 1] = parsed.get(i).value;
        }
        return argValues;
    }

    /**
     * Decode the transaction data that has one object encoded in it.
     * @param txData the transaction data that has one object encoded in it (with the descriptor).
     * @return the decoded object.
     */
    public static Object decodeOneObject(byte[] txData) {
        if (null == txData) {
            throw new NullPointerException();
        }
        // We will handle an empty payload as a null.
        Object result = null;
        if (txData.length > 0) {
            List<Tuple> parsed = ABICodec.parseEverything(txData);
            if(1 != parsed.size()) {
                throw new ABIException("Expected exactly one object from this decode call");
            }
            result = parsed.get(0).value;
        }
        return result;
    }
}
