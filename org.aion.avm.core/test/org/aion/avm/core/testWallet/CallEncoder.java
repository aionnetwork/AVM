package org.aion.avm.core.testWallet;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;


/**
 * Mostly a historical remnant from before we had the common ABIEncoder.
 * Will be removed soon.
 */
public class CallEncoder {
    public static byte[] init(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        return ABIEncoder.encodeMethodArguments("initWrapper", extra1, extra2, requiredVotes, dailyLimit);
    }
    public static byte[] payable(Address from, long value) {
        return ABIEncoder.encodeMethodArguments("payable", from, value);
    }
    public static byte[] addOwner(Address owner) {
        return ABIEncoder.encodeMethodArguments("addOwner", owner);
    }
    public static byte[] execute(Address to, long value, byte[] data) {
        return ABIEncoder.encodeMethodArguments("execute", to, value, data);
    }
    public static byte[] confirm(byte[] data) {
        return ABIEncoder.encodeMethodArguments("confirm", data);
    }
    public static byte[] changeRequirement(int newRequired) {
        return ABIEncoder.encodeMethodArguments("changeRequirement", newRequired);
    }
    public static byte[] getOwner(int ownerIndex) {
        return ABIEncoder.encodeMethodArguments("getOwner", ownerIndex);
    }
    public static byte[] changeOwner(Address from, Address to) {
        return ABIEncoder.encodeMethodArguments("changeOwner", from, to);
    }
    public static byte[] removeOwner(Address owner) {
        return ABIEncoder.encodeMethodArguments("removeOwner", owner);
    }
    public static byte[] revoke(byte[] transactionBytes) {
        return ABIEncoder.encodeMethodArguments("revoke", transactionBytes);
    }
}
