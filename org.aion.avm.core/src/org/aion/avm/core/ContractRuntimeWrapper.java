package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;


/**
 * This exists to resolve a chicken-and-egg problem found in issue-77:  the runtime object we pass to the contract code must be part
 * of the shadow hierarchy, but nothing in the shadow hierarchy can be instantiated until the runtime is installed.
 * We resolve this by installing an "external runtime", which is an object with no special nature.  We can then create this wrapper,
 * which is part of the shadow hierarchy, and pass that into the contract code.
 */
public class ContractRuntimeWrapper extends org.aion.avm.java.lang.Object implements BlockchainRuntime {
    private final BlockchainRuntime externalRuntime;

    public ContractRuntimeWrapper(BlockchainRuntime externalRuntime) {
        this.externalRuntime = externalRuntime;
    }
    @Override
    public Address getSender() {
        return this.externalRuntime.getSender();
    }
    @Override
    public Address getAddress() {
        return this.externalRuntime.getAddress();
    }
    @Override
    public long getEnergyLimit() {
        return this.externalRuntime.getEnergyLimit();
    }
    @Override
    public ByteArray getData() {
        return this.externalRuntime.getData();
    }
    @Override
    public ByteArray getStorage(ByteArray key) {
        return this.externalRuntime.getStorage(key);
    }
    @Override
    public void putStorage(ByteArray key, ByteArray value) {
        this.externalRuntime.putStorage(key, value);
    }
}
