package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;
import org.junit.Assert;


/**
 * A minimal implementation of BlockchainRuntime sufficient for our current class of tests.
 * These provide only the direct inputs, none of the interactive data layer.
 */
public class SimpleRuntime implements BlockchainRuntime {
    private final Address sender;
    private final Address address;
    private final long energyLimit;

    public SimpleRuntime(byte[] sender, byte[] address, long energyLimit) {
        // TODO:  Change this handling of null into asserts once our tests pass in real values.
        this.sender = (null != sender) ? new Address(sender) : null;
        this.address = (null != address) ? new Address(address) : null;
        this.energyLimit = energyLimit;
    }
    @Override
    public Address getSender() {
        return this.sender;
    }
    @Override
    public Address getAddress() {
        return this.address;
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
