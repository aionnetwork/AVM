package org.aion.avm.embed;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class BlockchainRuntimeBillingTarget {

    @Callable
    public static void fillArray(){
        int i = 0;
        Address[] addresses = new Address[9000];
        while(i<9000) {
            addresses[i] = Blockchain.getCaller();
            i++;
        }
    }

    @Callable
    public static long blake2b(int size) {
        byte[] message = new byte[size];
        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.blake2b(message);
        return remainingEnergy - Blockchain.getRemainingEnergy();
    }

    @Callable
    public static long sha256(int size) {
        byte[] message = new byte[size];
        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.sha256(message);
        return remainingEnergy - Blockchain.getRemainingEnergy();
    }

    @Callable
    public static long keccak(int size) {
        byte[] message = new byte[size];
        long remainingEnergy = Blockchain.getRemainingEnergy();
        Blockchain.keccak256(message);
        return remainingEnergy - Blockchain.getRemainingEnergy();
    }

    @Callable
    public static void blake2bForInput(byte[] message) {
        Blockchain.blake2b(message);
    }

    @Callable
    public static void sha256ForInput(byte[] message) {
        Blockchain.sha256(message);
    }

    @Callable
    public static void keccakForInput(byte[] message) {
        Blockchain.keccak256(message);
    }

}
