package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.api.Address;
import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.shadow.java.math.BigInteger;
import org.junit.Assert;



/**
 * A minimal implementation of IBlockchainRuntime sufficient for our current class of tests.
 * These provide only the direct inputs, none of the interactive data layer.
 */
public class SimpleRuntime implements IBlockchainRuntime {
    private final byte[] sender;
    private final byte[] address;
    private final long energyLimit;
    private final byte[] txData;

    // We can't eagerly create these addresses, since the IHelper isn't yet installed, but we do want to reuse the same instance, once we create it.
    private Address cachedSender;
    private Address cachedAddress;
    private ByteArray cachedTxData;

    public SimpleRuntime(byte[] sender, byte[] address, long energyLimit) {
        Assert.assertNotNull(sender);
        Assert.assertNotNull(address);

        this.sender = sender;
        this.address = address;
        this.energyLimit = energyLimit;
        this.txData = null;
    }

    public SimpleRuntime(byte[] sender, byte[] address, long energyLimit, byte[] txData) {
        Assert.assertNotNull(sender);
        Assert.assertNotNull(address);
        
        this.sender = sender;
        this.address = address;
        this.energyLimit = energyLimit;
        this.txData = txData;
    }
    @Override
    public Address avm_getCaller() {
        if (null == this.cachedSender) {
            this.cachedSender = new Address(this.sender);
        }
        return this.cachedSender;
    }

    @Override
    public Address avm_getOrigin() {
        return null;
    }

    @Override
    public Address avm_getAddress() {
        if (null == this.cachedAddress) {
            this.cachedAddress = new Address(this.address);
        }
        return this.cachedAddress;
    }
    @Override
    public long avm_getEnergyLimit() {
        return this.energyLimit;
    }

    @Override
    public long avm_getEnergyPrice() {
        return 1;
    }

    @Override
    public long avm_getValue() {
        return 0;
    }

    @Override
    public ByteArray avm_getData() {
        if (null == this.txData) {
            return null;
        }
        if (null == this.cachedTxData) {
            this.cachedTxData = new ByteArray(this.txData);
        }
        return cachedTxData;
    }
    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }
    @Override
    public void avm_putStorage(ByteArray key, ByteArray value) {
        Assert.fail("This implementation doesn't handle this");
    }

    @Override
    public long avm_getBalance(Address address) {
        return 0;
    }

    @Override
    public int avm_getCodeSize(Address address) {
        return 0;
    }

    @Override
    public long avm_getRemainingEnergy() {
        return 0;
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        Assert.fail("This implementation doesn't handle this");
    }
    @Override
    public long avm_getBlockTimestamp() {
        Assert.fail("This implementation doesn't handle this");
        return 0;
    }

    @Override
    public long avm_getBlockNumber() {
        Assert.fail("This implementation doesn't handle this");
        return 0;
    }

    @Override
    public long avm_getBlockEnergyLimit() {
        return 0;
    }

    @Override
    public Address avm_getBlockCoinbase() {
        return null;
    }

    @Override
    public ByteArray avm_getBlockPreviousHash() {
        return null;
    }

    @Override
    public BigInteger avm_getBlockDifficulty() {
        return null;
    }

    @Override
    public ByteArray avm_blake2b(ByteArray data) {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }

    @Override
    public ByteArray avm_call(Address targetAddress, long value, ByteArray payload, long energyToSend) {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }

    @Override
    public Address avm_create(long value, ByteArray data, long energyToSend) {
        return null;
    }

    @Override
    public void avm_log(ByteArray data) {

    }

    @Override
    public void avm_log(ByteArray index0, ByteArray data) {
        Assert.fail("This implementation doesn't handle this");
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {

    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {

    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {

    }
}
