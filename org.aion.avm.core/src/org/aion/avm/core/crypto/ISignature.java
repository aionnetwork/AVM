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
     * Converts into a byte array.
     */
    byte[] toBytes();

    /**
     * Returns the raw signature.
     */
    byte[] getSignature();

    /**
     * Returns the public key, encoded or recovered.
     *
     * @param msg Only required by Secp256k1; pass null if you're using ED25519
     */
    byte[] getPubkey(byte[] msg);
}
