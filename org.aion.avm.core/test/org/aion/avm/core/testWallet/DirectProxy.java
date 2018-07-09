package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void init(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        byte[] onto = CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        Wallet.decode(onto);
    }

    public static void payable(Address from, long value) {
        byte[] onto = CallEncoder.payable(from, value);
        Wallet.decode(onto);
    }

    public static void addOwner(Address owner) {
        byte[] onto = CallEncoder.addOwner(owner);
        Wallet.decode(onto);
    }

    public static byte[] execute(Address to, long value, byte[] data) {
        byte[] onto = CallEncoder.execute(to, value, data);
        return Wallet.decode(onto);
    }

    public static boolean confirm(byte[] data) {
        byte[] onto = CallEncoder.confirm(data);
        byte[] result = Wallet.decode(onto);
        return (0x1 == result[0]);
    }

    public static void changeRequirement(int newRequired) {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        Wallet.decode(onto);
    }

    public static Address getOwner(int ownerIndex) {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        byte[] result = Wallet.decode(onto);
        return new Address(result);
    }

    public static void changeOwner(Address from, Address to) {
        byte[] onto = CallEncoder.changeOwner(from, to);
        Wallet.decode(onto);
    }

    public static void removeOwner(Address owner) {
        byte[] onto = CallEncoder.removeOwner(owner);
        Wallet.decode(onto);
    }

    public static void revoke(byte[] transactionBytes) {
        byte[] onto = CallEncoder.revoke(transactionBytes);
        Wallet.decode(onto);
    }
}
