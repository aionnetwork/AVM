package org.aion.kernel;

import org.aion.avm.rt.Address;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * An emulator of the transformed Dapp Jar storage.
 */
public class TransformedDappStorage {
    private Map<byte[], File> codeStorage;

    public TransformedDappStorage() {
        codeStorage = new HashMap<>();
    }

    public void storeCode(Address address, File codeJar) {
        codeStorage.put(address.unwrap(), codeJar);
    }

    public File loadCode(Address address) {
        return codeStorage.get(address.unwrap());
    }
}
