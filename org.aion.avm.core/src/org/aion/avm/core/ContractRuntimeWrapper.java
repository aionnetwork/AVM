package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.java.lang.String;
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
    public void avm_updateCode(ByteArray newCode, String codeVersion) {
        this.externalRuntime.avm_updateCode(newCode, codeVersion);
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
    public ByteArray avm_getMessageData() {
        return this.externalRuntime.avm_getMessageData();
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
    public ByteArray avm_call(Address targetAddress, long energyToSend, ByteArray payload) {
        return this.externalRuntime.avm_call(targetAddress, energyToSend, payload);
    }
}
