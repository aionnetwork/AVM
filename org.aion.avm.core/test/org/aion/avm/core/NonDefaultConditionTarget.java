package org.aion.avm.core;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

public class NonDefaultConditionTarget {

    public static byte[] getStorage(byte[] key) {
        return Blockchain.getStorage(key);
    }

    public static void putStorage(byte[] key, byte[] value) {
        Blockchain.putStorage(key, value);
    }

    public static void selfDestruct() {
        Blockchain.selfDestruct(Blockchain.getCaller());
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();

        if (methodName == null) {
            return new byte[0];
        } else {
            switch (methodName) {
                case "getStorage": {
                    byte[] key = decoder.decodeOneByteArray();
                    return getStorage(key);
                }
                case "putStorage": {
                    byte[] key = decoder.decodeOneByteArray();
                    byte[] value = decoder.decodeOneByteArray();
                    putStorage(key, value);
                    break;
                }
                case "selfDestruct":
                    selfDestruct();
                    break;
            }
            return new byte[0];
        }
    }
}
