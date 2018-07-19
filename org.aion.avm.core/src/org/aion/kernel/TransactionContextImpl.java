package org.aion.kernel;

import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransactionContextImpl implements TransactionContext {

    // shared across-context
    private static DappCode dappCode = new DappCode();
    private static Map<ByteArrayWrapper, byte[]> dappStorage = new HashMap<>();

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

    @Override
    public void putTransformedCode(byte[] address, DappCode.CodeVersion version, byte[] code) {
        dappCode.storeCode(address, version, code);
    }

    // TODO: return both version and the file
    @Override
    public byte[] getTransformedCode(byte[] address) {
        return dappCode.loadCode(address);
    }

    @Override
    public void putStorage(byte[] address, byte[] key, byte[] value) {
        ByteArrayWrapper k = new ByteArrayWrapper(Helpers.merge(address, key));
        dappStorage.put(k, value);
    }

    @Override
    public byte[] getStorage(byte[] address, byte[] key) {
        ByteArrayWrapper k = new ByteArrayWrapper(Helpers.merge(address, key));
        return dappStorage.get(k);
    }

    @Override
    public TransactionResult call(InternalTransaction internalTx) {
        return new AvmImpl().run(new TransactionContextImpl(internalTx, block));
    }

    @Override
    public void updateCode(byte[] address, byte[] code) {

    }

    @Override
    public void selfdestruct(byte[] address, byte[] beneficiary) {

    }
}
