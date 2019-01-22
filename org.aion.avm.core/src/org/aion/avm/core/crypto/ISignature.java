package org.aion.avm.core.crypto;

/**
 * This class is the signature standard based from the aion kernel:
 * (https://github.com/aionnetwork/aion/blob/master/modCrypto/src/org/aion/crypto/ISignature.java).
 * An instance implementing this interface should be able to generate the 3 values:
 * - raw signature
 * - public key
 * - aion address
 */
public interface ISignature {
    /**
     * Used when serialization transactions to combine both the public key and the data signature for later verification.
     * @return The concatenated public key followed by signature.
     */
    byte[] toPublicKeyAndSignaturePair();

    /**
     * Returns the raw signature.
     */
    byte[] getSignature();

    /**
     * Returns the public key, encoded or recovered.
     *
     * @param msg Only required by Secp256k1; pass null if you're using ED25519
     */
    byte[] getPublicKey(byte[] msg);
}
