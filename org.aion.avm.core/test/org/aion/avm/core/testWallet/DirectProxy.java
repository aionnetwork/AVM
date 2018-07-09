package org.aion.avm.core.testWallet;

import java.util.function.Consumer;

import org.aion.avm.api.Address;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void init(Consumer<byte[]> inputConsumer, Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        byte[] onto = CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        inputConsumer.accept(onto);
        Wallet.decode();
    }

    public static void payable(Consumer<byte[]> inputConsumer, Address from, long value) {
        byte[] onto = CallEncoder.payable(from, value);
        inputConsumer.accept(onto);
        Wallet.decode();
    }

    public static void addOwner(Consumer<byte[]> inputConsumer, Address owner) {
        byte[] onto = CallEncoder.addOwner(owner);
        inputConsumer.accept(onto);
        Wallet.decode();
    }

    public static byte[] execute(Consumer<byte[]> inputConsumer, Address to, long value, byte[] data) {
        byte[] onto = CallEncoder.execute(to, value, data);
        inputConsumer.accept(onto);
        return Wallet.decode();
    }

    public static boolean confirm(Consumer<byte[]> inputConsumer, byte[] data) {
        byte[] onto = CallEncoder.confirm(data);
        inputConsumer.accept(onto);
        byte[] result = Wallet.decode();
        return (0x1 == result[0]);
    }

    public static void changeRequirement(Consumer<byte[]> inputConsumer, int newRequired) {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        inputConsumer.accept(onto);
        Wallet.decode();
    }

    public static Address getOwner(Consumer<byte[]> inputConsumer, int ownerIndex) {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        inputConsumer.accept(onto);
        byte[] result = Wallet.decode();
        return new Address(result);
    }

    public static void changeOwner(Consumer<byte[]> inputConsumer, Address from, Address to) {
        byte[] onto = CallEncoder.changeOwner(from, to);
        inputConsumer.accept(onto);
        Wallet.decode();
    }

    public static void removeOwner(Consumer<byte[]> inputConsumer, Address owner) {
        byte[] onto = CallEncoder.removeOwner(owner);
        inputConsumer.accept(onto);
        Wallet.decode();
    }

    public static void revoke(Consumer<byte[]> inputConsumer, byte[] transactionBytes) {
        byte[] onto = CallEncoder.revoke(transactionBytes);
        inputConsumer.accept(onto);
        Wallet.decode();
    }
}
