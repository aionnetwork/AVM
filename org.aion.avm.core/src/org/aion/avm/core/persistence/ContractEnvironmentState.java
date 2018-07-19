package org.aion.avm.core.persistence;

import org.aion.avm.core.util.Assert;
import org.aion.kernel.TransactionContext;


/**
 * Represents the part of the contract environment which must be persistent between invocations.
 */
public class ContractEnvironmentState {
    public static ContractEnvironmentState loadFromStorage(TransactionContext cb, byte[] address) {
        byte[] rawData = cb.getStorage(address, StorageKeys.CONTRACT_ENVIRONMENT);
        Assert.assertTrue(rawData.length == (Integer.BYTES + Long.BYTES));
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(rawData);
        int nextHashCode = decoder.decodeInt();
        long nextInstanceId = decoder.decodeLong();
        return new ContractEnvironmentState(nextHashCode, nextInstanceId);
    }

    public static void saveToStorage(TransactionContext cb, byte[] address, ContractEnvironmentState state) {
        byte[] bytes = new StreamingPrimitiveCodec.Encoder()
                .encodeInt(state.nextHashCode)
                .encodeLong(state.nextInstanceId)
                .toBytes();
        cb.putStorage(address, StorageKeys.CONTRACT_ENVIRONMENT, bytes);
    }


    public final int nextHashCode;
    public final long nextInstanceId;

    public ContractEnvironmentState(int nextHashCode, long nextInstanceId) {
        this.nextHashCode = nextHashCode;
        this.nextInstanceId = nextInstanceId;
    }
}
