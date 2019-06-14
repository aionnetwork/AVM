package org.aion.avm.tooling;

import org.aion.avm.core.AvmTransaction;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.types.AionAddress;
import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.tooling.crypto.CryptoUtil;
import org.aion.avm.tooling.hash.HashUtils;


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
    public AionAddress generateContractAddress(InternalTransaction tx) {
        return AddressUtil.generateContractAddress(tx);
    }

    @Override
    public AionAddress generateContractAddress(AvmTransaction tx) {
        return AddressUtil.generateContractAddress(tx);
    }
}
