package org.aion.kernel;

import java.math.BigInteger;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.data.IAccountStore;
import org.aion.data.IDataStore;
import org.aion.data.MemoryBackedDataStore;
import org.aion.types.Address;

import org.aion.vm.api.interfaces.KernelInterface;


/**
 * In in-memory cached used by the TransactionalKernel in order to store results of in-flight transactions prior to commit.
 */
public class CachingKernel implements KernelInterface {
    private final IDataStore dataStore;

    /**
     * Creates an instance which is backed by in-memory structures, only.
     */
    public CachingKernel() {
        this.dataStore = new MemoryBackedDataStore();
    }

    @Override
    public KernelInterface makeChildKernelInterface() {
        // While this kind of kernel could support children, the use-case would be an error, based on what this implementation is for.
        throw RuntimeAssertionError.unreachable("Caching kernel should never be asked to create children.");
    }

    @Override
    public void commit() {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
    }

    @Override
    public void commitTo(KernelInterface target) {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
    }

    @Override
    public byte[] getBlockHashByNumber(long blockNumber) {
        throw RuntimeAssertionError.unreachable("No equivalent concept in the Avm.");
    }

    @Override
    public void removeStorage(Address address, byte[] key) {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
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
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
    }

    @Override
    public void putCode(Address address, byte[] code) {
        // Note that saving empty code is invalid since a valid JAR is not empty.
        RuntimeAssertionError.assertTrue((null != code) && (code.length > 0));
        lazyCreateAccount(address.toBytes()).setCode(code);
    }

    @Override
    public byte[] getTransformedCode(Address address) {
        IAccountStore account = this.dataStore.openAccount(address.toBytes());
        return (null != account)
            ? account.getTransformedCode()
            : null;
    }

    @Override
    public void setTransformedCode(Address address, byte[] code) {
        RuntimeAssertionError.assertTrue((null != code) && (code.length > 0));
        lazyCreateAccount(address.toBytes()).setTransformedCode(code);
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

    @Override
    public boolean destinationAddressIsSafeForThisVM(Address address) {
        // This implementation knows nothing of other VMs so it could only ever return true.
        // Since that is somewhat misleading (it assumes it is making a decision based on something), it is more reliable to just never call it.
        throw RuntimeAssertionError.unreachable("Caching kernel knows nothing of other VMs.");
    }

    @Override
    public long getBlockNumber() {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
    }

    @Override
    public long getBlockTimestamp() {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
    }

    @Override
    public long getBlockEnergyLimit() {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
    }

    @Override
    public long getBlockDifficulty() {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
    }

    @Override
    public Address getMinerAddress() {
        throw RuntimeAssertionError.unreachable("This class does not implement this method.");
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


    private IAccountStore lazyCreateAccount(byte[] address) {
        IAccountStore account = this.dataStore.openAccount(address);
        if (null == account) {
            account = this.dataStore.createAccount(address);
        }
        return account;
    }

    private void internalAdjustBalance(Address address, BigInteger delta) {
        IAccountStore account = lazyCreateAccount(address.toBytes());
        BigInteger start = account.getBalance();
        account.setBalance(start.add(delta));
    }
}
