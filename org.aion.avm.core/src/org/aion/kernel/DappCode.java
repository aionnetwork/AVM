package org.aion.kernel;

import org.aion.avm.core.util.ByteArrayWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * An emulator of the transformed Dapp Jar storage.
 */
public class DappCode {
    /**
     * An enum of the code version.
     */
    public enum CodeVersion {
        VERSION_1_0  ("1.0"),
        VERSION_2_0  ("2.0");

        private final String version;

        CodeVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    /**
     * Dapp includes the code version and the transformed jar file.
     */
    public class Dapp {
        private CodeVersion version;
        private byte[] jarFile;

        public Dapp(CodeVersion version, byte[] jarFile) {
            this.version = version;
            this.jarFile = jarFile;
        }
    }

    private Map<ByteArrayWrapper, Dapp> codeStorage;

    /**
     * Constructor.
     */
    public DappCode() {
        this.codeStorage = new HashMap<>();
    }

    /**
     * Store a Dapp.
     * @param address the Dapp address.
     * @param version the code version.
     * @param codeJar the Jar file of the transformed code.
     */
    public void storeCode(byte[] address, CodeVersion version, byte[] codeJar) {
        codeStorage.put(new ByteArrayWrapper(address), new Dapp(version, codeJar));
    }

    /**
     * Load a Dapp code.
     * @param address the Dapp address.
     * @return the Jar file of the transformed code of the Dapp.
     */
    public byte[] loadCode(byte[] address) {
        ByteArrayWrapper wrapper = new ByteArrayWrapper(address);
        if (codeStorage.containsKey(wrapper)) {
            return codeStorage.get(wrapper).jarFile;
        }
        return null;
    }

    /**
     * Return the code version of a Dapp.
     * @param address the Dapp address.
     * @return the CodeVersion enum that indicates the code version of the Dapp.
     */
    public CodeVersion getCodeVersion(byte[] address) {
        return codeStorage.get(new ByteArrayWrapper(address)).version;
    }

    public void removeDapp(byte[] address) {
        codeStorage.remove(new ByteArrayWrapper(address));
    }
}
