package org.aion.avm.api;

public final class ABIEncoder {
    /**
     * This class cannot be instantiated.
     */
    private ABIEncoder(){}

    /**
     * Encode one object of any type that Aion ABI allows; generate the byte array that contains the descriptor and the encoded data.
     * @param data one object of any type that Aion ABI allows
     * @return the byte array that contains the argument descriptor and the encoded data.
     * @return
     */
    public static byte[] encodeOneObject(Object data) {
        return null;
    }

    /**
     * An utility method to encode the method name and method arguments to call with, according to Aion ABI format. Both method name and the arguments can be null if needed.
     * @param methodName the method name of the Dapp main class to call with
     * @param arguments the arguments of the corresponding method of Dapp main class to call with
     * @return the encoded byte array that contains the method descriptor, followed by the argument descriptor and encoded arguments, according the Aion ABI format.
     */
    public static byte[] encodeMethodArguments(String methodName, Object... arguments) {
        return null;
    }
}
