package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.java.lang.String;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.junit.Assert;



/**
 * A minimal implementation of BlockchainRuntime sufficient for our current class of tests.
 * These provide only the direct inputs, none of the interactive data layer.
 */
public class SimpleRuntime implements BlockchainRuntime {
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
    public Address avm_getSender() {
        if (null == this.cachedSender) {
            this.cachedSender = new Address(this.sender);
        }
        return this.cachedSender;
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
    public void avm_updateCode(ByteArray newCode, String codeVersion) {
        Assert.fail("This implementation doesn't handle this");
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        Assert.fail("This implementation doesn't handle this");
    }
    @Override
    public long avm_getBlockEpochSeconds() {
        Assert.fail("This implementation doesn't handle this");
        return 0;
    }

    @Override
    public ByteArray avm_getMessageData() {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }

    @Override
    public long avm_getBlockNumber() {
        Assert.fail("This implementation doesn't handle this");
        return 0;
    }

    @Override
    public ByteArray avm_sha3(ByteArray data) {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }

    @Override
    public ByteArray avm_call(Address targetAddress, long energyToSend, ByteArray payload) {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }
}
