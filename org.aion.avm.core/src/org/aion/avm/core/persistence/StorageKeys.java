package org.aion.avm.core.persistence;


/**
 * A single place where we manage mapping special-case and general-case keys into the global namespace.
 * It is mostly just a place where we can record our various special storage constants and make sure they don't collide.
 * 
 * TODO:  Determine real keys, once we know if/what organization is being applied to it.
 */
public class StorageKeys {
    public static final byte[] CLASS_STATICS = new byte[] {11,12,13,14,15,16,17,18,19,10};
    public static final byte[] CONTRACT_ENVIRONMENT = new byte[] {1,2,3,4,5,6,7,8,9,0};
}
