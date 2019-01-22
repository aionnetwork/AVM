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
    private byte[] publicKeyBytes;
    private byte[] privateKeyBytes;
    private EdDSAPublicKey publicKeyObject;
    private EdDSAPrivateKey privateKeyObject;

    static {
        try {
            edDSAEngine = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        } catch (NoSuchAlgorithmException e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public Ed25519Key(byte[] privateKeyBytes, byte[] publicKeyBytes) throws InvalidKeySpecException{
        if (privateKeyBytes.length != SECKEY_BYTES){
            throw new IllegalArgumentException(IMPORT_PRIVATE_KEY_EXCEPTION_MESSAGE);
        } else if (publicKeyBytes.length != PUBKEY_BYTES ){
            throw new IllegalArgumentException(IMPORT_PUBLIC_KEY_EXCEPTION_MESSAGE);
        }

        this.privateKeyObject = new EdDSAPrivateKey(new PKCS8EncodedKeySpec(addSkPrefix(Hex.toHexString(privateKeyBytes))));
        this.publicKeyObject = new EdDSAPublicKey(new X509EncodedKeySpec(addPkPrefix(Hex.toHexString(publicKeyBytes))));
        setKeyComponents();
    }

    public Ed25519Key(){
        this.publicKeyBytes = new byte[PUBKEY_BYTES];
        this.privateKeyBytes = new byte[SECKEY_BYTES];

        KeyPairGenerator keyGen = new KeyPairGenerator();
        KeyPair keyPair = keyGen.generateKeyPair();

        this.publicKeyObject = (EdDSAPublicKey) keyPair.getPublic();
        this.privateKeyObject = (EdDSAPrivateKey) keyPair.getPrivate();
        setKeyComponents();
    }

    /**
     * Signs a message with this key.
     *
     * @param data the message that was signed
     * @return the signature
     */
    public ISignature sign(byte[] data) throws InvalidKeyException, SignatureException {
        edDSAEngine.initSign(this.privateKeyObject);
        byte[] sig = edDSAEngine.signOneShot(data);
        return new Ed25519Signature(this.publicKeyBytes, sig);
    }

    /**
     * Static method for just signing a message using given private key
     *
     * @param data the message that was signed
     * @param privateKey bytes representation of private key
     * @return the signature
     */
    public static ISignature sign(byte[] data, byte[] privateKey) throws InvalidKeyException, InvalidKeySpecException, SignatureException {
        EdDSAPrivateKey privateKeyObject = new EdDSAPrivateKey(new PKCS8EncodedKeySpec(addSkPrefix(Hex.toHexString(privateKey))));
        edDSAEngine.initSign(privateKeyObject);
        byte[] sig = edDSAEngine.signOneShot(data);
        return new Ed25519Signature(privateKey, sig);
    }

    /**
     * Verifies if a message is signed by the given signature.
     *
     * @param data the message that was signed
     * @param signature of signed message
     * @return verify result
     */
    public boolean verify(byte[] data, byte[] signature) throws InvalidKeyException, SignatureException {
        edDSAEngine.initVerify(this.publicKeyObject);
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
        return this.publicKeyBytes;
    }

    public byte[] getPrivKeyBytes() {
        return this.privateKeyBytes;
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
        this.publicKeyBytes = this.publicKeyObject.getAbyte();
        this.privateKeyBytes = this.privateKeyObject.getSeed();
    }

}
