package org.aion.avm.core.types;

import java.math.BigInteger;
import org.aion.kernel.Transaction;
import org.aion.vm.api.interfaces.InternalTransactionInterface;

/**
 * Represents an AION internal transaction
 */
public class InternalTransaction extends Transaction implements InternalTransactionInterface {

    private boolean rejected ;

    public InternalTransaction(Type type, byte[] from, byte[] to, long nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
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

    @Override
    public byte[] getParentTransactionHash() {
        throw new AssertionError("No corresponding Avm concept exists for this yet.");
    }

    //TODO: This is currently implemented in TransactionContextImpl and needs to be refactored into
    //TODO: this class instead.
    @Override
    public int getStackDepth() {
        throw new AssertionError("No corresponding Avm concept exists for this yet.");
    }

    @Override
    public int getIndexOfInternalTransaction() {
        throw new AssertionError("No corresponding Avm concept exists for this yet.");
    }

    @Override
    public String getNote() {
        throw new AssertionError("No corresponding Avm concept exists for this yet.");
    }

    @Override
    public byte[] getEncoded() {
        throw new AssertionError("No corresponding Avm concept exists for this yet.");
    }

}
