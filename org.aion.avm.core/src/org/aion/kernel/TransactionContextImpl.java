package org.aion.kernel;


public class TransactionContextImpl implements TransactionContext {
    private Transaction tx;
    private Block block;

    public TransactionContextImpl(Transaction tx, Block block) {
        this.tx = tx;
        this.block = block;
    }

    @Override
    public Transaction getTransaction() {
        return tx;
    }

    @Override
    public Block getBlock() {
        return block;
    }
}
