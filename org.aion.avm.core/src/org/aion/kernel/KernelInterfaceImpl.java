package org.aion.kernel;

import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.Helpers;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class KernelInterfaceImpl implements KernelInterface {

    public static final byte[] PREMINED_ADDRESS = Helpers.randomBytes(32);
    public static final BigInteger PREMINED_AMOUNT = BigInteger.valueOf(500).multiply(BigInteger.TEN.pow(18));

    // shared across-context
    private static Map<ByteArrayWrapper, VersionedCode> dappCodeDB = new ConcurrentHashMap<>();
    private static Map<ByteArrayWrapper, byte[]> dappStorageDB = new ConcurrentHashMap<>();

    // account states, live within this instance
    private Map<ByteArrayWrapper, AccountState> accounts = new HashMap<>();

    public KernelInterfaceImpl() {
        accounts.put(new ByteArrayWrapper(PREMINED_ADDRESS), new AccountState(PREMINED_AMOUNT, 0));
    }

    @Override
    public void putCode(byte[] address, VersionedCode code) {
        dappCodeDB.put(new ByteArrayWrapper(address), code);
    }

    @Override
    public VersionedCode getCode(byte[] address) {
        return dappCodeDB.get(new ByteArrayWrapper(address));
    }

    @Override
    public void putStorage(byte[] address, byte[] key, byte[] value) {
        ByteArrayWrapper k = new ByteArrayWrapper(Helpers.merge(address, key));
        dappStorageDB.put(k, value);
    }

    @Override
    public byte[] getStorage(byte[] address, byte[] key) {
        ByteArrayWrapper k = new ByteArrayWrapper(Helpers.merge(address, key));
        return dappStorageDB.get(k);
    }

    @Override
    public void createAccount(byte[] address) {
        accounts.put(new ByteArrayWrapper(address), new AccountState());
    }

    @Override
    public void deleteAccount(byte[] address) {
        accounts.remove(new ByteArrayWrapper(address));
    }

    @Override
    public boolean isExists(byte[] address) {
        return accounts.containsKey(new ByteArrayWrapper(address));
    }

    @Override
    public BigInteger getBalance(byte[] address) {
        return accounts.getOrDefault(new ByteArrayWrapper(address), new AccountState()).balance;
    }

    @Override
    public void adjustBalance(byte[] address, BigInteger delta) {
        // NOTE: account is being created
        AccountState as = accounts.computeIfAbsent(new ByteArrayWrapper(address), k -> new AccountState());
        as.balance = as.balance.add(delta);
    }

    @Override
    public long getNonce(byte[] address) {
        return accounts.getOrDefault(new ByteArrayWrapper(address), new AccountState()).nonce;
    }

    @Override
    public void incrementNonce(byte[] address) {
        // NOTE: account is being created
        AccountState as = accounts.computeIfAbsent(new ByteArrayWrapper(address), k -> new AccountState());
        as.nonce++;
    }
}
