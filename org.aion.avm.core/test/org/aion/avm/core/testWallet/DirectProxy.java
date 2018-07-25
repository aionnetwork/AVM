package org.aion.avm.core.testWallet;

import java.util.function.Consumer;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.arraywrapper.ByteArray;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void init(Consumer<byte[]> inputConsumer, Address extra1, Address extra2, int requiredVotes, long dailyLimit) throws InvalidTxDataException {
        byte[] onto = CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        inputConsumer.accept(onto);
        WalletShim.main();
    }

    public static void payable(Consumer<byte[]> inputConsumer, Address from, long value) throws InvalidTxDataException {
        byte[] onto = CallEncoder.payable(from, value);
        inputConsumer.accept(onto);
        WalletShim.main();
    }

    public static boolean addOwner(Consumer<byte[]> inputConsumer, Address owner) throws InvalidTxDataException {
        byte[] onto = CallEncoder.addOwner(owner);
        inputConsumer.accept(onto);
        return (Boolean) ABIDecoder.decodeOneObject(WalletShim.main());
    }

    public static byte[] execute(Consumer<byte[]> inputConsumer, Address to, long value, byte[] data) throws InvalidTxDataException {
        byte[] onto = CallEncoder.execute(to, value, data);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return (null != result)
                ? ((ByteArray) ABIDecoder.decodeOneObject(result)).getUnderlying()
                : null;
    }

    public static boolean confirm(Consumer<byte[]> inputConsumer, byte[] data) throws InvalidTxDataException {
        byte[] onto = CallEncoder.confirm(data);
        inputConsumer.accept(onto);
        return (Boolean) ABIDecoder.decodeOneObject(WalletShim.main());
    }

    public static boolean changeRequirement(Consumer<byte[]> inputConsumer, int newRequired) throws InvalidTxDataException {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        inputConsumer.accept(onto);
        return (Boolean) ABIDecoder.decodeOneObject(WalletShim.main());
    }

    public static Address getOwner(Consumer<byte[]> inputConsumer, int ownerIndex) throws InvalidTxDataException {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        inputConsumer.accept(onto);
        return (Address) ABIDecoder.decodeOneObject(WalletShim.main());
    }

    public static boolean changeOwner(Consumer<byte[]> inputConsumer, Address from, Address to) throws InvalidTxDataException {
        byte[] onto = CallEncoder.changeOwner(from, to);
        inputConsumer.accept(onto);
        return (Boolean) ABIDecoder.decodeOneObject(WalletShim.main());
    }

    public static boolean removeOwner(Consumer<byte[]> inputConsumer, Address owner) throws InvalidTxDataException {
        byte[] onto = CallEncoder.removeOwner(owner);
        inputConsumer.accept(onto);
        return (Boolean) ABIDecoder.decodeOneObject(WalletShim.main());
    }

    public static void revoke(Consumer<byte[]> inputConsumer, byte[] transactionBytes) throws InvalidTxDataException {
        byte[] onto = CallEncoder.revoke(transactionBytes);
        inputConsumer.accept(onto);
        WalletShim.main();
    }
}
