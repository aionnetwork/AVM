package org.aion.avm.embed;

import org.aion.types.AionAddress;
import org.aion.types.InternalTransaction;

import java.math.BigInteger;

import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.embed.crypto.CryptoUtil;
import org.aion.avm.embed.hash.HashUtils;


/**
 * The standard capabilities provided to the AVM by our tests and tooling.
 */
public class StandardCapabilities implements IExternalCapabilities {
    @Override
    public byte[] sha256(byte[] data) {
        return HashUtils.sha256(data);
    }

    @Override
    public byte[] blake2b(byte[] data) {
        return HashUtils.blake2b(data);
    }

    @Override
    public byte[] keccak256(byte[] data) {
        return HashUtils.keccak256(data);
    }

    @Override
    public boolean verifyEdDSA(byte[] data, byte[] signature, byte[] publicKey) {
        return CryptoUtil.verifyEdDSA(data, signature, publicKey);
    }

    @Override
    public AionAddress generateContractAddress(AionAddress deployerAddress, BigInteger nonce) {
        return AddressUtil.generateContractAddress(deployerAddress, nonce);
    }

    @Override
    public InternalTransaction decodeSerializedTransaction(byte[] transactionPayload, AionAddress executor, long energyPrice, long energyLimit) {
        throw new AssertionError("TODO (AKI-359):  Implement decodeSerializedTransaction based on standard network encoding (once it has been finalized)");
    }
}
