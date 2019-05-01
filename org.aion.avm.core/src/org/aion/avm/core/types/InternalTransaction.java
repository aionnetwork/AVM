package org.aion.avm.core.types;

import java.math.BigInteger;
import org.aion.kernel.Transaction;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.InternalTransactionInterface;

/**
 * Represents an AION internal transaction
 */
public class InternalTransaction extends Transaction implements InternalTransactionInterface {

    private boolean rejected ;

    public InternalTransaction(Type type, Address from, Address to, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        super(type, from, to, nonce, value, data, energyLimit, energyPrice);
    }

    @Override
    public long getTransactionCost() {
        return 0;
    }

    @Override
    public void markAsRejected() {
        this.rejected = true;
    }

    @Override
    public boolean isRejected() {
        return rejected;
    }

}
