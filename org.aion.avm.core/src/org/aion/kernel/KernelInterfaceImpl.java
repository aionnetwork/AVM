package org.aion.kernel;

import java.math.BigInteger;
import org.aion.avm.core.util.Helpers;
import org.aion.data.DirectoryBackedDataStore;
import org.aion.data.IAccountStore;
import org.aion.data.IDataStore;
import org.aion.data.MemoryBackedDataStore;

import java.io.File;


/**
 * Mostly just a high-level wrapper around and underlying IDataStore.
 * Note that this implementation implicitly creates accounts in response to mutative operations.  They are not explicitly created.
 * Likewise, reading data from a non-existent account safely returns null or 0L, rather than failing.
 */
public class KernelInterfaceImpl implements KernelInterface {

    public static final byte[] PREMINED_ADDRESS = Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f725efc06378");
    public static final BigInteger PREMINED_AMOUNT = BigInteger.TEN.pow(18);

    private final IDataStore dataStore;

    /**
     * Creates an instance of the interface which is backed by in-memory structures, only.
     */
    public KernelInterfaceImpl() {
        this.dataStore = new MemoryBackedDataStore();
        IAccountStore premined = this.dataStore.createAccount(PREMINED_ADDRESS);
        premined.setBalance(PREMINED_AMOUNT);
    }

    /**
     * Creates an instance of the interface which is backed by a directory on disk.
     * 
     * @param onDiskRoot The root directory which this implementation will use for persistence.
     */
    public KernelInterfaceImpl(File onDiskRoot) {
        this.dataStore = new DirectoryBackedDataStore(onDiskRoot);
        // Try to open the account, creating it if doesn't exist.
        IAccountStore premined = this.dataStore.openAccount(PREMINED_ADDRESS);
        if (null == premined) {
            premined = this.dataStore.createAccount(PREMINED_ADDRESS);
        }
        premined.setBalance(PREMINED_AMOUNT);
    }

    @Override
    public void createAccount(byte[] address) {
        this.dataStore.createAccount(address);
    }

    @Override
    public boolean hasAccountState(byte[] address) {
        return this.dataStore.openAccount(address) != null;
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
    public BigInteger getBalance(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        return (null != account)
                ? account.getBalance()
                : BigInteger.ZERO;
    }

    @Override
    public void adjustBalance(byte[] address, BigInteger delta) {
        IAccountStore account = lazyCreateAccount(address);
        BigInteger start = account.getBalance();
        account.setBalance(start.add(delta));
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
    public boolean accountNonceEquals(byte[] address, long nonce) {
        return nonce == this.getNonce(address);
    }

    @Override
    public boolean accountBalanceIsAtLeast(byte[] address, BigInteger amount) {
        return this.getBalance(address).compareTo(amount) >= 0;
    }

    @Override
    public boolean isValidEnergyLimit(long energyLimit) {
        return energyLimit > 0;
    }

    private IAccountStore lazyCreateAccount(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        if (null == account) {
            account = this.dataStore.createAccount(address);
        }
        return account;
    }
}
