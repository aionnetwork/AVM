package org.aion.kernel;

import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;
import org.aion.data.IAccountStore;
import org.aion.data.IDataStore;
import org.aion.data.MemoryBackedDataStore;

import java.util.Map;


/**
 * Mostly just a high-level wrapper around and underlying IDataStore.
 * Note that this implementation implicitly creates accounts in response to mutative operations.  They are not explicitly created.
 * Likewise, reading data from a non-existent account safely returns null or 0L, rather than failing.
 */
public class KernelInterfaceImpl implements KernelInterface {
    public static final byte[] PREMINED_ADDRESS = Helpers.randomBytes(32);
    public static final long PREMINED_AMOUNT = (long) (500L * Math.pow(10, 18));

    private final IDataStore dataStore;

    public KernelInterfaceImpl() {
        this.dataStore = new MemoryBackedDataStore();
        IAccountStore premined = this.dataStore.createAccount(PREMINED_ADDRESS);
        premined.setBalance(PREMINED_AMOUNT);
    }

    @Override
    public boolean isExists(byte[] address) {
        return (null != this.dataStore.openAccount(address));
    }

    @Override
    public void createAccount(byte[] address) {
        this.dataStore.createAccount(address);
    }

    @Override
    public void putCode(byte[] address, byte[] code) {
        lazyCreateAccount(address).setCode(code);
    }

    @Override
    public byte[] getCode(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        return (null != account)
                ? account.getCode()
                : null;
    }

    @Override
    public void putStorage(byte[] address, byte[] key, byte[] value) {
        lazyCreateAccount(address).setData(key, value);
    }

    @Override
    public byte[] getStorage(byte[] address, byte[] key) {
        IAccountStore account = this.dataStore.openAccount(address);
        return (null != account)
                ? account.getData(key)
                : null;
    }

    @Override
    public void deleteAccount(byte[] address) {
        this.dataStore.deleteAccount(address);
    }

    @Override
    public long getBalance(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        return (null != account)
                ? account.getBalance()
                : 0L;
    }

    @Override
    public void adjustBalance(byte[] address, long delta) {
        IAccountStore account = lazyCreateAccount(address);
        long start = account.getBalance();
        account.setBalance(start + delta);
    }

    @Override
    public long getNonce(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        return (null != account)
                ? account.getNonce()
                : 0L;
    }

    @Override
    public void incrementNonce(byte[] address) {
        IAccountStore account = lazyCreateAccount(address);
        long start = account.getNonce();
        account.setNonce(start + 1);
    }

    @Override
    public Map<ByteArrayWrapper, byte[]> getStorageEntries(byte[] address) {
        return this.dataStore.openAccount(address).getStorageEntries();
    }


    private IAccountStore lazyCreateAccount(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        if (null == account) {
            account = this.dataStore.createAccount(address);
        }
        return account;
    }
}
