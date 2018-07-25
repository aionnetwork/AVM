package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.arraywrapper.ByteArray;


/**
 * This just exposes access to the Wallet for calls from the DirectProxy.  The reason for this is that the ABI expects to call with the
 * wrapped, contract-space types, while the DirectProxy is calling the pre-transformed Wallet code, meaning it expects real types.
 * This shim does the wrapping/unwrapping for the calls into the core Wallet code.
 */
public class WalletShim {
    public static byte[] main() throws InvalidTxDataException {
        // Most of our paths return nothing so just default to the empty byte array.
        byte[] result = new byte[0];
        byte[] input = BlockchainRuntime.getData();
        Abi.Decoder decoder = Abi.buildDecoder(input);
        byte methodByte = decoder.decodeByte();
        
        switch (methodByte) {
        case Abi.kWallet_init : {
            // We know that this is Address, Address, int, long.
            Address extra1 = decoder.decodeAddress();
            Address extra2 = decoder.decodeAddress();
            int votesRequiredPerOperation = decoder.decodeInt();
            long daylimit = decoder.decodeLong();
            WalletShim.avm_initWrapper(extra1, extra2, votesRequiredPerOperation, daylimit);
            break;
        }
        case Abi.kWallet_payable : {
            Address from = decoder.decodeAddress();
            long value = decoder.decodeLong();
            WalletShim.avm_payable(from, value);
            break;
        }
        case Abi.kWallet_addOwner : {
            Address owner = decoder.decodeAddress();
            boolean output = WalletShim.avm_addOwner(owner);
            result = output ? new byte[0] : null;
            break;
        }
        case Abi.kWallet_execute : {
            Address to = decoder.decodeAddress();
            long value = decoder.decodeLong();
            byte[] data = decoder.decodeRemainder();
            result = WalletShim.avm_execute(to, value, new ByteArray(data));
            break;
        }
        case Abi.kWallet_confirm : {
            byte[] data = decoder.decodeRemainder();
            boolean bool = WalletShim.avm_confirm(new ByteArray(data));
            result = new byte[] { (byte)(bool ? 0x1 : 0x0) };
            break;
        }
        case Abi.kWallet_changeRequirement : {
            int newRequired = decoder.decodeInt();
            boolean output = WalletShim.avm_changeRequirement(newRequired);
            result = output ? new byte[0] : null;
            break;
        }
        case Abi.kWallet_getOwner : {
            int ownerIndex = decoder.decodeInt();
            Address owner = WalletShim.avm_getOwner(ownerIndex);
            // We need to encode this so allocate a buffer and write it with the encoder.
            byte[] onto = new byte[Address.LENGTH];
            Abi.buildEncoder(onto).encodeAddress(owner);
            result = onto;
            break;
        }
        case Abi.kWallet_changeOwner : {
            Address from = decoder.decodeAddress();
            Address to = decoder.decodeAddress();
            boolean output = WalletShim.avm_changeOwner(from, to);
            result = output ? new byte[0] : null;
            break;
        }
        case Abi.kWallet_removeOwner : {
            Address owner = decoder.decodeAddress();
            boolean output = WalletShim.avm_removeOwner(owner);
            result = output ? new byte[0] : null;
            break;
        }
        case Abi.kWallet_revoke : {
            byte[] transactionBytes = decoder.decodeRemainder();
            WalletShim.avm_revoke(new ByteArray(transactionBytes));
            break;
        }
        default:
            throw new AssertionError("No method for byte: " + methodByte);
        }
        return result;
    }

    public static void avm_initWrapper(Address extra1, Address extra2, int requiredVotes, long dailyLimit) {
        Wallet.initWrapper(extra1, extra2, requiredVotes, dailyLimit);
    }

    public static void avm_payable(Address from, long value) {
        Wallet.payable(from, value);
    }

    public static boolean avm_addOwner(Address owner) {
        return Wallet.addOwner(owner);
    }

    public static byte[] avm_execute(Address to, long value, ByteArray data) {
        return Wallet.execute(to, value, data.getUnderlying());
    }

    public static boolean avm_confirm(ByteArray h) {
        return Wallet.confirm(h.getUnderlying());
    }

    public static boolean avm_changeRequirement(int newRequired) {
        return Wallet.changeRequirement(newRequired);
    }

    public static Address avm_getOwner(int ownerIndex) {
        return Wallet.getOwner(ownerIndex);
    }

    public static boolean avm_changeOwner(Address from, Address to) {
        return Wallet.changeOwner(from, to);
    }

    public static boolean avm_removeOwner(Address owner) {
        return Wallet.removeOwner(owner);
    }

    public static void avm_revoke(ByteArray transactionBytes) {
        Wallet.revoke(transactionBytes.getUnderlying());
    }
}
