package org.aion.avm.api;

public class ABIDecoder {
    public static class MethodCaller {
        public String methodName;
        public String argsDescriptor;
        public Object[] arguments;
    }

    public static byte[] decodeAndRun(Object object, byte[] txData) {
        return null;
    }

    public static MethodCaller decode(byte[] txData) {
        return null;
    }
}
