package org.aion.avm.core.testWallet;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.internal.InvalidTxDataException;


/**
 * Mostly a historical remnant from before we had the common ABIEncoder.
 * Will be removed soon.
 */
public class CallEncoder {
    public static byte[] init(Address extra1, Address extra2, int requiredVotes, long dailyLimit) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("initWrapper", extra1, extra2, requiredVotes, dailyLimit);
    }
    public static byte[] payable(Address from, long value) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("payable", from, value);
    }
    public static byte[] addOwner(Address owner) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("addOwner", owner);
    }
    public static byte[] execute(Address to, long value, byte[] data) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("execute", to, value, data);
    }
    public static byte[] confirm(byte[] data) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("confirm", data);
    }
    public static byte[] changeRequirement(int newRequired) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("changeRequirement", newRequired);
    }
    public static byte[] getOwner(int ownerIndex) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("getOwner", ownerIndex);
    }
    public static byte[] changeOwner(Address from, Address to) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("changeOwner", from, to);
    }
    public static byte[] removeOwner(Address owner) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("removeOwner", owner);
    }
    public static byte[] revoke(byte[] transactionBytes) throws InvalidTxDataException {
        return ABIEncoder.encodeMethodArguments("revoke", transactionBytes);
    }
}
