package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * The test class loaded by HashCodeIntegrationTest.
 */
public class HashCodeIntegrationTestTarget {
    private static Object persistentObject;

    public static byte[] main() {
        // We use an instance target since that lets us advance the hashcode based on how many calls we receive.
        return ABIDecoder.decodeAndRunWithClass(HashCodeIntegrationTestTarget.class, BlockchainRuntime.getData());
    }
    
    public static int persistNewObject() {
        persistentObject = new Object();
        return persistentObject.hashCode();
    }
    
    public static int readPersistentHashCode() {
        return persistentObject.hashCode();
    }
}
