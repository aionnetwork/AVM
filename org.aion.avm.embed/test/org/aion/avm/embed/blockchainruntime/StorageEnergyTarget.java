package org.aion.avm.embed.blockchainruntime;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

import java.math.BigInteger;

public class StorageEnergyTarget {

    @Callable
    public static void putStorage() {
        byte[] key = new byte[32];
        key[0] = 0x1;
        Blockchain.putStorage(key, new byte[0]);
        key[0] = 0x2;
        Blockchain.putStorage(key, new byte[0]);
        key[0] = 0x3;
        Blockchain.putStorage(key, new byte[0]);
        key[0] = 0x4;
        Blockchain.putStorage(key, new byte[0]);
        key[0] = 0x5;
        Blockchain.putStorage(key, new byte[0]);
    }

    @Callable
    public static void resetStorage() {
        byte[] key = new byte[32];
        key[0] = 0x1;
        Blockchain.putStorage(key, null);
        key[0] = 0x2;
        Blockchain.putStorage(key, null);
        key[0] = 0x3;
        Blockchain.putStorage(key, null);
        key[0] = 0x4;
        Blockchain.putStorage(key, null);
        key[0] = 0x5;
        Blockchain.putStorage(key, null);
    }

    @Callable
    public static void resetStorageSelfDestruct() {
        byte[] key = new byte[32];
        key[0] = 0x1;
        Blockchain.putStorage(key, null);
        key[0] = 0x2;
        Blockchain.putStorage(key, null);
        key[0] = 0x3;
        Blockchain.putStorage(key, null);
        key[0] = 0x4;
        Blockchain.putStorage(key, null);
        key[0] = 0x5;
        Blockchain.putStorage(key, null);

        Blockchain.selfDestruct(Blockchain.getCaller());
    }

    @Callable
    public static void putStorageSameKey() {
        byte[] key = new byte[32];
        key[0] = 0x1;
        for (int i = 0; i < 5; i++) {
            Blockchain.putStorage(key, new byte[0]);
        }
    }

    @Callable
    public static void resetStorageSameKey() {
        for (int i = 0; i < 5; i++) {
            byte[] key = new byte[32];
            key[0] = 0x1;
            Blockchain.putStorage(key, null);
        }
    }

    @Callable
    public static byte[] getStorage(byte[] key) {
        return Blockchain.getStorage(key);
    }

    @Callable
    public static void reentrantCall(Address callee, byte[] data) {
        Blockchain.call(callee, BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
    }

    @Callable
    public static void reentrantCallAfterPut(Address callee, byte[] data) {
        putStorage();
        Blockchain.call(callee, BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
    }

    @Callable
    public static void checkStorage() {
        byte[] key = new byte[32];
        key[0] = 0x1;
        Blockchain.putStorage(key, new byte[0]);
        Blockchain.require(Blockchain.getStorage(key).length == 0);
        Blockchain.putStorage(key, null);
        Blockchain.require(Blockchain.getStorage(key) == null);
        Blockchain.putStorage(key, new byte[0]);
        Blockchain.require(Blockchain.getStorage(key).length == 0);

    }

    @Callable
    public static void reentrantReset(Address callee, byte[] data) {
        Blockchain.call(callee, BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        resetStorage();
    }

}
