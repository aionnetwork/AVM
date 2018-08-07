package org.aion.avm.core;

import org.aion.kernel.Block;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;


public class AvmImpl implements Avm {
    @Override
    public TransactionResult run(TransactionContext ctx) {

        Transaction tx = ctx.getTransaction();
        Block block = ctx.getBlock();

        // only one result (mutable) shall be created per transaction execution
        TransactionResult result = new TransactionResult();

        // TODO: charge basic transaction cost

        switch (tx.getType()) {
            case CREATE:
                DAppCreator.create(tx, block, ctx, result);
                break;
            case CALL:
                DAppExecutor.call(tx, block, ctx, result);
                break;
            default:
                result.setStatusCode(TransactionResult.Code.INVALID_TX);
                result.setEnergyUsed(tx.getEnergyLimit());
                break;
        }

        return result;
    }
}
