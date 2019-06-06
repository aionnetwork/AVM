package org.aion.avm.tooling.crypto;

import net.i2p.crypto.eddsa.*;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;

import org.aion.avm.core.util.Helpers;

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
    private static final String skEncodedPrefix = "302e020100300506032b657004220420";
    private static final String pkEncodedPrefix = "302a300506032b6570032100";

    private final EdDSAEngine edDSAEngine;
    private byte[] publicKeyBytes;
    private byte[] privateKeyBytes;
    private EdDSAPublicKey publicKeyObject;
    private EdDSAPrivateKey privateKeyObject;


    public Ed25519Key(byte[] privateKeyBytes, byte[] publicKeyBytes) throws InvalidKeySpecException{
        this.edDSAEngine = createEngine();
        
        if (privateKeyBytes.length != SECKEY_BYTES){
            throw new IllegalArgumentException(IMPORT_PRIVATE_KEY_EXCEPTION_MESSAGE);
        } else if (publicKeyBytes.length != PUBKEY_BYTES ){
            throw new IllegalArgumentException(IMPORT_PUBLIC_KEY_EXCEPTION_MESSAGE);
        }

        this.privateKeyObject = new EdDSAPrivateKey(new PKCS8EncodedKeySpec(addSkPrefix(Helpers.bytesToHexString(privateKeyBytes))));
        this.publicKeyObject = new EdDSAPublicKey(new X509EncodedKeySpec(addPkPrefix(Helpers.bytesToHexString(publicKeyBytes))));
        
        this.publicKeyBytes = this.publicKeyObject.getAbyte();
        this.privateKeyBytes = this.privateKeyObject.getSeed();
    }

    /**
     * Signs a message with this key.
     *
     * @param data the message that was signed
     * @return The bytes making up the raw signature (not including the public key)
     */
    public byte[] sign(byte[] data) throws InvalidKeyException, SignatureException {
        edDSAEngine.initSign(this.privateKeyObject);
        return edDSAEngine.signOneShot(data);
    }

    /**
     * Static method for just signing a message using given private key
     *
     * @param data the message that was signed
     * @param privateKey bytes representation of private key
     * @return The bytes making up the raw signature (not including the public key)
     */
    public static byte[] sign(byte[] data, byte[] privateKey) throws InvalidKeyException, InvalidKeySpecException, SignatureException {
        EdDSAPrivateKey privateKeyObject = new EdDSAPrivateKey(new PKCS8EncodedKeySpec(addSkPrefix(Helpers.bytesToHexString(privateKey))));
        EdDSAEngine engine = createEngine();
        engine.initSign(privateKeyObject);
        return engine.signOneShot(data);
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
        EdDSAPublicKey publicKey = new EdDSAPublicKey(new X509EncodedKeySpec(addPkPrefix(Helpers.bytesToHexString(pk))));
        EdDSAEngine engine = createEngine();
        engine.initVerify(publicKey);
        return engine.verifyOneShot(data, signature);
    }


    public byte[] getPubKeyBytes() {
        return this.publicKeyBytes;
    }

    public byte[] getPrivKeyBytes() {
        return this.privateKeyBytes;
    }

    /**
     * Add encoding prefix for importing public key
     */
    private static byte[] addPkPrefix(String pkString){
        String pkEncoded = pkEncodedPrefix + pkString;
        return Utils.hexToBytes(pkEncoded);
    }

    /**
     * Add encoding prefix for importing private key
     */
    private static byte[] addSkPrefix(String skString){
        String skEncoded = skEncodedPrefix + skString;
        return Utils.hexToBytes(skEncoded);
    }

    private static EdDSAEngine createEngine() {
        EdDSAEngine engine = null;
        try {
            engine = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        } catch (NoSuchAlgorithmException e) {
            // If we see this exception, it means the AVM isn't properly installed.
            // This is just used in tests so we don't expect a failure.
            throw new AssertionError(e);
        }
        return engine;
    }
}
