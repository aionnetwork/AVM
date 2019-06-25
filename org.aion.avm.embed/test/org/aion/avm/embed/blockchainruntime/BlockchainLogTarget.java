package org.aion.avm.embed.blockchainruntime;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class BlockchainLogTarget {
    @Callable
    public static long testLog0Topic(int dataSize) {
        byte[] bytes = new byte[dataSize];

        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.log(bytes);
        long consumedEnergy = remainingEnergy - Blockchain.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog1Topic(int topicSize, int dataSize) {
        byte[] data = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.log(topic, data);
        long consumedEnergy = remainingEnergy - Blockchain.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog2Topics(int topicSize, int dataSize) {
        byte[] data = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.log(topic, topic, data);
        long consumedEnergy = remainingEnergy - Blockchain.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog3Topics(int topicSize, int dataSize) {
        byte[] bytes = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.log(topic, topic, topic, bytes);
        long consumedEnergy = remainingEnergy - Blockchain.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static long testLog4Topics(int topicSize, int dataSize) {
        byte[] bytes = new byte[dataSize];
        byte[] topic = new byte[topicSize];

        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.log(topic, topic, topic, topic, bytes);
        long consumedEnergy = remainingEnergy - Blockchain.getRemainingEnergy();

        return consumedEnergy;
    }

    @Callable
    public static boolean testLog4TopicsNull() {
        try {
            Blockchain.log(null, null, null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e){
            // Expected
        }

        return true;
    }

}
