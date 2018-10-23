package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.userlib.AionBuffer;

import java.util.Arrays;

import static org.aion.avm.api.BlockchainRuntime.*;

public class BlockchainRuntimeTestResource {

    public static byte[] main() {
        AionBuffer buffer = AionBuffer.allocate(1024);
        buffer.put(getAddress().unwrap());
        buffer.put(getCaller().unwrap());
        buffer.put(getOrigin().unwrap());
        buffer.putLong(getEnergyLimit());
        buffer.putLong(getEnergyPrice());
        buffer.putLong(getValue().longValue());
        buffer.put(getData());

        buffer.putLong(getBlockTimestamp());
        buffer.putLong(getBlockNumber());
        buffer.putLong(getBlockEnergyLimit());
        buffer.put(getBlockCoinbase().unwrap());
        buffer.put(getBlockPreviousHash());
        buffer.put(getBlockDifficulty().toByteArray());

        buffer.put("value".getBytes());
        buffer.putLong(getBalance(new Address(new byte[32])).longValue());
        buffer.putLong(getCodeSize(getAddress()));

        getRemainingEnergy();
        call(new Address(new byte[32]), BigInteger.ZERO, new byte[0], 0);
        create(BigInteger.ZERO, new byte[0], 0);
        // selfDestruct(new Address(new byte[32]));

        log("data".getBytes());
        log("topic1".getBytes(), "data".getBytes());
        log("topic1".getBytes(), "topic2".getBytes(), "data".getBytes());
        log("topic1".getBytes(), "topic2".getBytes(), "topic3".getBytes(), "data".getBytes());
        log("topic1".getBytes(), "topic2".getBytes(), "topic3".getBytes(), "topic4".getBytes(), "data".getBytes());

        buffer.put(blake2b("message".getBytes()));

        println("message");

        print("message");

        return Arrays.copyOfRange(buffer.array(), 0, buffer.size());
    }
}
