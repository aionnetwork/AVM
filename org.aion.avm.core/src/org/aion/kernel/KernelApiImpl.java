package org.aion.kernel;

import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;


public class KernelApiImpl implements KernelApi {

    private DappCode codeStorage = new DappCode();
    private Map<ByteArrayWrapper, byte[]> dappStorage = new HashMap<>();
    private ArrayList<String> logStorage = new ArrayList<>();

    public Block block = null;

    public KernelApiImpl(Block cb){
        block = cb;
    }

    public KernelApiImpl(){
    }

    @Override
    public void putTransformedCode(byte[] address, DappCode.CodeVersion version, byte[] code) {
        codeStorage.storeCode(address, version, code);
    }

    // TODO: return both version and the file
    @Override
    public byte[] getTransformedCode(byte[] address) {
        return codeStorage.loadCode(address);
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
    public TransactionResult call(byte[] from, byte[] to, long value, byte[] data, long energyLimit) {
        Assert.assertTrue(block != null);

        Transaction internalTx = new Transaction(Transaction.Type.CALL, from, to, value, data, energyLimit);
        AvmImpl avm = new AvmImpl();
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
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Address: " + Helpers.toHexString(address) + "\n");
        logBuilder.append("Source: " + new String(index0)+ "\n");
        logBuilder.append("Data: " + new String(data)+ "\n");
        logStorage.add(logBuilder.toString());
    }

    public void printLog() {
        for (String s: logStorage){
            System.out.println(s);
        }
    }
}
