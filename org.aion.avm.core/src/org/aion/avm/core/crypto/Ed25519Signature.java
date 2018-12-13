package org.aion.avm.core.crypto;

import java.util.Arrays;

/**
 * ED25519 signature implementation. Each {@link Ed25519Signature} contains two components, public
 * key and raw signature.
 *
 */
public class Ed25519Signature implements ISignature {

    private static final int LEN = Ed25519Key.PUBKEY_BYTES + Ed25519Key.SIG_BYTES;

    private byte[] pk;

    private byte[] sig;

    public Ed25519Signature(byte[] pk, byte[] sig) {
        this.pk = pk;
        this.sig = sig;
    }

    public static Ed25519Signature fromBytes(byte[] args) {
        if (args != null && args.length == LEN) {
            byte[] pk = Arrays.copyOfRange(args, 0, Ed25519Key.PUBKEY_BYTES);
            byte[] sig = Arrays.copyOfRange(args, Ed25519Key.PUBKEY_BYTES, LEN);
            return new Ed25519Signature(pk, sig);
        } else {
            System.err.println("Ed25519 signature decode failed!");
            return null;
        }
    }

    @Override
    public byte[] toBytes() {
        byte[] buf = new byte[LEN];
        System.arraycopy(pk, 0, buf, 0, Ed25519Key.PUBKEY_BYTES);
        System.arraycopy(sig, 0, buf, Ed25519Key.PUBKEY_BYTES, Ed25519Key.SIG_BYTES);

        return buf;
    }

    @Override
    public byte[] getSignature() {
        return sig;
    }

    @Override
    public byte[] getPubkey(byte[] msg) {
        return pk;
    }

    @Override
    public String toString() {

        return "[pk: "
                + (this.pk == null ? "null" : CryptoUtil.toHexString(this.pk))
                + " signature: "
                + (this.sig == null ? "null" : CryptoUtil.toHexString(this.sig))
                + "]";
    }
}
