package org.aion.kernel;

/**
 * A bundle of version and code, for future vm upgrade.
 */
public class VersionedCode {

    public static final byte V1 = 1;
    public static final byte V2 = 2;

    private byte version;
    private byte[] code;

    public VersionedCode(byte version, byte[] code) {
        this.version = version;
        this.code = code;
    }

    public byte getVersion() {
        return version;
    }

    public byte[] getCode() {
        return code;
    }
}
