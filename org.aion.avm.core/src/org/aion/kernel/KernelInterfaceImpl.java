package org.aion.kernel;

import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class KernelInterfaceImpl implements KernelInterface {

    public static final byte[] PREMINED_ADDRESS = Helpers.randomBytes(32);
    public static final long PREMINED_AMOUNT = (long) (500L * Math.pow(10, 18));

    private Map<ByteArrayWrapper, AccountState> accounts = new HashMap<>();

    public KernelInterfaceImpl() {
        accounts.put(new ByteArrayWrapper(PREMINED_ADDRESS), new AccountState(PREMINED_AMOUNT, 0));
    }

    /**
     * Returns an account.
     *
     * @param address the account address
     * @return account state, or NULL if not exist
     */
    private AccountState getAccount(byte[] address) {
        return accounts.get(new ByteArrayWrapper(address));
    }

    @Override
    public boolean isExists(byte[] address) {
        return accounts.containsKey(new ByteArrayWrapper(address));
    }

    @Override
    public void createAccount(byte[] address) {
        if (!isExists(address)) {
            accounts.put(new ByteArrayWrapper(address), new AccountState());
        }
    }

    @Override
    public void putCode(byte[] address, VersionedCode code) {
        createAccount(address);
        getAccount(address).code = code;
    }

    @Override
    public VersionedCode getCode(byte[] address) {
        AccountState acc = getAccount(address);
        return acc == null ? null : acc.code;
    }

    @Override
    public void putStorage(byte[] address, byte[] key, byte[] value) {
        createAccount(address);
        getAccount(address).storage.put(new ByteArrayWrapper(key), value);
    }

    @Override
    public byte[] getStorage(byte[] address, byte[] key) {
        AccountState acc = getAccount(address);
        return acc == null ? null : acc.storage.get(new ByteArrayWrapper(key));
    }

    @Override
    public void deleteAccount(byte[] address) {
        accounts.remove(new ByteArrayWrapper(address));
    }

    @Override
    public long getBalance(byte[] address) {
        AccountState acc = getAccount(address);
        return acc == null ? 0 : acc.balance;
    }

    @Override
    public void adjustBalance(byte[] address, long delta) {
        createAccount(address);
        getAccount(address).balance += delta;
    }

    @Override
    public long getNonce(byte[] address) {
        AccountState acc = getAccount(address);
        return acc == null ? 0 : acc.nonce;
    }

    @Override
    public void incrementNonce(byte[] address) {
        createAccount(address);
        getAccount(address).nonce++;
    }

    @Override
    public Map<ByteArrayWrapper, byte[]> getStorageEntries(byte[] address) {
        AccountState acc = getAccount(address);
        if (acc == null) {
            return Collections.emptyMap();
        }

        return new HashMap<>(acc.storage);
    }
}
