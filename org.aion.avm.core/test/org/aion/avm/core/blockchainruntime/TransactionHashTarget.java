package org.aion.avm.core.blockchainruntime;

import avm.Address;
import avm.Blockchain;
import avm.Result;
import java.math.BigInteger;
import java.util.Arrays;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class TransactionHashTarget {
    private static final byte[] TRANSACTION_HASH = Blockchain.getTransactionHash();

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String method = decoder.decodeMethodName();

        if (method.equals("getTransactionHash")) {
            return getTransactionHash();
        } else if (method.equals("getTransactionHashInInternalCall")) {
            return getTransactionHashInInternalCall();
        } else if (method.equals("modifyHashInExternalCall")) {
            modifyHashInExternalCall();
        } else if (method.equals("modifyHashInInternalCall")) {
            modifyHashInInternalCall();
        } else if (method.equals("modifyHash")) {
            modifyHash();
        } else if (method.equals("getTransactionHashFromClinit")) {
            return TRANSACTION_HASH;
        } else if (method.equals("getTransactionHashFromInternalCallClinit")) {
            return getTransactionHashFromInternalCallClinit(decoder.decodeOneByteArray());
        } else {
            // Hitting this is a mistake.
            Blockchain.require(false);
        }

        return null;
    }

    public static byte[] getTransactionHashFromInternalCallClinit(byte[] jar) {
        // Deploy the contract.
        Result result = Blockchain.create(BigInteger.ZERO, jar, Blockchain.getRemainingEnergy());
        Blockchain.require(result.isSuccess());
        Address contract = new Address(result.getReturnData());

        // Call the contract to get its hash stored during its clinit.
        byte[] data = ABIEncoder.encodeOneString("getTransactionHashFromClinit");
        result = Blockchain.call(contract, BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        Blockchain.require(result.isSuccess());
        return result.getReturnData();
    }

    public static void modifyHashInInternalCall() {
        // Save the actual hash in a copy.
        byte[] hash = Blockchain.getTransactionHash();
        byte[] originalHash = new byte[hash.length];
        System.arraycopy(hash, 0, originalHash, 0, hash.length);

        byte[] data = ABIEncoder.encodeOneString("modifyHash");
        Result result = Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        Blockchain.require(result.isSuccess());

        // Verify the returned hash equals the original.
        byte[] hash2 = Blockchain.getTransactionHash();
        Blockchain.require(Arrays.equals(originalHash, hash2));
    }

    public static void modifyHashInExternalCall() {
        // We expect the transaction hash to be lazily cached by AVM so we should get back the same instance on each call.
        // Consequently, any modifications we make to the hash will be treated like any other write to a byte[].
        // Save the actual hash in a copy.
        byte[] hash = Blockchain.getTransactionHash();
        byte[] expectedHash = new byte[hash.length];
        System.arraycopy(hash, 0, expectedHash, 0, hash.length);
        expectedHash[0] = (byte) ~expectedHash[0];

        modifyHash();

        // Verify that the instance is the same and the modification is reflected.
        byte[] hash2 = Blockchain.getTransactionHash();
        Blockchain.require(hash == hash2);
        Blockchain.require(Arrays.equals(expectedHash, hash2));
    }

    public static void modifyHash() {
        byte[] hash = Blockchain.getTransactionHash();
        hash[0] = (byte) ~hash[0];
    }

    public static byte[] getTransactionHashInInternalCall() {
        byte[] data = ABIEncoder.encodeOneString("getTransactionHash");
        Result result = Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        Blockchain.require(result.isSuccess());
        return result.getReturnData();
    }

    public static byte[] getTransactionHash() {
        return Blockchain.getTransactionHash();
    }
}
