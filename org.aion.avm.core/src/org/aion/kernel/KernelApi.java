package org.aion.kernel;

import org.aion.avm.core.AvmResult;

import java.io.File;

/**
 * Interface for accessing kernel features.
 */
public interface KernelApi {

    void putTransformedCode(byte[] address, TransformedDappStorage.CodeVersion version, File code);

    File getTransformedCode(byte[] address);

    void putStorage(byte[] address, byte[] key, byte[] value);

    byte[] getStorage(byte[] address, byte[] key);

    AvmResult call(byte[] from, byte[] to, byte[] value, byte[] data, long energyLimit);

    void updateCode(byte[] address, byte[] code);

    void selfdestruct(byte[] address, byte[] beneficiary);

    void log(byte[] address, byte[] index0, byte[] data);
}
