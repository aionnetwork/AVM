package org.aion.avm.core.types;

import org.aion.kernel.Transaction;

/**
 * Represents an AION internal transaction
 */
public class InternalTransaction extends Transaction {

    private boolean rejected ;

    public InternalTransaction(Type type, byte[] from, byte[] to, long value, byte[] data, long energyLimit, long energyPrice) {
        super(type, from, to, value, data, energyLimit, energyPrice);
    }

    public void markAsRejected() {
        this.rejected = true;
    }

    public boolean isRejected() {
        return rejected;
    }
}
