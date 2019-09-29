package org.aion.avm.core.blockchainruntime;

import java.math.BigInteger;
import java.util.Arrays;

import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.TestingMetaEncoder;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.InternalTransaction;

import i.RuntimeAssertionError;


/**
 * Most of our tests don't need any capabilities so this is provided to fail on unexpected calls.
 */
public class EmptyCapabilities implements IExternalCapabilities {
    @Override
    public byte[] sha256(byte[] data) {
        throw RuntimeAssertionError.unimplemented("Not called in test");
    }

    @Override
    public byte[] blake2b(byte[] data) {
        return fakeHash(data);
    }

    @Override
    public byte[] keccak256(byte[] data) {
        throw RuntimeAssertionError.unimplemented("Not called in test");
    }

    @Override
    public boolean verifyEdDSA(byte[] data, byte[] signature, byte[] publicKey) {
        throw RuntimeAssertionError.unimplemented("Not called in test");
    }

    @Override
    public AionAddress generateContractAddress(AionAddress deployerAddress, BigInteger nonce) {
        // NOTE:  This address generation isn't anything particular.  It is just meant to be deterministic and derived from the tx.
        // It is NOT meant to be equivalent/similar to the implementation used by an actual kernel.
        byte[] senderAddressBytes = deployerAddress.toByteArray();
        byte[] raw = new byte[AionAddress.LENGTH];
        for (int i = 0; i < AionAddress.LENGTH - 1; ++i) {
            byte one = (i < senderAddressBytes.length) ? senderAddressBytes[i] : (byte)i;
            byte two = (i < nonce.toByteArray().length) ? nonce.toByteArray()[i] : (byte)i;
            // We write into the (i+1)th byte because the 0th byte is for the prefix.
            // This means we will have a collision if an address reaches nonces that agree on the first 31 bytes,
            // but that number is huge, so it's fine for testing purposes.
            raw[i+1] = (byte) (one + two);
        }
        raw[0] = TestingState.AVM_CONTRACT_PREFIX;
        return new AionAddress(raw);
    }

    @Override
    public InternalTransaction decodeSerializedTransaction(byte[] transactionPayload, AionAddress executor, long energyPrice, long energyLimit) {
        TestingMetaEncoder.MetaTransaction deserialized = TestingMetaEncoder.decode(transactionPayload);
        // Verify signature (for tests, we just use a [0x1] as "correctly signed").
        if (!Arrays.equals(new byte[] {0x1}, deserialized.signature)) {
            throw new IllegalArgumentException();
        }
        // Verify the executor.
        if (!executor.equals(deserialized.executor)) {
            throw new IllegalArgumentException();
        }
        // Aion transaction hash is defined as blake2b.
        byte[] hash = blake2b(transactionPayload);
        return (null != deserialized.targetAddress)
                ? InternalTransaction.contractCallInvokableTransaction(
                        InternalTransaction.RejectedStatus.NOT_REJECTED,
                        deserialized.senderAddress,
                        deserialized.targetAddress,
                        deserialized.nonce,
                        deserialized.value,
                        deserialized.data,
                        energyLimit,
                        energyPrice,
                        hash)
                : InternalTransaction.contractCreateInvokableTransaction(
                        InternalTransaction.RejectedStatus.NOT_REJECTED,
                        deserialized.senderAddress,
                        deserialized.nonce,
                        deserialized.value,
                        deserialized.data,
                        energyLimit,
                        energyPrice,
                        hash);
    }


    private static byte[] fakeHash(byte[] data) {
        byte[] hash = new byte[32];
        int u = Math.min(32, data.length);
        System.arraycopy(data, 0, hash, 32 - u, u);
        return hash;
    }
}
