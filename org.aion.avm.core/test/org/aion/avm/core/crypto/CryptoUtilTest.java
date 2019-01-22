package org.aion.avm.core.crypto;

import net.i2p.crypto.eddsa.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtilTest {

    private static byte[] privateKeyBytes = Utils.hexToBytes("538d0a994bbe8ba995cb7597c6fa19309a99e4ba0a1ea50bbae429e99db15356");
    private static byte[] publicKeyBytes = Utils.hexToBytes("8c11e9a4772bb651660a5a5e412be38d33f26b2de0487115d472a6c8bf60aa19");
    private byte[] testMessage = "test message".getBytes();
    private byte[] messageSignature = Utils.hexToBytes("0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61fb73777830ba3e92e4aa1832d41ec00c6e1d3bdd193e779cc2d51cb10d908c0a");

    @Test
    public void testSignEdDSA(){
        ISignature signature = null;

        try {
            signature = CryptoUtil.signEdDSA(testMessage, privateKeyBytes);
        } catch (InvalidKeyException | InvalidKeySpecException | SignatureException e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(signature);
        Assert.assertArrayEquals(messageSignature, signature.getSignature());
    }

    @Test
    public void testVerifyEdDSA(){
        boolean verifyResult = false;

        try{
            verifyResult = CryptoUtil.verifyEdDSA(testMessage, messageSignature, publicKeyBytes);
        } catch (SignatureException | InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(verifyResult);
    }

    @Test
    public void testVerifyEdDSAFail(){
        boolean verifyResult = true;

        // modify the signature slightly
        byte[] modifiedSignature = messageSignature;
        modifiedSignature[0] = (byte) ((int) modifiedSignature[0] + 1);

        try{
            verifyResult = CryptoUtil.verifyEdDSA(testMessage, messageSignature, publicKeyBytes);
        } catch (SignatureException | InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        Assert.assertFalse(verifyResult);
    }

    @Test
    public void testSignAndVerify(){
        Ed25519Key key = new Ed25519Key();
        byte[] privateKey = key.getPrivKeyBytes();
        byte[] publicKey = key.getPubKeyBytes();
        byte[] testMessage = "create key from scratch".getBytes();

        // signing
        ISignature signature = null;
        try {
            signature = CryptoUtil.signEdDSA(testMessage, privateKey);
        } catch (InvalidKeyException | InvalidKeySpecException | SignatureException e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(signature);

        // verify
        boolean verifyResult = false;
        try{
            verifyResult = CryptoUtil.verifyEdDSA(testMessage, signature.getSignature(), publicKey);
        } catch (SignatureException | InvalidKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(verifyResult);
    }
}
