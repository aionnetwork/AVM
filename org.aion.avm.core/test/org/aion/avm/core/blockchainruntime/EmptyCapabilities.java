package org.aion.avm.core.blockchainruntime;

import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.TestingKernel;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.TransactionInterface;


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
        throw RuntimeAssertionError.unimplemented("Not called in test");
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
    public Address generateContractAddress(TransactionInterface tx) {
        // NOTE:  This address generation isn't anything particular.  It is just meant to be deterministic and derived from the tx.
        // It is NOT meant to be equivalent/similar to the implementation used by an actual kernel.
        byte[] hash = tx.getTransactionHash();
        byte[] nonce = tx.getNonce();
        byte[] raw = new byte[Address.SIZE];
        for (int i = 0; i < Address.SIZE; ++i) {
            byte one = (i < hash.length) ? hash[i] : (byte)i;
            byte two = (i < nonce.length) ? nonce[i] : (byte)i;
            raw[i] = (byte) (one + two);
        }
        raw[0] = TestingKernel.AVM_CONTRACT_PREFIX;
        return Address.wrap(raw);
    }
}
