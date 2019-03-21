package org.aion.avm.core;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * The test class loaded by HashCodeIntegrationTest.
 */
public class HashCodeIntegrationTestTarget {
    private static Object persistentObject;

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("persistNewObject")) {
                return ABIEncoder.encodeOneObject(persistNewObject());
            } else if (methodName.equals("readPersistentHashCode")) {
                return ABIEncoder.encodeOneObject(readPersistentHashCode());
            } else {
                return new byte[0];
            }
        }
    }
    
    public static int persistNewObject() {
        persistentObject = new Object();
        return persistentObject.hashCode();
    }
    
    public static int readPersistentHashCode() {
        return persistentObject.hashCode();
    }
}
