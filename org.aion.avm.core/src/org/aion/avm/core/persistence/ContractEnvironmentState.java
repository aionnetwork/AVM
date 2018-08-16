package org.aion.avm.core.persistence;

import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.KernelInterface;


/**
 * Represents the part of the contract environment which must be persistent between invocations.
 */
public class ContractEnvironmentState {
    public static ContractEnvironmentState loadFromStorage(KernelInterface kernel, byte[] address) {
        byte[] rawData = kernel.getStorage(address, StorageKeys.CONTRACT_ENVIRONMENT);
        RuntimeAssertionError.assertTrue(rawData.length == (Integer.BYTES + Long.BYTES));
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(rawData);
        int nextHashCode = decoder.decodeInt();
        long nextInstanceId = decoder.decodeLong();
        return new ContractEnvironmentState(nextHashCode, nextInstanceId);
    }

    public static void saveToStorage(KernelInterface kernel, byte[] address, ContractEnvironmentState state) {
        byte[] bytes = new StreamingPrimitiveCodec.Encoder()
                .encodeInt(state.nextHashCode)
                .encodeLong(state.nextInstanceId)
                .toBytes();
        kernel.putStorage(address, StorageKeys.CONTRACT_ENVIRONMENT, bytes);
    }


    public final int nextHashCode;
    public final long nextInstanceId;

    public ContractEnvironmentState(int nextHashCode, long nextInstanceId) {
        this.nextHashCode = nextHashCode;
        this.nextInstanceId = nextInstanceId;
    }
}
