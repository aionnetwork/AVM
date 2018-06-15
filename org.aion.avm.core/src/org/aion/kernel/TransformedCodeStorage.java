package org.aion.kernel;

import org.aion.avm.rt.Address;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A emulator of the transformed Dapp Jar storage.
 */
public class TransformedCodeStorage {
    private Map<byte[], File> codeStorage;

    public TransformedCodeStorage() {
        codeStorage = new HashMap<>();
    }

    public void storeCode(Address address, File codeJar) {
        codeStorage.put(address.unwrap(), codeJar);
    }

    public File loadCode(Address address) {
        return codeStorage.get(address.unwrap());
    }
}
