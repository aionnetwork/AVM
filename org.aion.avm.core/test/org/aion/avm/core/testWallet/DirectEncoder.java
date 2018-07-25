package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;


/**
 * This is a temporary clone of CallEncoder so that we can convert the CallProxy and DirectProxy to the new ABI, independently.
 */
public class DirectEncoder {
    public static byte[] init(Address extra1, Address extra2, int requiredVotes, long dailyLimit) throws InvalidTxDataException {
        // Note that we are calling the initWrapper, so we pass the 2 Address instances, directly.
        byte[] onto = new byte[1 + Address.LENGTH + Address.LENGTH + Integer.BYTES + Long.BYTES];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_init)
            .encodeAddress(extra1)
            .encodeAddress(extra2)
            .encodeInt(requiredVotes)
            .encodeLong(dailyLimit);
        return onto;
    }
    public static byte[] payable(Address from, long value) throws InvalidTxDataException {
        byte[] onto = new byte[1 + Address.LENGTH + Long.BYTES];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_payable)
            .encodeAddress(from)
            .encodeLong(value);
        return onto;
    }
    public static byte[] addOwner(Address owner) throws InvalidTxDataException {
        byte[] onto = new byte[1 + Address.LENGTH];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_addOwner)
            .encodeAddress(owner);
        return onto;
    }
    public static byte[] execute(Address to, long value, byte[] data) throws InvalidTxDataException {
        byte[] onto = new byte[1 + Address.LENGTH + Long.BYTES + data.length];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_execute)
            .encodeAddress(to)
            .encodeLong(value)
            .encodeRemainder(data);
        return onto;
    }
    public static byte[] confirm(byte[] data) throws InvalidTxDataException {
        byte[] onto = new byte[1 + data.length];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_confirm)
            .encodeRemainder(data);
        return onto;
    }
    public static byte[] changeRequirement(int newRequired) throws InvalidTxDataException {
        byte[] onto = new byte[1 + Integer.BYTES];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_changeRequirement)
            .encodeInt(newRequired);
        return onto;
    }
    public static byte[] getOwner(int ownerIndex) throws InvalidTxDataException {
        byte[] onto = new byte[1 + Integer.BYTES];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_getOwner)
            .encodeInt(ownerIndex);
        return onto;
    }
    public static byte[] changeOwner(Address from, Address to) throws InvalidTxDataException {
        byte[] onto = new byte[1 + Address.LENGTH + Address.LENGTH];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_changeOwner)
            .encodeAddress(from)
            .encodeAddress(to);
        return onto;
    }
    public static byte[] removeOwner(Address owner) throws InvalidTxDataException {
        byte[] onto = new byte[1 + Address.LENGTH];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_removeOwner)
            .encodeAddress(owner);
        return onto;
    }
    public static byte[] revoke(byte[] transactionBytes) throws InvalidTxDataException {
        byte[] onto = new byte[1 + transactionBytes.length];
        Abi.Encoder encoder = Abi.buildEncoder(onto);
        encoder
            .encodeByte(Abi.kWallet_revoke);
        encoder.encodeRemainder(transactionBytes);
        return onto;
    }
}
