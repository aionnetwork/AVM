package org.aion.avm.core.testWallet;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;


/**
 * Mostly a historical remnant from before we had the common ABIEncoder.
 * Will be removed soon.
 */
public class CallEncoder {
    public static byte[] init(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        return ABIUtil.encodeMethodArguments("initWrapper", extra1, extra2, requiredVotes, dailyLimit);
    }
    public static byte[] payable(Address from, long value) {
        return ABIUtil.encodeMethodArguments("payable", from, value);
    }
    public static byte[] addOwner(Address owner) {
        return ABIUtil.encodeMethodArguments("addOwner", owner);
    }
    public static byte[] execute(Address to, long value, byte[] data) {
        return ABIUtil.encodeMethodArguments("execute", to, value, data);
    }
    public static byte[] confirm(byte[] data) {
        return ABIUtil.encodeMethodArguments("confirm", data);
    }
    public static byte[] changeRequirement(int newRequired) {
        return ABIUtil.encodeMethodArguments("changeRequirement", newRequired);
    }
    public static byte[] getOwner(int ownerIndex) {
        return ABIUtil.encodeMethodArguments("getOwner", ownerIndex);
    }
    public static byte[] changeOwner(Address from, Address to) {
        return ABIUtil.encodeMethodArguments("changeOwner", from, to);
    }
    public static byte[] removeOwner(Address owner) {
        return ABIUtil.encodeMethodArguments("removeOwner", owner);
    }
    public static byte[] revoke(byte[] transactionBytes) {
        return ABIUtil.encodeMethodArguments("revoke", transactionBytes);
    }
}
