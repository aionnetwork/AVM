package org.aion.avm.api;

public final class ABIDecoder {
    /**
     * This class cannot be instantiated.
     */
    private ABIDecoder(){}

    /**
     * Decode the transaction data and invoke the corresponding method of the Dapp class.
     * @param clazz the user space class.
     * @param txData the transaction data that is encoded with the method name and arguments to call with.
     * @return the encoded return data from the method call.
     */
    public static byte[] decodeAndRunWithClass(Class<?> clazz, byte[] txData) {
        return null;
    }

    /**
     * Decode the transaction data and invoke the corresponding method of the object's class.
     * @param object the user space class object.
     * @param txData the transaction data that is encoded with the method name and arguments to call with.
     * @return the encoded return data from the method call.
     */
    public static byte[] decodeAndRunWithObject(Object object, byte[] txData) {
        return null;
    }

    /**
     * Decode the transaction data and return the method name.
     * @param txData the transaction data that has the encoded method name to call with.
     * @return the decoded method name.
     */
    public static String decodeMethodName(byte[] txData) {
        return null;
    }

    /**
     * Decode the transaction data and return the argument list that is encoded in it.
     * @param txData the transaction data that has the encoded arguments descriptor and arguments.
     * @return an object array that contains all of the arguments.
     */
    public static Object[] decodeArguments(byte[] txData) {
        return null;
    }

    /**
     * Decode the transaction data that has one object encoded in it.
     * @param txData the transaction data that has one object encoded in it (with the descriptor).
     * @return the decoded object.
     */
    public static Object decodeOneObject(byte[] txData) {
        return null;
    }
}
