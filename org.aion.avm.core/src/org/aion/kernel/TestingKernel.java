package org.aion.kernel;

import java.math.BigInteger;
import org.aion.avm.core.util.Helpers;
import org.aion.data.DirectoryBackedDataStore;
import org.aion.data.IAccountStore;
import org.aion.data.IDataStore;
import org.aion.data.MemoryBackedDataStore;
import org.aion.types.Address;

import java.io.File;
import org.aion.vm.api.interfaces.KernelInterface;


/**
 * A modified version of CachingKernel to support more general usage so it can be used as the kernel underlying tests.
 */
public class TestingKernel implements KernelInterface {
    /**
     * For testing purposes, we will give every contract address this prefix.
     */
    public static final byte AVM_CONTRACT_PREFIX = 0x0b;

    public static final Address PREMINED_ADDRESS = Address.wrap(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f725efc06378"));
    public static final Address BIG_PREMINED_ADDRESS = Address.wrap(Helpers.hexStringToBytes("a035f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f725efc06378"));
    public static final BigInteger PREMINED_AMOUNT = BigInteger.TEN.pow(18);
    public static final BigInteger PREMINED_BIG_AMOUNT = BigInteger.valueOf(465000000).multiply(PREMINED_AMOUNT);

    private BigInteger blockDifficulty;
    private long blockNumber;
    private long blockTimestamp;
    private long blockNrgLimit;
    private Address blockCoinbase;

    private final IDataStore dataStore;

    /**
     * Creates an instance of the interface which is backed by in-memory structures, only.
     */
    public TestingKernel() {
        this.dataStore = new MemoryBackedDataStore();
        IAccountStore premined = this.dataStore.createAccount(PREMINED_ADDRESS.toBytes());
        premined.setBalance(PREMINED_AMOUNT);
        premined = this.dataStore.createAccount(BIG_PREMINED_ADDRESS.toBytes());
        premined.setBalance(PREMINED_BIG_AMOUNT);
        this.blockDifficulty = BigInteger.valueOf(10_000_000L);
        this.blockNumber = 1;
        this.blockTimestamp = System.currentTimeMillis();
        this.blockNrgLimit = 10_000_000L;
        this.blockCoinbase = Helpers.randomAddress();
    }

    /**
     * Creates an instance of the interface which is backed by in-memory structures, only.
     */
    public TestingKernel(Block block) {
        this.dataStore = new MemoryBackedDataStore();
        IAccountStore premined = this.dataStore.createAccount(PREMINED_ADDRESS.toBytes());
        premined.setBalance(PREMINED_AMOUNT);
        premined = this.dataStore.createAccount(BIG_PREMINED_ADDRESS.toBytes());
        premined.setBalance(PREMINED_BIG_AMOUNT);
        this.blockDifficulty = block.getDifficulty();
        this.blockNumber = block.getNumber();
        this.blockTimestamp = block.getTimestamp();
        this.blockNrgLimit = block.getEnergyLimit();
        this.blockCoinbase = block.getCoinbase();
    }

    /**
     * Creates an instance of the interface which is backed by a directory on disk.
     * 
     * @param onDiskRoot The root directory which this implementation will use for persistence.
     * @param block The top block of the current state of this kernel.
     */
    public TestingKernel(File onDiskRoot, Block block) {
        this.dataStore = new DirectoryBackedDataStore(onDiskRoot);
        // Try to open the account, creating it if doesn't exist.
        IAccountStore premined = this.dataStore.openAccount(PREMINED_ADDRESS.toBytes());
        if (null == premined) {
            premined = this.dataStore.createAccount(PREMINED_ADDRESS.toBytes());
        }
        premined.setBalance(PREMINED_AMOUNT);
        this.blockDifficulty = block.getDifficulty();
        this.blockNumber = block.getNumber();
        this.blockTimestamp = block.getTimestamp();
        this.blockNrgLimit = block.getEnergyLimit();
        this.blockCoinbase = block.getCoinbase();
    }

    @Override
    public KernelInterface makeChildKernelInterface() {
        return new TransactionalKernel(this);
    }

    @Override
    public void commit() {
        throw new AssertionError("This class does not implement this method.");
    }

    @Override
    public void commitTo(KernelInterface target) {
        throw new AssertionError("This class does not implement this method.");
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {
        throw new AssertionError("No equivalent concept in the Avm.");
    }

    @Override
    public void removeStorage(Address address, byte[] key) {
        throw new AssertionError("This class does not implement this method.");
    }

    @Override
    public void createAccount(Address address) {
        this.dataStore.createAccount(address.toBytes());
    }

    @Override
    public boolean hasAccountState(Address address) {
        return this.dataStore.openAccount(address.toBytes()) != null;
    }

    @Override
    public byte[] getCode(Address address) {
        // getCode is an interface for fvm, the avm should not call this method.
        throw new AssertionError("This class does not implement this method.");
    }

    @Override
    public void putCode(Address address, byte[] code) {
        lazyCreateAccount(address.toBytes()).setCode(code);
    }

    @Override
    public byte[] getTransformedCode(Address address) {
        return internalGetCode(address);
    }

    @Override
    public void setTransformedCode(Address address, byte[] bytes) {
        lazyCreateAccount(address.toBytes()).setTransformedCode(bytes);
    }

    @Override
    public void putObjectGraph(Address address, byte[] bytes) {
        lazyCreateAccount(address.toBytes()).setObjectGraph(bytes);
    }

    @Override
    public byte[] getObjectGraph(Address address) {
        return lazyCreateAccount(address.toBytes()).getObjectGraph();
    }

    @Override
    public void putStorage(Address address, byte[] key, byte[] value) {
        lazyCreateAccount(address.toBytes()).setData(key, value);
    }

    @Override
    public byte[] getStorage(Address address, byte[] key) {
        IAccountStore account = this.dataStore.openAccount(address.toBytes());
        return (null != account)
                ? account.getData(key)
                : null;
    }

    @Override
    public void deleteAccount(Address address) {
        this.dataStore.deleteAccount(address.toBytes());
    }

    @Override
    public BigInteger getBalance(Address address) {
        IAccountStore account = this.dataStore.openAccount(address.toBytes());
        return (null != account)
                ? account.getBalance()
                : BigInteger.ZERO;
    }

    @Override
    public void adjustBalance(Address address, BigInteger delta) {
        internalAdjustBalance(address, delta);
    }

    @Override
    public BigInteger getNonce(Address address) {
        IAccountStore account = this.dataStore.openAccount(address.toBytes());
        return (null != account)
                ? BigInteger.valueOf(account.getNonce())
                : BigInteger.ZERO;
    }

    @Override
    public void incrementNonce(Address address) {
        IAccountStore account = lazyCreateAccount(address.toBytes());
        long start = account.getNonce();
        account.setNonce(start + 1);
    }

    @Override
    public boolean accountNonceEquals(Address address, BigInteger nonce) {
        return nonce.compareTo(this.getNonce(address)) == 0;
    }

    @Override
    public boolean accountBalanceIsAtLeast(Address address, BigInteger amount) {
        return this.getBalance(address).compareTo(amount) >= 0;
    }

    @Override
    public boolean isValidEnergyLimitForCreate(long energyLimit) {
        return energyLimit > 0;
    }

    @Override
    public boolean isValidEnergyLimitForNonCreate(long energyLimit) {
        return energyLimit > 0;
    }

    private IAccountStore lazyCreateAccount(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        if (null == account) {
            account = this.dataStore.createAccount(address);
        }
        return account;
    }

    @Override
    public boolean destinationAddressIsSafeForThisVM(Address address) {
        // This implementation knows about contract address prefixes (just used by tests - real kernel stores out-of-band meta-data).
        // So, it is valid to use any regular address or AVM contract address.
        byte[] code = internalGetCode(address);
        return (code == null) || (address.toBytes()[0] == TestingKernel.AVM_CONTRACT_PREFIX);
    }

    @Override
    public long getBlockNumber() {
        return blockNumber;
    }

    @Override
    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    @Override
    public long getBlockEnergyLimit() {
        return blockNrgLimit;
    }

    @Override
    public long getBlockDifficulty() {
        return blockDifficulty.longValue();
    }

    @Override
    public Address getMinerAddress() {
        return blockCoinbase;
    }

    @Override
    public void refundAccount(Address address, BigInteger amount) {
        // This method may have special logic in the kernel. Here it is just adjustBalance.
        internalAdjustBalance(address, amount);
    }

    @Override
    public void deductEnergyCost(Address address, BigInteger cost) {
        // This method may have special logic in the kernel. Here it is just adjustBalance.
        internalAdjustBalance(address, cost);
    }

    @Override
    public void payMiningFee(Address address, BigInteger fee) {
        // This method may have special logic in the kernel. Here it is just adjustBalance.
        internalAdjustBalance(address, fee);
    }

    public void updateBlock(Block block) {
        this.blockDifficulty = block.getDifficulty();
        this.blockNumber = block.getNumber();
        this.blockTimestamp = block.getTimestamp();
        this.blockNrgLimit = block.getEnergyLimit();
        this.blockCoinbase = block.getCoinbase();
    }


    private void internalAdjustBalance(Address address, BigInteger delta) {
        IAccountStore account = lazyCreateAccount(address.toBytes());
        BigInteger start = account.getBalance();
        account.setBalance(start.add(delta));
    }

    private byte[] internalGetCode(Address address) {
        IAccountStore account = this.dataStore.openAccount(address.toBytes());
        return (null != account)
                ? account.getTransformedCode()
                : null;
    }
}
