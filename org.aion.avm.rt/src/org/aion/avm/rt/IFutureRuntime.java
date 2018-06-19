package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;


/**
 * We define this as an extension of BlockchainRuntime since these mechanisms probably need to be pushed back into it, at some point.
 * 
 * TODO:  Refactor this into BlockchainRuntime and then remove it as part of issue-102.
 */
public interface IFutureRuntime extends BlockchainRuntime {
    // Runtime-facing implementation.
    public long avm_getBlockEpochSeconds();

    public ByteArray avm_getMessageData();

    public long avm_getBlockNumber();

    // Note that this response is always 32 bytes.  User-space might want to wrap it.  Should we wrap it on the runtime interface level?
    public ByteArray avm_sha3(ByteArray data);

    public ByteArray avm_call(Address targetAddress, long energyToSend, ByteArray payload);


    // Compiler-facing implementation.
    public default long getBlockEpochSeconds() { return avm_getBlockEpochSeconds(); }

    public default byte[] getMessageData() { return avm_getMessageData().getUnderlying(); }

    public default long getBlockNumber() { return avm_getBlockNumber(); }

    // Note that this response is always 32 bytes.  User-space might want to wrap it.  Should we wrap it on the runtime interface level?
    public default byte[] sha3(byte[] data) { return avm_sha3(new ByteArray(data)).getUnderlying(); }

    public default byte[] call(Address targetAddress, long energyToSend, byte[] payload) { return avm_call(targetAddress, energyToSend, new ByteArray(payload)).getUnderlying(); }
}
