package org.aion.kernel;

import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmResult;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Assert;


public class KernelApiImpl implements KernelApi {

    private TransformedDappStorage codeStorage = new TransformedDappStorage();

    public AvmSharedClassLoader sharedClassLoader = null;
    public Block block = null;

    @Override
    public void putTransformedCode(byte[] address, TransformedDappStorage.CodeVersion version, byte[] code) {
        codeStorage.storeCode(address, version, code);
    }

    // TODO: return both version and the file
    @Override
    public byte[] getTransformedCode(byte[] address) {
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
    public AvmResult call(byte[] from, byte[] to, long value, byte[] data, long energyLimit) {
        Assert.assertTrue(sharedClassLoader != null);
        Assert.assertTrue(block != null);

        Transaction internalTx = new Transaction(Transaction.Type.CALL, from, to, value, data, energyLimit);
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        return avm.run(internalTx, block, this); // TODO: passing this instance is wrong
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
