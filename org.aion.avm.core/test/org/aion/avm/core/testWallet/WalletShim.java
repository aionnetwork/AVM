package org.aion.avm.core.testWallet;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;


/**
 * This just exposes access to the Wallet for calls from the DirectProxy.  The reason for this is that the ABI expects to call with the
 * wrapped, contract-space types, while the DirectProxy is calling the pre-transformed Wallet code, meaning it expects real types.
 * This shim does the wrapping/unwrapping for the calls into the core Wallet code.
 */
public class WalletShim {
    public static byte[] main() {
        byte[] input = BlockchainRuntime.getData();
        return ABIDecoder.decodeAndRunWithClass(WalletShim.class, input);
    }

    public static void initWrapper(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        Wallet.initWrapper(extra1, extra2, requiredVotes, dailyLimit);
    }

    public static void avm_initWrapper(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        Wallet.initWrapper(extra1, extra2, requiredVotes, dailyLimit);
    }

    public static void payable(Address from, long value) {
        Wallet.payable(from, value);
    }

    public static void avm_payable(Address from, long value) {
        Wallet.payable(from, value);
    }

    public static boolean addOwner(Address owner) {
        return Wallet.addOwner(owner);
    }

    public static boolean avm_addOwner(Address owner) {
        return Wallet.addOwner(owner);
    }

    public static byte[] execute(Address to, long value, byte[] data) {
        return Wallet.execute(to, value, data);
    }

    public static byte[] avm_execute(Address to, long value, ByteArray data) {
        return Wallet.execute(to, value, data.getUnderlying());
    }

    public static boolean confirm(byte[] h) {
        return Wallet.confirm(h);
    }

    public static boolean avm_confirm(ByteArray h) {
        return Wallet.confirm(h.getUnderlying());
    }

    public static boolean changeRequirement(int newRequired) {
        return Wallet.changeRequirement(newRequired);
    }

    public static boolean avm_changeRequirement(int newRequired) {
        return Wallet.changeRequirement(newRequired);
    }

    public static Address getOwner(int ownerIndex) {
        return Wallet.getOwner(ownerIndex);
    }

    public static Address avm_getOwner(int ownerIndex) {
        return Wallet.getOwner(ownerIndex);
    }

    public static boolean changeOwner(Address from, Address to) {
        return Wallet.changeOwner(from, to);
    }

    public static boolean avm_changeOwner(Address from, Address to) {
        return Wallet.changeOwner(from, to);
    }

    public static boolean removeOwner(Address owner) {
        return Wallet.removeOwner(owner);
    }

    public static boolean avm_removeOwner(Address owner) {
        return Wallet.removeOwner(owner);
    }

    public static void revoke(byte[] transactionBytes) {
        Wallet.revoke(transactionBytes);
    }

    public static void avm_revoke(ByteArray transactionBytes) {
        Wallet.revoke(transactionBytes.getUnderlying());
    }
}
