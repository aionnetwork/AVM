package org.aion.avm.core.persistence;

import org.aion.avm.core.persistence.keyvalue.StorageKeys;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Represents the part of the contract environment which must be persistent between invocations.
 */
public class ContractEnvironmentState {
    public static ContractEnvironmentState loadFromGraph(IObjectGraphStore graphStore) {
        byte[] rawData = graphStore.getStorage(StorageKeys.CONTRACT_ENVIRONMENT);
        RuntimeAssertionError.assertTrue(rawData.length == (Integer.BYTES + Long.BYTES));
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(rawData);
        int nextHashCode = decoder.decodeInt();
        long nextInstanceId = decoder.decodeLong();
        return new ContractEnvironmentState(nextHashCode, nextInstanceId);
    }

    public static void saveToGraph(IObjectGraphStore graphStore, ContractEnvironmentState state) {
        byte[] bytes = new StreamingPrimitiveCodec.Encoder()
                .encodeInt(state.nextHashCode)
                .encodeLong(state.nextInstanceId)
                .toBytes();
        graphStore.putStorage(StorageKeys.CONTRACT_ENVIRONMENT, bytes);
    }


    public final int nextHashCode;
    public final long nextInstanceId;

    public ContractEnvironmentState(int nextHashCode, long nextInstanceId) {
        this.nextHashCode = nextHashCode;
        this.nextInstanceId = nextInstanceId;
    }
}
