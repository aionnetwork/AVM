package org.aion.avm.core.crypto;

import net.i2p.crypto.eddsa.*;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.aion.avm.internal.RuntimeAssertionError;
import org.spongycastle.util.encoders.Hex;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Ed25519Key {

    protected static final int PUBKEY_BYTES = 32;
    protected static final int SECKEY_BYTES = 32;
    protected static final int SIG_BYTES = 64;
    protected static final String IMPORT_PRIVATE_KEY_EXCEPTION_MESSAGE = "Private key should have a length of 32.";
    protected static final String IMPORT_PUBLIC_KEY_EXCEPTION_MESSAGE = "Public key should have a length of 32.";

    // statics
    private static final EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
    private static final EdDSAEngine edDSAEngine;
    private static final String skEncodedPrefix = "302e020100300506032b657004220420";
    private static final String pkEncodedPrefix = "302a300506032b6570032100";

    // public and private key representations
    private byte[] pk;
    private byte[] sk;
    private EdDSAPublicKey publicKey;
    private EdDSAPrivateKey privateKey;

    static {
        try {
            edDSAEngine = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        } catch (NoSuchAlgorithmException e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public Ed25519Key(byte[] skBytes, byte[] pkBytes) throws InvalidKeySpecException{
        if (skBytes.length != 32){
            throw new IllegalArgumentException(IMPORT_PRIVATE_KEY_EXCEPTION_MESSAGE);
        } else if (pkBytes.length != 32 ){
            throw new IllegalArgumentException(IMPORT_PUBLIC_KEY_EXCEPTION_MESSAGE);
        }

        this.privateKey = new EdDSAPrivateKey(new PKCS8EncodedKeySpec(addSkPrefix(Hex.toHexString(skBytes))));
        this.publicKey = new EdDSAPublicKey(new X509EncodedKeySpec(addPkPrefix(Hex.toHexString(pkBytes))));
        setKeyComponents();
    }

    public Ed25519Key(){
        this.pk = new byte[PUBKEY_BYTES];
        this.sk = new byte[SECKEY_BYTES];

        KeyPairGenerator keyGen = new KeyPairGenerator();
        KeyPair keyPair = keyGen.generateKeyPair();

        this.publicKey = (EdDSAPublicKey) keyPair.getPublic();
        this.privateKey = (EdDSAPrivateKey) keyPair.getPrivate();
        setKeyComponents();
    }

    /**
     * Signs a message with this key.
     *
     * @param data the message that was signed
     * @return the signature
     */
    public ISignature sign(byte[] data) throws InvalidKeyException, SignatureException {
        edDSAEngine.initSign(privateKey);
        byte[] sig = edDSAEngine.signOneShot(data);
        return new Ed25519Signature(pk, sig);
    }

    /**
     * Static method for just signing a message using given private key
     *
     * @param data the message that was signed
     * @param sk bytes representation of private key
     * @return the signature
     */
    public static ISignature sign(byte[] data, byte[] sk) throws InvalidKeyException, InvalidKeySpecException, SignatureException {
        EdDSAPrivateKey privateKey = new EdDSAPrivateKey(new PKCS8EncodedKeySpec(addSkPrefix(Hex.toHexString(sk))));
        edDSAEngine.initSign(privateKey);
        byte[] sig = edDSAEngine.signOneShot(data);
        return new Ed25519Signature(sk, sig);
    }

    /**
     * Verifies if a message is signed by the given signature.
     *
     * @param data the message that was signed
     * @param signature of signed message
     * @return verify result
     */
    public boolean verify(byte[] data, byte[] signature) throws InvalidKeyException, SignatureException {
        edDSAEngine.initVerify(this.publicKey);
        return edDSAEngine.verifyOneShot(data, signature);
    }

    /**
     * Static method for just verifying a message using given public key
     *
     * @param data the message that was signed
     * @param signature of signed message
     * @param pk bytes representation of public key
     * @return verify result
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pk) throws InvalidKeyException, InvalidKeySpecException, SignatureException {
        EdDSAPublicKey publicKey = new EdDSAPublicKey(new X509EncodedKeySpec(addPkPrefix(Hex.toHexString(pk))));
        edDSAEngine.initVerify(publicKey);
        return edDSAEngine.verifyOneShot(data, signature);
    }


    public byte[] getPubKeyBytes() {
        return pk;
    }

    public byte[] getPrivKeyBytes() {
        return sk;
    }

    /**
     * Add encoding prefix for importing private key
     */
    private static byte[] addPkPrefix(String pkString){
        String pkEncoded = pkEncodedPrefix + pkString;
        return Utils.hexToBytes(pkEncoded);
    }

    /**
     * Add encoding prefix for importing public key
     */
    private static byte[] addSkPrefix(String skString){
        String skEncoded = skEncodedPrefix + skString;
        return Utils.hexToBytes(skEncoded);
    }

    /**
     * Extract public and private key byte[] representations
     */
    private void setKeyComponents(){
        this.pk = publicKey.getAbyte();
        this.sk = privateKey.getSeed();
    }

}
