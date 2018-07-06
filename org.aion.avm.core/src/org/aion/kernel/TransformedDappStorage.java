package org.aion.kernel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An emulator of the transformed Dapp Jar storage.
 */
public class TransformedDappStorage {
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
     * DappStorage includes the code version and the transformed jar file.
     */
    public class DappStorage {
        private CodeVersion version;
        private byte[] jarFile;

        public DappStorage(CodeVersion version, byte[] jarFile) {
            this.version = version;
            this.jarFile = jarFile;
        }
    }

    /**
     * A wrapper of byte[], to be used as the HashMap key.
     */
    public final class ByteArrayWrapper
    {
        private final byte[] data;

        public ByteArrayWrapper(byte[] data)
        {
            if (data == null)
            {
                throw new NullPointerException();
            }
            this.data = data;
        }

        @Override
        public boolean equals(Object object)
        {
            if (!(object instanceof ByteArrayWrapper))
            {
                return false;
            }
            return Arrays.equals(data, ((ByteArrayWrapper)object).data);
        }

        @Override
        public int hashCode()
        {
            return Arrays.hashCode(data);
        }
    }

    private Map<ByteArrayWrapper, DappStorage> codeStorage;

    /**
     * Constructor.
     */
    public TransformedDappStorage() {
        this.codeStorage = new HashMap<>();
    }

    /**
     * Store a Dapp.
     * @param address the Dapp address.
     * @param version the code version.
     * @param codeJar the Jar file of the transformed code.
     */
    public void storeCode(byte[] address, CodeVersion version, byte[] codeJar) {
        codeStorage.put(new ByteArrayWrapper(address), new DappStorage(version, codeJar));
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
