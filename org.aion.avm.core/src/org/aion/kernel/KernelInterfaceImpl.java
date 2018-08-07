package org.aion.kernel;

import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class KernelInterfaceImpl implements KernelInterface {

    // shared across-context
    private static DappCode dappCode = new DappCode();
    private static Map<ByteArrayWrapper, byte[]> dappStorage = new ConcurrentHashMap<>();

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
    public TransactionResult call(InternalTransaction internalTx, Block parentBlock) {
        return new AvmImpl(this).run(new TransactionContextImpl(internalTx, parentBlock));
    }

    @Override
    public void updateCode(byte[] address, byte[] code) {

    }

    @Override
    public void selfdestruct(byte[] address, byte[] beneficiary) {

    }
}
