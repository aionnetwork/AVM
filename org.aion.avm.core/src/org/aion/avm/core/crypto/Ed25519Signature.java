package org.aion.avm.core.crypto;

import java.util.Arrays;

/**
 * ED25519 signature implementation. Each {@link Ed25519Signature} contains two components, public
 * key and raw signature.
 *
 */
public class Ed25519Signature implements ISignature {

    private static final int LEN = Ed25519Key.PUBKEY_BYTES + Ed25519Key.SIG_BYTES;

    private byte[] publicKey;
    private byte[] signature;

    public Ed25519Signature(byte[] publicKey, byte[] signature) {
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public static Ed25519Signature fromCombinedPublicKeyAndSignature(byte[] combined) {
        if (combined != null && combined.length == LEN) {
            byte[] publicKey = Arrays.copyOfRange(combined, 0, Ed25519Key.PUBKEY_BYTES);
            byte[] sig = Arrays.copyOfRange(combined, Ed25519Key.PUBKEY_BYTES, LEN);
            return new Ed25519Signature(publicKey, sig);
        } else {
            System.err.println("Ed25519 signature decode failed!");
            return null;
        }
    }

    @Override
    public byte[] toPublicKeyAndSignaturePair() {
        byte[] buf = new byte[LEN];
        System.arraycopy(this.publicKey, 0, buf, 0, Ed25519Key.PUBKEY_BYTES);
        System.arraycopy(this.signature, 0, buf, Ed25519Key.PUBKEY_BYTES, Ed25519Key.SIG_BYTES);

        return buf;
    }

    @Override
    public byte[] getSignature() {
        return this.signature;
    }

    @Override
    public byte[] getPublicKey(byte[] msg) {
        return this.publicKey;
    }

    @Override
    public String toString() {

        return "[pk: "
                + (this.publicKey == null ? "null" : CryptoUtil.toHexString(this.publicKey))
                + " signature: "
                + (this.signature == null ? "null" : CryptoUtil.toHexString(this.signature))
                + "]";
    }
}
