package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.rt.BlockchainRuntime;
import org.junit.Assert;


/**
 * A minimal implementation of BlockchainRuntime sufficient for our current class of tests.
 * These provide only the direct inputs, none of the interactive data layer.
 */
public class SimpleRuntime implements BlockchainRuntime {
    private final byte[] sender;
    private final byte[] address;
    private final long energyLimit;

    public SimpleRuntime(byte[] sender, byte[] address, long energyLimit) {
        this.sender = sender;
        this.address = address;
        this.energyLimit = energyLimit;
    }
    @Override
    public ByteArray getSender() {
        return new ByteArray(this.sender);
    }
    @Override
    public ByteArray getAddress() {
        return new ByteArray(this.address);
    }
    @Override
    public long getEnergyLimit() {
        return this.energyLimit;
    }
    @Override
    public ByteArray getData() {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }
    @Override
    public ByteArray getStorage(ByteArray key) {
        Assert.fail("This implementation doesn't handle this");
        return null;
    }
    @Override
    public void putStorage(ByteArray key, ByteArray value) {
        Assert.fail("This implementation doesn't handle this");
    }
}
