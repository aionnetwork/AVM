package org.aion.avm.core;

import org.aion.avm.api.Address;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.aion.avm.api.BlockchainRuntime.*;

public class BlockchainRuntimeTestResource {

    public static byte[] main() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(getAddress().unwrap());
        buffer.put(getCaller().unwrap());
        buffer.put(getOrigin().unwrap());
        buffer.putLong(getEnergyLimit());
        buffer.putLong(getEnergyPrice());
        buffer.putLong(getValue());
        buffer.put(getData());

        buffer.putLong(getBlockTimestamp());
        buffer.putLong(getBlockNumber());
        buffer.putLong(getBlockEnergyLimit());
        buffer.put(getBlockCoinbase().unwrap());
        buffer.put(getBlockPreviousHash());
        buffer.put(getBlockDifficulty().toByteArray());

        putStorage("key".getBytes(), "value".getBytes());
        buffer.put(getStorage("key".getBytes()));
        buffer.putLong(getBalance(getAddress()));
        buffer.putLong(getCodeSize(getAddress()));

        getRemainingEnergy();
        call(new Address(new byte[32]), 0, new byte[0], 0);
        create(0, new byte[0], 0);
        // selfDestruct(new Address(new byte[32]));

        log("data".getBytes());
        log("topic1".getBytes(), "data".getBytes());
        log("topic1".getBytes(), "topic2".getBytes(), "data".getBytes());
        log("topic1".getBytes(), "topic2".getBytes(), "topic3".getBytes(), "data".getBytes());
        log("topic1".getBytes(), "topic2".getBytes(), "topic3".getBytes(), "topic4".getBytes(), "data".getBytes());

        buffer.put(blake2b("message".getBytes()));

        println("message");

        print("message");

        return Arrays.copyOfRange(buffer.array(), 0, buffer.position());
    }
}
