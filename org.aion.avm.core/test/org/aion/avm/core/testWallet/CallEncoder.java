package org.aion.avm.core.testWallet;

import avm.Address;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;


/**
 * Mostly a historical remnant from before we had the common ABIEncoder.
 * Will be removed soon.
 */
public class CallEncoder {
    public static byte[] init(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        return new ABIStreamingEncoder()
                .encodeOneString("initWrapper")
                .encodeOneAddress(extra1)
                .encodeOneAddress(extra2)
                .encodeOneInteger(requiredVotes)
                .encodeOneLong(dailyLimit)
                .toBytes();
    }
    public static byte[] payable(Address from, long value) {
        return new ABIStreamingEncoder()
                .encodeOneString("payable")
                .encodeOneAddress(from)
                .encodeOneLong(value)
                .toBytes();
    }
    public static byte[] addOwner(Address owner) {
        return new ABIStreamingEncoder()
                .encodeOneString("addOwner")
                .encodeOneAddress(owner)
                .toBytes();
    }
    public static byte[] execute(Address to, long value, byte[] data) {
        return new ABIStreamingEncoder()
                .encodeOneString("execute")
                .encodeOneAddress(to)
                .encodeOneLong(value)
                .encodeOneByteArray(data)
                .toBytes();
    }
    public static byte[] confirm(byte[] data) {
        return new ABIStreamingEncoder()
                .encodeOneString("confirm")
                .encodeOneByteArray(data)
                .toBytes();
    }
    public static byte[] changeRequirement(int newRequired) {
        return new ABIStreamingEncoder()
                .encodeOneString("changeRequirement")
                .encodeOneInteger(newRequired)
                .toBytes();
    }
    public static byte[] getOwner(int ownerIndex) {
        return new ABIStreamingEncoder()
                .encodeOneString("getOwner")
                .encodeOneInteger(ownerIndex)
                .toBytes();
    }
    public static byte[] changeOwner(Address from, Address to) {
        return new ABIStreamingEncoder()
                .encodeOneString("changeOwner")
                .encodeOneAddress(from)
                .encodeOneAddress(to)
                .toBytes();
    }
    public static byte[] removeOwner(Address owner) {
        return new ABIStreamingEncoder()
                .encodeOneString("removeOwner")
                .encodeOneAddress(owner)
                .toBytes();
    }
    public static byte[] revoke(byte[] transactionBytes) {
        return new ABIStreamingEncoder()
                .encodeOneString("revoke")
                .encodeOneByteArray(transactionBytes)
                .toBytes();
    }
}
