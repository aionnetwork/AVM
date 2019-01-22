package org.aion.avm.core.crypto;

import net.i2p.crypto.eddsa.Utils;
import org.junit.Assert;
import org.junit.Test;

public class Ed25519SignatureTest {

    private static final byte[] PUB_KEY_BYTES = Utils.hexToBytes("af6377ce7c22f3f4752f275cf434a3fbfed3ccb172a15197b9e1011252302fda");
    private static final byte[] TEST_SIGNATURE = Utils.hexToBytes("4458846e351cca3607e31a798adc25ef9e729f622f9e1450f849ca73e86567d58f343832f4c2fd3e30eef9a6f5bf34a4b968598780ab0aa9aa1b393fae7fec03");
    private static final Ed25519Signature defaultSignature = new Ed25519Signature(PUB_KEY_BYTES, TEST_SIGNATURE);

    @Test
    public void testFromBytes(){
        byte[] input = getInput();

        Ed25519Signature signature = Ed25519Signature.fromCombinedPublicKeyAndSignature(input);
        Assert.assertNotNull(signature);
        Assert.assertArrayEquals(PUB_KEY_BYTES, signature.getPublicKey(null)); // pass null for ed25519
        Assert.assertArrayEquals(TEST_SIGNATURE, signature.getSignature());
    }

    @Test
    public void testFromBytesInvalidLength(){
        // generate signature from given arguments, providing wrong lentgh
        byte[] input = new byte[PUB_KEY_BYTES.length + TEST_SIGNATURE.length - 1];
        System.arraycopy(PUB_KEY_BYTES,0, input, 0, PUB_KEY_BYTES.length);
        System.arraycopy(TEST_SIGNATURE, 0, input, PUB_KEY_BYTES.length, TEST_SIGNATURE.length-1);

        Ed25519Signature signature = Ed25519Signature.fromCombinedPublicKeyAndSignature(input);
        Assert.assertNull(signature);
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
