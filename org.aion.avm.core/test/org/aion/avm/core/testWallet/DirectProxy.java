package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.IBlockchainRuntime;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void payable(IBlockchainRuntime runtime, Address from, long value) {
        byte[] onto = CallEncoder.payable(from, value);
        Wallet.decode(runtime, onto);
    }

    public static void addOwner(IBlockchainRuntime runtime, Address owner) {
        byte[] onto = CallEncoder.addOwner(owner);
        Wallet.decode(runtime, onto);
    }

    public static byte[] execute(IBlockchainRuntime runtime, Address to, long value, byte[] data) {
        byte[] onto = CallEncoder.execute(to, value, data);
        return Wallet.decode(runtime, onto);
    }

    public static boolean confirm(IBlockchainRuntime runtime, byte[] data) {
        byte[] onto = CallEncoder.confirm(data);
        byte[] result = Wallet.decode(runtime, onto);
        return (0x1 == result[0]);
    }

    public static void changeRequirement(IBlockchainRuntime runtime, int newRequired) {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        Wallet.decode(runtime, onto);
    }

    public static Address getOwner(IBlockchainRuntime runtime, int ownerIndex) {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        byte[] result = Wallet.decode(runtime, onto);
        return new Address(result);
    }

    public static void changeOwner(IBlockchainRuntime runtime, Address from, Address to) {
        byte[] onto = CallEncoder.changeOwner(from, to);
        Wallet.decode(runtime, onto);
    }

    public static void removeOwner(IBlockchainRuntime runtime, Address owner) {
        byte[] onto = CallEncoder.removeOwner(owner);
        Wallet.decode(runtime, onto);
    }

    public static void revoke(IBlockchainRuntime runtime) {
        byte[] onto = CallEncoder.revoke();
        Wallet.decode(runtime, onto);
    }
}
