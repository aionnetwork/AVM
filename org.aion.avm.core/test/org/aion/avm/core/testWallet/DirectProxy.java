package org.aion.avm.core.testWallet;

import java.util.function.Consumer;

import avm.Address;
import org.aion.avm.userlib.abi.ABIDecoder;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void init(Consumer<byte[]> inputConsumer, Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        byte[] onto = CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        inputConsumer.accept(onto);
        callVoid();
    }

    public static void payable(Consumer<byte[]> inputConsumer, Address from, long value) {
        byte[] onto = CallEncoder.payable(from, value);
        inputConsumer.accept(onto);
        callVoid();
    }

    public static boolean addOwner(Consumer<byte[]> inputConsumer, Address owner) {
        byte[] onto = CallEncoder.addOwner(owner);
        inputConsumer.accept(onto);
        return callBoolean();
    }

    public static byte[] execute(Consumer<byte[]> inputConsumer, Address to, long value, byte[] data) {
        byte[] onto = CallEncoder.execute(to, value, data);
        inputConsumer.accept(onto);
        return callByteArray();
    }

    public static boolean confirm(Consumer<byte[]> inputConsumer, byte[] data) {
        byte[] onto = CallEncoder.confirm(data);
        inputConsumer.accept(onto);
        return callBoolean();
    }

    public static boolean changeRequirement(Consumer<byte[]> inputConsumer, int newRequired) {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        inputConsumer.accept(onto);
        return callBoolean();
    }

    public static Address getOwner(Consumer<byte[]> inputConsumer, int ownerIndex) {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        inputConsumer.accept(onto);
        return callAddress();
    }

    public static boolean changeOwner(Consumer<byte[]> inputConsumer, Address from, Address to) {
        byte[] onto = CallEncoder.changeOwner(from, to);
        inputConsumer.accept(onto);
        return callBoolean();
    }

    public static boolean removeOwner(Consumer<byte[]> inputConsumer, Address owner) {
        byte[] onto = CallEncoder.removeOwner(owner);
        inputConsumer.accept(onto);
        return callBoolean();
    }

    public static void revoke(Consumer<byte[]> inputConsumer, byte[] transactionBytes) {
        byte[] onto = CallEncoder.revoke(transactionBytes);
        inputConsumer.accept(onto);
        callVoid();
    }


    private static void callVoid() {
        byte[] result = WalletShim.main();
        if (0 != result.length) {
            throw new AssertionError("Empty byte[] expected for void return");
        }
    }

    private static boolean callBoolean() {
        byte[] result = WalletShim.main();
        return new ABIDecoder(result).decodeOneBoolean();
    }

    private static byte[] callByteArray() {
        byte[] result = WalletShim.main();
        return new ABIDecoder(result).decodeOneByteArray();
    }

    private static Address callAddress() {
        byte[] result = WalletShim.main();
        return new ABIDecoder(result).decodeOneAddress();
    }
}
