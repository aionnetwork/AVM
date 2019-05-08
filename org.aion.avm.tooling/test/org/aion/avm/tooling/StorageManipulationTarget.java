package org.aion.avm.tooling;

import avm.Blockchain;
import java.math.BigInteger;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIEncoder;

public class StorageManipulationTarget {

    @Callable
    public static void manipulatePutStorageKey() {
        byte[] key = new byte[32];
        key[0] = 0x1;

        byte[] keyCopy = new byte[32];
        System.arraycopy(key, 0, keyCopy, 0, 32);

        byte[] value = new byte[32];
        value[0] = 0x2;

        // Add the key-value pair.
        Blockchain.putStorage(key, value);

        // Now modify the second key so that it is the same as the first key.
        key[0] = 0x0;

        // Verify the key-value pair in storage should not have been modified.
        byte[] storageValue = Blockchain.getStorage(keyCopy);
        Blockchain.require(storageValue != null);
        Blockchain.require(0x2 == storageValue[0]);
    }

    @Callable
    public static void manipulatePutStorageValue() {
        byte[] key = new byte[32];

        byte[] value = new byte[32];
        value[0] = 0x1;

        // Add the key-value pairs.
        Blockchain.putStorage(key, value);

        // Now modify the second value so that it is the same as the first value.
        value[0] = 0x0;

        // Verify the key-value pair in storage should not have been modified.
        byte[] storageValue = Blockchain.getStorage(key);
        Blockchain.require(storageValue != null);
        Blockchain.require(0x1 == storageValue[0]);
    }

    @Callable
    public static void manipulateGetStorageValue() {
        byte[] key = new byte[32];
        byte[] value = new byte[32];
        value[0] = 0x1;

        byte[] valueCopy = new byte[32];
        System.arraycopy(value, 0, valueCopy, 0, 32);

        Blockchain.putStorage(key, value);

        // Get the bytes from storage and modify them.
        byte[] fetchedValue = Blockchain.getStorage(key);
        fetchedValue[0] = 0x0;

        // Verify that the storage value did not actually change.
        Blockchain.require(Blockchain.getStorage(key)[0] == valueCopy[0]);
    }

    @Callable
    public static void manipulateStorageInReentrantCall() {
        byte[] key = new byte[32];
        byte[] value = new byte[32];
        key[0] = 0x1;
        value[0] = 0x2;

        Blockchain.putStorage(key, value);

        // Call into self and manipulate your own storage.
        byte[] data = ABIEncoder.encodeOneString("reentrantManipulate");
        Blockchain.require(Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, Blockchain.getRemainingEnergy()).isSuccess());

        // Verify that our key-value pair is left intact.
        byte[] fetchedValue = Blockchain.getStorage(key);
        Blockchain.require(fetchedValue != null);
        Blockchain.require(fetchedValue[0] == 0x2);
    }

    @Callable
    public static void reentrantManipulate() {
        byte[] key = new byte[32];
        key[0] = 0x1;

        byte[] value = Blockchain.getStorage(key);
        value[0] = 0x3;
    }
}
