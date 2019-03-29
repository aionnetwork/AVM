package org.aion.avm.core.exceptionwrapping;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * A test of the persistent exception behaviour:  built-in and user-defined exceptions should be serialized/deserialized correctly.
 */
public class PersistentExceptionTarget {
    private static NullPointerException system;
    private static UserDefinedException user;

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("storeSystem")) {
                return ABIEncoder.encodeOneInteger(storeSystem());
            } else if (methodName.equals("loadSystem")) {
                return ABIEncoder.encodeOneInteger(loadSystem());
            } else if (methodName.equals("storeUser")) {
                return ABIEncoder.encodeOneByteArray(storeUser());
            } else if (methodName.equals("loadUser")) {
                return ABIEncoder.encodeOneByteArray(loadUser());
            } else if (methodName.equals("getSecond")) {
                return ABIEncoder.encodeOneByteArray(getSecond());
            } else {
                return new byte[0];
            }
        }
    }

    public static int storeSystem() {
        system = null;
        try {
            // Cause the throw to happen.
            ((Object)null).hashCode();
        } catch (NullPointerException e) {
            // Save this.
            system = e;
        }
        return system.hashCode();
    }

    public static int loadSystem() {
        return system.hashCode();
    }

    public static byte[] storeUser() {
        user = null;
        try {
            // Cause the throw to happen.
            throw new UserDefinedException("MESSAGE", "Second message");
        } catch (UserDefinedException e) {
            // Save this.
            user = e;
        }
        return user.getMessage().getBytes();
    }

    public static byte[] loadUser() {
        return user.getMessage().getBytes();
    }

    public static byte[] getSecond() {
        return user.second.getBytes();
    }


    public static class UserDefinedException extends Exception {
        private static final long serialVersionUID = 1L;
        public final String second;
        public UserDefinedException(String message, String second) {
            super(message);
            this.second = second;
        }
    }
}
