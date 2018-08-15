package org.aion.avm.api;

public final class ABIDecoder {
    public static class MethodCaller {
        public String methodName;
        public String argsDescriptor;
        public Object[] arguments;
    }

    /**
     * This class cannot be instantiated.
     */
    private ABIDecoder(){}

    /**
     * Decode the transaction data and invoke the corresponding method of the object's class.
     * @param object the user space class object.
     * @param txData the transaction data that is encoded with the method name and arguments to call with.
     * @return the encoded return data from the method call.
     */
    public static byte[] decodeAndRun(Object object, byte[] txData) {
        return null;
    }

    /**
     * Decode the transaction data and return the method caller.
     * @param txData the transaction data that has the encoded method name, arguments descriptor and arguments to call with.
     * @return the method caller that contains the method name, arguments descriptor and the arguments.
     */
    public static MethodCaller decode(byte[] txData) {
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
