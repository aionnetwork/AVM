package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.java.lang.String;

public class BlockchainRuntimeImpl implements BlockchainRuntime {
    private final IAvmProxy avm;
    private final byte[] sender;
    private final byte[] address;
    private final long energyLimit;
    private final byte[] txData;

    // We can't eagerly create these addresses, since the IHelper isn't yet installed, but we do want to reuse the same instance, once we create it.
    private Address cachedSender;
    private Address cachedAddress;
    private ByteArray cachedTxData;

    public BlockchainRuntimeImpl(byte[] sender, byte[] address, long energyLimit, byte[] txData, IAvmProxy avm) {
        this.sender = sender;
        this.address = address;
        this.energyLimit = energyLimit;
        this.txData = txData;
        this.avm = avm;
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
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }

    @Override
    public void avm_putStorage(ByteArray key, ByteArray value) {
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }

    @Override
    public void avm_updateCode(ByteArray newCode, String codeVersion) {
        avm.deploy(newCode.getUnderlying(), codeVersion,this);
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        avm.removeDapp(beneficiary, this);
    }

    @Override
    public long avm_getBlockEpochSeconds() {
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }

    @Override
    public ByteArray avm_getMessageData() {
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }

    @Override
    public long avm_getBlockNumber() {
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }

    @Override
    public ByteArray avm_sha3(ByteArray data) {
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }

    @Override
    public ByteArray avm_call(Address targetAddress, long energyToSend, ByteArray payload) {
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }

    @Override
    public void avm_log(ByteArray index0, ByteArray data) {
        throw new RuntimeAssertionError("This implementation doesn't handle this");
    }
}
