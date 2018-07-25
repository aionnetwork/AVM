package org.aion.avm.core.testWallet;

import java.util.function.Consumer;

import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void init(Consumer<byte[]> inputConsumer, Address extra1, Address extra2, int requiredVotes, long dailyLimit) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        inputConsumer.accept(onto);
        WalletShim.main();
    }

    public static void payable(Consumer<byte[]> inputConsumer, Address from, long value) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.payable(from, value);
        inputConsumer.accept(onto);
        WalletShim.main();
    }

    public static boolean addOwner(Consumer<byte[]> inputConsumer, Address owner) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.addOwner(owner);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return (null != result);
    }

    public static byte[] execute(Consumer<byte[]> inputConsumer, Address to, long value, byte[] data) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.execute(to, value, data);
        inputConsumer.accept(onto);
        return WalletShim.main();
    }

    public static boolean confirm(Consumer<byte[]> inputConsumer, byte[] data) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.confirm(data);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return (0x1 == result[0]);
    }

    public static boolean changeRequirement(Consumer<byte[]> inputConsumer, int newRequired) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.changeRequirement(newRequired);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return (null != result);
    }

    public static Address getOwner(Consumer<byte[]> inputConsumer, int ownerIndex) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.getOwner(ownerIndex);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return new Address(result);
    }

    public static boolean changeOwner(Consumer<byte[]> inputConsumer, Address from, Address to) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.changeOwner(from, to);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return (null != result);
    }

    public static boolean removeOwner(Consumer<byte[]> inputConsumer, Address owner) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.removeOwner(owner);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return (null != result);
    }

    public static void revoke(Consumer<byte[]> inputConsumer, byte[] transactionBytes) throws InvalidTxDataException {
        byte[] onto = DirectEncoder.revoke(transactionBytes);
        inputConsumer.accept(onto);
        WalletShim.main();
    }
}
