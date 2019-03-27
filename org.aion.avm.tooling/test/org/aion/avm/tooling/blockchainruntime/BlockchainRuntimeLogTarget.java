package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;

public class BlockchainRuntimeLogTarget {
    @Callable
    public static long testLog0Topic(int dataSize) {
        byte[] bytes = new byte[dataSize];

        long remainingEnergy = BlockchainRuntime.getRemainingEnergy();
        BlockchainRuntime.log(bytes);
        long consumedEnergy = remainingEnergy - BlockchainRuntime.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog1Topic(int topicSize, int dataSize) {
        byte[] data = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = BlockchainRuntime.getRemainingEnergy();
        BlockchainRuntime.log(topic, data);
        long consumedEnergy = remainingEnergy - BlockchainRuntime.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog2Topics(int topicSize, int dataSize) {
        byte[] data = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = BlockchainRuntime.getRemainingEnergy();
        BlockchainRuntime.log(topic, topic, data);
        long consumedEnergy = remainingEnergy - BlockchainRuntime.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog3Topics(int topicSize, int dataSize) {
        byte[] bytes = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = BlockchainRuntime.getRemainingEnergy();
        BlockchainRuntime.log(topic, topic, topic, bytes);
        long consumedEnergy = remainingEnergy - BlockchainRuntime.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog4Topics(int topicSize, int dataSize) {
        byte[] bytes = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = BlockchainRuntime.getRemainingEnergy();
        BlockchainRuntime.log(topic, topic, topic, topic, bytes);
        long consumedEnergy = remainingEnergy - BlockchainRuntime.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static boolean testLog4TopicsNull() {
        try {
            BlockchainRuntime.log(null, null, null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e){
            // Expected
        }

        return true;
    }

}
