package org.aion.avm.core.crypto;

import net.i2p.crypto.eddsa.Utils;

import java.nio.charset.StandardCharsets;

import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Test;


public class Ed25519SignatureTest {
    private static final byte[] PUB_KEY_BYTES = Utils.hexToBytes("af6377ce7c22f3f4752f275cf434a3fbfed3ccb172a15197b9e1011252302fda");
    private static final byte[] TEST_SIGNATURE = Utils.hexToBytes("4458846e351cca3607e31a798adc25ef9e729f622f9e1450f849ca73e86567d58f343832f4c2fd3e30eef9a6f5bf34a4b968598780ab0aa9aa1b393fae7fec03");
    private static final Ed25519Signature defaultSignature = Ed25519Signature.fromPublicKeyAndSignature(PUB_KEY_BYTES, TEST_SIGNATURE);

    @Test
    public void testFromBytes(){
        byte[] input = getInput();

        Ed25519Signature signature = Ed25519Signature.fromCombinedPublicKeyAndSignature(input);
        Assert.assertNotNull(signature);
        Assert.assertArrayEquals(PUB_KEY_BYTES, signature.getPublicKey(null)); // pass null for ed25519
        Assert.assertArrayEquals(TEST_SIGNATURE, signature.getSignature());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromBytesInvalidLength(){
        // generate signature from given arguments, providing wrong length
        byte[] input = new byte[PUB_KEY_BYTES.length + TEST_SIGNATURE.length - 1];
        System.arraycopy(PUB_KEY_BYTES,0, input, 0, PUB_KEY_BYTES.length);
        System.arraycopy(TEST_SIGNATURE, 0, input, PUB_KEY_BYTES.length, TEST_SIGNATURE.length-1);

        // This should fail.
        Ed25519Signature.fromCombinedPublicKeyAndSignature(input);
    }

    @Test
    public void testToBytes(){
        byte[] byteRepresentationOfSignature = defaultSignature.toPublicKeyAndSignaturePair();

        Assert.assertArrayEquals(getInput(), byteRepresentationOfSignature);
    }

    @Test
    public void testToString(){
        String expectedOutput = "[pk: af6377ce7c22f3f4752f275cf434a3fbfed3ccb172a15197b9e1011252302fda signature: 4458846e351cca3607e31a798adc25ef9e729f622f9e1450f849ca73e86567d58f343832f4c2fd3e30eef9a6f5bf34a4b968598780ab0aa9aa1b393fae7fec03]";
        String actualOutput = defaultSignature.toString();
        Assert.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testCombinedPublicKeySignature() throws Exception {
        // Test that we can create a signature of the shape used in a serialized transaction where the public key and signature are concatenated.
        // NOTE: This test is more just to demonstrate what this pattern is, as oppose to strictly testing it.
        byte[] publicKey = Helpers.hexStringToBytes("ac49f81893b2cbc2fe5d9cd6d8538afd8fbd4fb9318d88074f3b2761489a25cb");
        byte[] privateKey = Helpers.hexStringToBytes("58ccacf693a85b5a9781e8324212246bb1afe27f458b39dcabaf66299fd99b09");
        Ed25519Key key = new Ed25519Key(privateKey, publicKey);
        byte[] payload = "Some test message".getBytes(StandardCharsets.UTF_8);
        byte[] signature = key.sign(payload);
        byte[] expectedSignature = Helpers.hexStringToBytes("47098c11e65439483d5c099481d1a589a03116bb04d355c1844fe9c9fd09c7784c40bfcc95daf23401fcbe99ccf6589235f2f09f2977ea77ee2a7cbc07e25f0a");
        Assert.assertArrayEquals(expectedSignature, signature);
        
        Ed25519Signature signatureObject = Ed25519Signature.fromPublicKeyAndSignature(key.getPubKeyBytes(), signature);
        byte[] packedResult = signatureObject.toPublicKeyAndSignaturePair();
        byte[] expectedResult = new byte[publicKey.length + signature.length];
        System.arraycopy(publicKey, 0, expectedResult, 0, publicKey.length);
        System.arraycopy(expectedSignature, 0, expectedResult, publicKey.length, expectedSignature.length);
        Assert.assertArrayEquals(expectedResult, packedResult);
    }

    /**
     * generate input for fromBytes() with default arguments
     */
    private byte[] getInput(){
        byte[] input = new byte[PUB_KEY_BYTES.length + TEST_SIGNATURE.length];
        System.arraycopy(PUB_KEY_BYTES,0, input, 0, PUB_KEY_BYTES.length);
        System.arraycopy(TEST_SIGNATURE, 0, input, PUB_KEY_BYTES.length, TEST_SIGNATURE.length);
        return input;
    }
}
