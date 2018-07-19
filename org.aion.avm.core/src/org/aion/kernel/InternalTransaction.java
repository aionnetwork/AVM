package org.aion.kernel;

/**
 * Represents an AION internal transaction
 */
public class InternalTransaction extends Transaction {

    private Transaction parent;
    private boolean rejected ;

    public InternalTransaction(Type type, byte[] from, byte[] to, long value, byte[] data, long energyLimit, Transaction parent) {
        super(type, from, to, value, data, energyLimit);
        this.parent = parent;
    }

    public void markAsRejected() {
        this.rejected = true;
    }

    public boolean isRejected() {
        return rejected;
    }
}
