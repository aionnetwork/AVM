package org.aion.avm.core.types;

import java.math.BigInteger;
import org.aion.kernel.Transaction;

/**
 * Represents an AION internal transaction
 */
public class InternalTransaction extends Transaction {

    private boolean rejected ;

    public InternalTransaction(Type type, byte[] from, byte[] to, long nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        super(type, from, to, nonce, value, data, energyLimit, energyPrice);
    }

    @Override
    public long getBasicCost() {
        return 0;
    }

    public void markAsRejected() {
        this.rejected = true;
    }

    public boolean isRejected() {
        return rejected;
    }
}
