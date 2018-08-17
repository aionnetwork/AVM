package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * The test class loaded by HashCodeIntegrationTest.
 */
public class HashCodeIntegrationTestTarget {
    private static Object persistentObject;

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(new HashCodeIntegrationTestTarget(), BlockchainRuntime.getData());
    }
    
    public static int persistNewObject() {
        persistentObject = new Object();
        return persistentObject.hashCode();
    }
    
    public static int readPersistentHashCode() {
        return persistentObject.hashCode();
    }
}
