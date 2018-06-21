package org.aion.kernel;

import org.aion.avm.core.AvmResult;

import java.io.File;

public class KernelApiImpl implements KernelApi {

    private TransformedDappStorage codeStorage = new TransformedDappStorage();

    @Override
    public void putTransformedCode(byte[] address, TransformedDappStorage.CodeVersion version, File code) {
        codeStorage.storeCode(address, version, code);
    }

    // TODO: return both version and the file
    @Override
    public File getTransformedCode(byte[] address) {
        return codeStorage.loadCode(address);
    }

    @Override
    public void putStorage(byte[] address, byte[] key, byte[] value) {

    }

    @Override
    public byte[] getStorage(byte[] address, byte[] key) {
        return new byte[0];
    }

    @Override
    public AvmResult call(byte[] from, byte[] to, byte[] value, byte[] data, long energyLimit) {
        return null;
    }

    @Override
    public void updateCode(byte[] address, byte[] code) {

    }

    @Override
    public void selfdestruct(byte[] address, byte[] beneficiary) {

    }

    @Override
    public void log(byte[] address, byte[] index0, byte[] data) {

    }
}
