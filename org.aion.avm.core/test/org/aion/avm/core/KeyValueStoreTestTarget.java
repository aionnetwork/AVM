package org.aion.avm.core;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

public class KeyValueStoreTestTarget {

    public static byte[] testAvmGetStorage(byte[] key) {
        return Blockchain.getStorage(key);
    }

    public static void testAvmPutStorage(byte[] key, byte[] value) {
        Blockchain.putStorage(key, value);
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();

        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("testAvmGetStorage")) {
                byte[] key = decoder.decodeOneByteArray();
                return testAvmGetStorage(key);
            } else if (methodName.equals("testAvmPutStorage")) {
                byte[] key = decoder.decodeOneByteArray();
                byte[] value = decoder.decodeOneByteArray();
                testAvmPutStorage(key, value);
                return new byte[0];
            } else if (methodName.equals("testAvmPutStorageNullValue")) {
                byte[] key = decoder.decodeOneByteArray();
                testAvmPutStorage(key, null);
                return new byte[0];
            }
            else {
                return new byte[0];
            }
        }
    }
}
