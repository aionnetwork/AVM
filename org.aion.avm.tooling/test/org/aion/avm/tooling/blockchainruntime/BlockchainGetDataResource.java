package org.aion.avm.tooling.blockchainruntime;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionBuffer;


public class BlockchainGetDataResource {

    static {
        AionBuffer buffer = AionBuffer.wrap(Blockchain.getAddress().toByteArray());
        buffer.put("modified!".getBytes());
    }

    @Callable
    public static byte[] getDataAndModify(boolean isModify) {
        assert Blockchain.getData() == Blockchain.getData();

        AionBuffer buffer = AionBuffer.wrap(Blockchain.getData());
        if (isModify) {
            buffer.put("modified!".getBytes());
        }

        return Blockchain.getData();
    }

    @Callable
    public static boolean getDataAndCompare() {
        return Blockchain.getData() == Blockchain.getData();
    }

    @Callable
    public static byte[] getAddressAndModify(boolean isModify) {
        AionBuffer buffer = AionBuffer.wrap(Blockchain.getAddress().toByteArray());
        if (isModify) {
            buffer.put("modified!".getBytes());
        }
        return Blockchain.getAddress().toByteArray();
    }

    @Callable
    public static byte[] getAddress() {
        return Blockchain.getAddress().toByteArray();
    }


    @Callable
    public static boolean getAddressAndCompare() {
        return Blockchain.getAddress() == Blockchain.getAddress();
    }

    @Callable
    public static byte[] getCallerAndModify(boolean isModify) {
        AionBuffer buffer = AionBuffer.wrap(Blockchain.getCaller().toByteArray());
        if (isModify) {
            buffer.put("modified!".getBytes());
        }
        return Blockchain.getCaller().toByteArray();
    }

    @Callable
    public static boolean getCallerAndCompare() {
        return Blockchain.getCaller() == Blockchain.getCaller();
    }

    @Callable
    public static byte[] getOriginAndModify(boolean isModify) {
        AionBuffer buffer = AionBuffer.wrap(Blockchain.getOrigin().toByteArray());
        if (isModify) {
            buffer.put("modified!".getBytes());
        }
        return Blockchain.getOrigin().toByteArray();
    }

    @Callable
    public static boolean getOriginAndCompare() {
        return Blockchain.getOrigin() == Blockchain.getOrigin();
    }

    @Callable
    public static byte[] getBlockCoinbaseAndModify(boolean isModify) {
        AionBuffer buffer = AionBuffer.wrap(Blockchain.getBlockCoinbase().toByteArray());
        if (isModify) {
            buffer.put("modified!".getBytes());
        }
        return Blockchain.getBlockCoinbase().toByteArray();
    }

    @Callable
    public static boolean getBlockCoinbaseAndCompare() {
        return Blockchain.getBlockCoinbase() == Blockchain.getBlockCoinbase();
    }

    @Callable
    public static byte[] getBlockDifficulty() {
        return Blockchain.getBlockDifficulty().toByteArray();
    }

    @Callable
    public static boolean getBlockDifficultyAndCompare() {
        return Blockchain.getBlockDifficulty() == Blockchain.getBlockDifficulty();
    }
}
