package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;


/**
 * Based on the CallProxy, to give a similar interface for even the direct call comparison.
 */
public class DirectProxy {
    public static void payable(Address from, long value) {
        Wallet.payable(from, value);
    }

    public static void addOwner(BlockchainRuntime runtime, Address owner) {
        Wallet.addOwner(runtime, owner);
    }

    public static byte[] execute(BlockchainRuntime runtime, Address to, long value, byte[] data) {
        return Wallet.execute(runtime, to, value, data);
    }

    public static boolean confirm(BlockchainRuntime runtime, byte[] data) {
        return Wallet.confirm(runtime, data);
    }

    public static void changeRequirement(BlockchainRuntime runtime, int newRequired) {
        Wallet.changeRequirement(runtime, newRequired);
    }

    public static Address getOwner(BlockchainRuntime runtime, int ownerIndex) {
        return Wallet.getOwner(runtime, ownerIndex);
    }

    public static void changeOwner(BlockchainRuntime runtime, Address from, Address to) {
        Wallet.changeOwner(runtime, from, to);
    }

    public static void removeOwner(BlockchainRuntime runtime, Address owner) {
        Wallet.removeOwner(runtime, owner);
    }

    public static void revoke(BlockchainRuntime runtime) {
        Wallet.revoke(runtime);
    }
}
