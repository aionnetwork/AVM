package org.aion.kernel;

import org.aion.avm.core.Avm;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class KernelInterfaceImpl implements KernelInterface {

    // shared across-context
    private static Map<ByteArrayWrapper, VersionedCode> dappCodeDB = new ConcurrentHashMap<>();
    private static Map<ByteArrayWrapper, byte[]> dappStorageDB = new ConcurrentHashMap<>();

    @Override
    public void putCode(byte[] address, VersionedCode code) {
        dappCodeDB.put(new ByteArrayWrapper(address), code);
    }

    @Override
    public VersionedCode getCode(byte[] address) {
        return dappCodeDB.get(new ByteArrayWrapper(address));
    }

    @Override
    public void putStorage(byte[] address, byte[] key, byte[] value) {
        ByteArrayWrapper k = new ByteArrayWrapper(Helpers.merge(address, key));
        dappStorageDB.put(k, value);
    }

    @Override
    public byte[] getStorage(byte[] address, byte[] key) {
        ByteArrayWrapper k = new ByteArrayWrapper(Helpers.merge(address, key));
        return dappStorageDB.get(k);
    }

    @Override
    public void updateCode(byte[] address, byte[] code) {

    }

    @Override
    public void selfdestruct(byte[] address, byte[] beneficiary) {

    }
}
