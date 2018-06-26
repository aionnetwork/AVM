package org.aion.avm.core;

import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.api.Address;


/**
 * This exists to resolve a chicken-and-egg problem found in issue-77:  the runtime object we pass to the contract code must be part
 * of the shadow hierarchy, but nothing in the shadow hierarchy can be instantiated until the runtime is installed.
 * We resolve this by installing an "external runtime", which is an object with no special nature.  We can then create this wrapper,
 * which is part of the shadow hierarchy, and pass that into the contract code.
 */
public class ContractRuntimeWrapper extends org.aion.avm.shadow.java.lang.Object implements IBlockchainRuntime {
    private final IBlockchainRuntime externalRuntime;

    public ContractRuntimeWrapper(IBlockchainRuntime externalRuntime) {
        this.externalRuntime = externalRuntime;
    }
    @Override
    public Address avm_getSender() {
        return this.externalRuntime.avm_getSender();
    }
    @Override
    public Address avm_getAddress() {
        return this.externalRuntime.avm_getAddress();
    }
    @Override
    public long avm_getEnergyLimit() {
        return this.externalRuntime.avm_getEnergyLimit();
    }
    @Override
    public ByteArray avm_getData() {
        return this.externalRuntime.avm_getData();
    }
    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        return this.externalRuntime.avm_getStorage(key);
    }
    @Override
    public void avm_putStorage(ByteArray key, ByteArray value) {
        this.externalRuntime.avm_putStorage(key, value);
    }
    @Override
    public void avm_updateCode(ByteArray newCode) {
        this.externalRuntime.avm_updateCode(newCode);
    }
    @Override
    public void avm_selfDestruct(Address beneficiary) {
        this.externalRuntime.avm_selfDestruct(beneficiary);
    }
    @Override
    public long avm_getBlockEpochSeconds() {
        return this.externalRuntime.avm_getBlockEpochSeconds();
    }
    @Override
    public long avm_getBlockNumber() {
        return this.externalRuntime.avm_getBlockNumber();
    }
    @Override
    public ByteArray avm_sha3(ByteArray data) {
        return this.externalRuntime.avm_sha3(data);
    }
    @Override
    public ByteArray avm_call(Address targetAddress, ByteArray value, ByteArray payload, long energyToSend) {
        return this.externalRuntime.avm_call(targetAddress, null, payload, energyToSend);
    }
    @Override
    public void avm_log(ByteArray index0, ByteArray data) {
        this.externalRuntime.avm_log(index0, data);
    }
}
