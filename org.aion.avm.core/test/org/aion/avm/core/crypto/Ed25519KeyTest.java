package org.aion.avm.core.crypto;

import net.i2p.crypto.eddsa.Utils;
import org.aion.avm.core.util.HashUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class Ed25519KeyTest {

    private static Ed25519Key key1, key2;
    private static final byte[] KEY2_PRI_KEY_BYTES = Utils.hexToBytes("14eb6689c5fea41cbbd28bab9fc354725aca39855c58d868973263f8650939ce");
    private static final byte[] KEY2_PUB_KEY_BYTES = Utils.hexToBytes("af6377ce7c22f3f4752f275cf434a3fbfed3ccb172a15197b9e1011252302fda");
    private static final byte[] TEST_MESSAGE = "testing message 123".getBytes();
    private static final byte[] TEST_SIGNATURE = Utils.hexToBytes("4458846e351cca3607e31a798adc25ef9e729f622f9e1450f849ca73e86567d58f343832f4c2fd3e30eef9a6f5bf34a4b968598780ab0aa9aa1b393fae7fec03");

    private static final byte[] TEST_STATIC_MESSAGE = "testing message 12345".getBytes();
    private static final byte[] TEST_STATIC_SIGNATURE = Utils.hexToBytes("cf3625aa16746a535d1e14aeeac9c2f0d7ee16aac7b2291a5809f5ca7aa434397b8ea5b2067c01b3510fbdd1d03d6ec12b3b587a406571a10d45f52736433f01");

    /**
     * Increases these values to see more averaged results (ex. 10, 100 ...).
     */
    private static final int BENCHMARK_TEST_LOOP_COUNT = 1;
    private static final int BENCHMARK_TEST_AMOUNT_UNIT = 1;

    @BeforeClass
    public static void setup() throws InvalidKeySpecException {
        // create Ed25519Key instance using both the normal constructor and import constructor.
        key1 = new Ed25519Key();
        key2 = new Ed25519Key(KEY2_PRI_KEY_BYTES, KEY2_PUB_KEY_BYTES);
    }

    @Test
    public void testImportInvalidPrivateKey() {
        byte[] invalidPrivateKey = new byte[KEY2_PRI_KEY_BYTES.length - 1];
        System.arraycopy(KEY2_PRI_KEY_BYTES, 0, invalidPrivateKey, 0, invalidPrivateKey.length);
        Ed25519Key key = null;

        try{
            key = new Ed25519Key(invalidPrivateKey, KEY2_PUB_KEY_BYTES);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            Assert.assertEquals(Ed25519Key.IMPORT_PRIVATE_KEY_EXCEPTION_MESSAGE, e.getMessage());
        }

        Assert.assertNull(key);
    }

    @Test
    public void testImportInvalidPublicKey() {
        byte[] invalidPublicKey = new byte[KEY2_PUB_KEY_BYTES.length - 1];
        System.arraycopy(KEY2_PUB_KEY_BYTES, 0, invalidPublicKey, 0, invalidPublicKey.length);
        Ed25519Key key = null;

        try{
            key = new Ed25519Key(KEY2_PRI_KEY_BYTES, invalidPublicKey);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            Assert.assertEquals(Ed25519Key.IMPORT_PUBLIC_KEY_EXCEPTION_MESSAGE, e.getMessage());
        }

        Assert.assertNull(key);
    }

    @Test
    public void testKey1SignAndVerify() throws SignatureException, InvalidKeyException {
        // sign
        ISignature signature = key1.sign(TEST_MESSAGE);

        // verify
        boolean verifyResult = key1.verify(TEST_MESSAGE, signature.getSignature());
        Assert.assertTrue(verifyResult);
    }

    @Test
    public void testKey2Sign() throws SignatureException, InvalidKeyException {
        ISignature signature = key2.sign(TEST_MESSAGE);
        Assert.assertArrayEquals(TEST_SIGNATURE, signature.getSignature());
    }

    @Test
    public void testKey2Verify() throws InvalidKeyException, SignatureException {
        boolean verifyResult = key2.verify(TEST_MESSAGE, TEST_SIGNATURE);
        Assert.assertTrue(verifyResult);
    }

    @Test
    public void testStaticSign() throws InvalidKeySpecException, InvalidKeyException, SignatureException {
        ISignature signature = Ed25519Key.sign(TEST_STATIC_MESSAGE, key2.getPrivKeyBytes());
        Assert.assertArrayEquals(TEST_STATIC_SIGNATURE, signature.getSignature());
    }

    @Test
    public void testStaticVerify() throws InvalidKeySpecException, InvalidKeyException, SignatureException {
        boolean verifyResult = Ed25519Key.verify(TEST_STATIC_MESSAGE, TEST_STATIC_SIGNATURE, key2.getPubKeyBytes());
        Assert.assertTrue(verifyResult);
    }

    @Test
    public void benchmark() throws InvalidKeyException, InvalidKeySpecException, SignatureException{
        for (int j = 0; j < BENCHMARK_TEST_LOOP_COUNT; j ++) {
            final Ed25519Key key = new Ed25519Key();
            byte[] pk = key.getPubKeyBytes();
            byte[] input = HashUtils.blake2b("test".getBytes());
            ISignature[] sig = new ISignature[BENCHMARK_TEST_AMOUNT_UNIT];

            // warm up
            for (int i = 0; i < 10; i++) {
                key.sign(input);
            }

            long timStart, timeEnd;

            // time signing
            timStart = System.nanoTime();
            for (int i = 0; i < BENCHMARK_TEST_AMOUNT_UNIT; i++) {
                key.sign(input);
            }
            timeEnd = System.nanoTime();
            System.out.println("ed25519     sign: " + (timeEnd - timStart) / BENCHMARK_TEST_AMOUNT_UNIT + " ns / call");

            // we don't want the signature assignment statement from above to added into signing time
            for (int i = 0; i < BENCHMARK_TEST_AMOUNT_UNIT; i++) {
                sig[i] = key.sign(input);
            }

            // time verify
            timStart = System.nanoTime();
            for (int i = 0; i < BENCHMARK_TEST_AMOUNT_UNIT; i++) {
                Ed25519Key.verify(input, sig[i].getSignature(), pk);
            }
            timeEnd = System.nanoTime();
            System.out.println("ed25519   verify: " + (timeEnd - timStart) / BENCHMARK_TEST_AMOUNT_UNIT + " ns / call");
        }
    }
}
