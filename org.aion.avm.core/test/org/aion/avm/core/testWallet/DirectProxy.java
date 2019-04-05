package org.aion.avm.core.testWallet;

import java.util.function.Consumer;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void init(Consumer<byte[]> inputConsumer, Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        byte[] onto = CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        inputConsumer.accept(onto);
        WalletShim.main();
    }

    public static void payable(Consumer<byte[]> inputConsumer, Address from, long value) {
        byte[] onto = CallEncoder.payable(from, value);
        inputConsumer.accept(onto);
        WalletShim.main();
    }

    public static boolean addOwner(Consumer<byte[]> inputConsumer, Address owner) {
        byte[] onto = CallEncoder.addOwner(owner);
        inputConsumer.accept(onto);
        return (Boolean) ABIUtil.decodeOneObject(WalletShim.main());
    }

    public static byte[] execute(Consumer<byte[]> inputConsumer, Address to, long value, byte[] data) {
        byte[] onto = CallEncoder.execute(to, value, data);
        inputConsumer.accept(onto);
        byte[] result = WalletShim.main();
        return (null != result)
                ? (byte[])ABIUtil.decodeOneObject(result)
                : null;
    }

    public static boolean confirm(Consumer<byte[]> inputConsumer, byte[] data) {
        byte[] onto = CallEncoder.confirm(data);
        inputConsumer.accept(onto);
        return (Boolean) ABIUtil.decodeOneObject(WalletShim.main());
    }

    public static boolean changeRequirement(Consumer<byte[]> inputConsumer, int newRequired) {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        inputConsumer.accept(onto);
        return (Boolean) ABIUtil.decodeOneObject(WalletShim.main());
    }

    public static Address getOwner(Consumer<byte[]> inputConsumer, int ownerIndex) {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        inputConsumer.accept(onto);
        return (Address) ABIUtil.decodeOneObject(WalletShim.main());
    }

    public static boolean changeOwner(Consumer<byte[]> inputConsumer, Address from, Address to) {
        byte[] onto = CallEncoder.changeOwner(from, to);
        inputConsumer.accept(onto);
        return (Boolean) ABIUtil.decodeOneObject(WalletShim.main());
    }

    public static boolean removeOwner(Consumer<byte[]> inputConsumer, Address owner) {
        byte[] onto = CallEncoder.removeOwner(owner);
        inputConsumer.accept(onto);
        return (Boolean) ABIUtil.decodeOneObject(WalletShim.main());
    }

    public static void revoke(Consumer<byte[]> inputConsumer, byte[] transactionBytes) {
        byte[] onto = CallEncoder.revoke(transactionBytes);
        inputConsumer.accept(onto);
        WalletShim.main();
    }
}
