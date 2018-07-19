package org.aion.kernel;

/**
 * Represents an AION internal transaction
 */
public class InternalTransaction extends Transaction {

    public InternalTransaction(Type type, byte[] from, byte[] to, long value, byte[] data, long energyLimit) {
        super(type, from, to, value, data, energyLimit);
    }
}
