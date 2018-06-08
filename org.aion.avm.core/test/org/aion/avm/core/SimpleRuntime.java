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
        Assert.assertNotNull(sender);
        Assert.assertNotNull(address);
        
        this.sender = new Address(sender);
        this.address = new Address(address);
        this.energyLimit = energyLimit;
    }
    @Override
    public Address avm_getSender() {
        return this.sender;
    }
    @Override
    public Address avm_getAddress() {
        return this.address;
    }
    @Override
    public long avm_getEnergyLimit() {
        return this.energyLimit;
    }
    @Override
    public ByteArray avm_getData() {
        Assert.fail("This implementation doesn't handle this");
        return null;
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
}
