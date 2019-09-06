package org.aion.avm.embed;

import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.hash.Blake2b;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Blake2bPersonalizationTest {

    private static final int n = 210;
    private static final int k = 9;
    private static final int indicesPerHashOutput = 512 / n;
    private static final int indicesHashLength = (n + 7) / 8;
    private static final int collisionBitLength = n / (k + 1);
    private static final int collisionByteLength = (collisionBitLength + 7) / 8;
    private static final int hashLength = (k + 1) * collisionByteLength;
    private static final int finalFullWidth = 2 * collisionByteLength + (Integer.BYTES * (1 << k));
    private static final Blake2b.Param initState = getParam(n, k);


    /**
     * This test class is created as a reference to the usage of blake2b personalization implementation,
     * which we use for the Equihash mining verification.
     */

    @Test
    public void testBlake2bPersonalization(){
        // example solution values
        byte[] solution = Helpers.hexStringToBytes("011ecfb957609b0c87104d3e8079bb5415d62e7093b7113e0e68d95303692f485b364e68fcd1c389a39e4ca906d64bc553772ac2ff636726f9effc69b8b035b5a5e21820c3a0eaa931b86ebe971a009fbf0842620d0e44330533b238ead0ce287fd55b38bc3287b2a4d246d5398b267b2e44525a1e6139e6a4453eefc57f7da8093d2a8322840e231a45bfe53e2e452cd30240a38dbb05f83f0952a27bfd6398ea4b77cd68ca90af82774cf1fc372818026637ca86e9334ffaba585daafa64078b7406b355ae301889c41e264e963d780936ba8ec587f7c20f207aa906e12da7b5a110cd9e540f22863d48705c4845f5ec6c08f7f95ee4b1e30aaa99a939eebfe62ab809a3a883480513b6969233795c629b111388f57b7756e5c779da537031dfd21d088458f3f4ce7e9fba990feb64d3f771dd3e467b6258aae3f0386d3f491f97195a54b55930cf7d3ffed525903da1fafb69d65a657baf212e98e27e842a07cc1027c1a6eb2c65621a173b06498592720fac88ca15580973f1f5690937dcab7f9c8bca36895abe32b2c8202963f9a36616272d98d62857d5166216bc5539ffaa53b211d4828662e2ab7bee6c48abcaae3874173e15260ac03ace94763bcca6b9811e59d1fb21f6d79c75d2cd135e673b55d62706f09e3b1c98dcdf7b6ae8893aa9b3317a2ec0ee037ec91ca10453ba65e498d997f1f0a10975fbfeac7758da8eab9b728714fba97c88a41ba86c020c26e49a9f5576eb58c1764769da25b29526a898294b4b809a93ca15cceed993155522263e14296ec635cce62e50aae0291a96e734ac2a3ee9cd4dfe9687b278ce593fd9f6df02a8f504b0b9afb2ce7adcc5cdfa18bc9e77134cc95cab2638e6efa6ae1e80da2b05f2cc6bee1356247e3ae33e24bec7311d8c735ce7249c1911eb3cbe2d14ae39153c47c829a7434417de37f98446821ddc5e701973232193e59266ef32f31ba44f4886f6583167eb6904b3428efca0c644ab8d1732bb227f8d78133729b078058b6ab514509f13f5563407bb13a16f931fe2fc3a9f1350e6e878eb70a438b442a51f7bdae56a7c77eb28d12c22e207f3cddd01fcbd6530a9c281876648e333a3e405aef2d4564abfdbad49b716dce5bd7b1d9a537f09b10bd3d61aad257844ac139f3c82ba99f78745ddafab7007e771965f4be732757876186cbcc221680a367e6ae1156ddeb26d79a4b5fa558358e0b7a83c6a5430f093d40c6508977676eb272838a20c77c07e1447c2d07b4a8c248d5b44e3e9bf2df20b94292daadbfb4871736dc91b13960bfe02394d0c3b1fd3960bb701c62ca93377e38f2724466be23c0870b446022c2f6679bba73a49a802fa11e6dfbf1972db45b9049b34afa2fbc7859a3de1a7772f1c2d98b3a4f16eaaae459d4ac2bd0b0bde7f74ee861e22ff63b9fda0bffa74c1240eb2c66f6cc9dcbc57816971265ff7480db06d273a9d1a47ac2729f98f3dc321058727638e6435725d7b1c4bc519540436c697fe6e082fdb791944d57f0c9f4e4a6d7005d898a73be03bd33b0ec477cc6f513fe33cb41c1011713f8bf4d731b864af197482483209add1bc2d821a5851220828ce1bed927906cc82c5624849d3e9e4ce777397d336adc242ff74380b7b7c68d5582c72eb02b8289ff37f18f583c4e56cfa0e54071a5187500732938e5553be2704e74297faae161988d0def98e69ad7c23f538e0a3d5bac5fef363461d0c6d4b0c95633b9e6ec84a24a7d4cd707e8f6b7fe6792e6e7ac0127a015cf2e41234ac9374787a637b761fb31ca4d0e78f03a61930560b4b38cfe3769bd30077f74046f2aded27d5d550f2ac906603eb09b34c6c74f7dc3d26c3189cf87b62b97c93cc3205279cdff6254d3289c7445a1fddba35b779ae16cb5d9002ee513be37633719a2928a319c458a5af29c96066065799da470d7c35f0973fbebf749dd6850c5a5e0b43ea095b489af2ba77e2d1");
        byte[] blockHeader = Helpers.hexStringToBytes("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        byte[] nonce = Helpers.hexStringToBytes("0100000000000000000000000000000000000000000000000000000000000000");

        boolean result = isValidSolution(solution, blockHeader, nonce);
        Assert.assertTrue(result);
    }

    /**
     * Example implementation of a checker, Determines if a solution is valid. This is based off EquiValidator.java
     * implementation in aion java kernel, simplified here. It uses the java implementation of blake2b.
     *
     *
     * @param solution The solution in minimal form.
     * @param blockHeader The block header.
     * @param nonce The nonce for the solution.
     * @return True if the solution is valid for the blockHeader and nonce.
     * @throws NullPointerException when given null input
     */
    private boolean isValidSolution(byte[] solution, byte[] blockHeader, byte[] nonce){
        Blake2b blake = Blake2b.Digest.newInstance(initState);

        FullStepRow[] X = new FullStepRow[1 << k];
        byte[] tmpHash;
        int j = 0;

        for (int i : getIndicesFromMinimal(solution, collisionBitLength)) {

            // as reuse blake instance, need reset before every round.
            blake.reset();

            // Build H(I | V ...

            // I = block header minus nonce and solution
            blake.update(blockHeader, 0, blockHeader.length);

            // V = nonce
            blake.update(nonce, 0, nonce.length);

            byte[] x = intToBytesLE(i / indicesPerHashOutput);

            blake.update(x, 0, x.length);

            tmpHash = blake.digest();

            X[j] =
                    new FullStepRow(
                            finalFullWidth,
                            Arrays.copyOfRange(
                                    tmpHash,
                                    (i % indicesPerHashOutput) * indicesHashLength,
                                    ((i % indicesPerHashOutput) * indicesHashLength) + hashLength),
                            indicesHashLength,
                            hashLength,
                            collisionBitLength,
                            i);
            j++;
        }

        int hashLen = hashLength;
        int lenIndices = Integer.BYTES;

        int loopLen = 512;

        // use X, Y as swap container for this algo to avoid 512 round memory
        // alloc and copy.
        FullStepRow[] Y = new FullStepRow[1 << k];

        for (int loopIdx = 0; loopIdx < 9; loopIdx++, loopLen >>= 1) {

            for (int i = 0; i < loopLen / 2; i++) {

                if (!hasCollision(X[i * 2], X[i * 2 + 1], collisionByteLength)) {
                    System.out.println("No collision");
                    return false;
                }

                if (indicesBefore(X[i * 2 + 1], X[i * 2], hashLen, lenIndices)) {
                    System.out.println("Incorrect order");
                    return false;
                }
                if (!distinctIndices(X[i * 2 + 1], X[i * 2], hashLen, lenIndices)) {
                    System.out.println("DUp order");
                    return false;
                }

                // Check order of X[i] and X[i+1] in because indices before is
                // called in the constructor
                // Xc.add(new FullStepRow(finalFullWidth, X[i], X[i + 1],
                // hashLen, lenIndices, collisionByteLength));
                Y[i] =
                        new FullStepRow(
                                finalFullWidth,
                                X[i * 2],
                                X[i * 2 + 1],
                                hashLen,
                                lenIndices,
                                collisionByteLength);
            }

            hashLen -= collisionByteLength;
            lenIndices *= 2;

            // swap X, Y
            FullStepRow[] swap = X;
            X = Y;
            Y = swap;
        }

        return X[0].isZero(hashLen);
    }

    /**
     * Helper classes and methods used by isValidSolution method. These are also mostly based off from aion java kernel,
     * see ByteUtil.java, EquiValidator.java, EquiUtils.java, and Equihash.java for more detail (https://github.com/aionnetwork/aion).
     */

    private int[] getIndicesFromMinimal(byte[] minimal, int cBitLen) {
        if (minimal == null) {
            throw new NullPointerException("null minimal bytes");
        }

        int lenIndices = 8 * Integer.BYTES * minimal.length / (cBitLen + 1);
        int bytePad = Integer.BYTES - ((cBitLen + 1) + 7) / 8;

        byte[] arr = new byte[lenIndices];
        extendArray(minimal, arr, cBitLen + 1, bytePad);

        return bytesToInts(arr, true);
    }

    private static Blake2b.Param getParam(int n, int k){
        Blake2b.Param param = new Blake2b.Param();
        byte[] personalization =
                Helpers.merge("AION0PoW".getBytes(), Helpers.merge(intToBytesLE(n), intToBytesLE(k)));
        param.setPersonal(personalization);
        param.setDigestLength((512/n) * (n + 7) / 8);
        return param;
    }

    /**
     * Determines if the hashes of A and B have collisions on length l
     *
     * @param a StepRow A
     * @param b StepRow B
     * @param l Length of bytes to compare
     * @return False if no collision in hashes a,b up to l, else true.
     * @throws NullPointerException when given null input
     */
    private boolean hasCollision(FullStepRow a, FullStepRow b, int l) {
        if (a == null || b == null) {
            throw new NullPointerException("null StepRow passed");
        }

        for (int j = 0; j < l; j++) {
            if (a.getHash()[j] != b.getHash()[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare indices and ensure the intersection of hashes is empty
     *
     * @param a StepRow a
     * @param b StepRow b
     * @param len Number of elements to compare
     * @param lenIndices Number of indices to compare
     * @return true if distinct; false otherwise
     * @throws NullPointerException when given null input
     */
    private boolean distinctIndices(FullStepRow a, FullStepRow b, int len, int lenIndices) {
        if (a == null || b == null) {
            throw new NullPointerException("null FullStepRow passed");
        }

        for (int i = 0; i < lenIndices; i = i + Integer.BYTES) {
            for (int j = 0; j < lenIndices; j = j + Integer.BYTES) {
                if (Arrays.compare(
                        Arrays.copyOfRange(a.getHash(), len + i, len + i + Integer.BYTES),
                        Arrays.copyOfRange(b.getHash(), len + j, len + j + Integer.BYTES))
                        == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // conversion helpers
    private static byte[] intToBytesLE(int val) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.putInt(val).array();
    }

    private static int[] bytesToInts(byte[] arr, boolean bigEndian) {
        int[] ret = new int[arr.length / 4];
        bytesToInts(arr, ret, bigEndian);
        return ret;
    }

    private static void bytesToInts(byte[] b, int[] arr, boolean bigEndian) {
        if (!bigEndian) {
            int off = 0;
            for (int i = 0; i < arr.length; i++) {
                int ii = b[off++] & 0x000000FF;
                ii |= (b[off++] << 8) & 0x0000FF00;
                ii |= (b[off++] << 16) & 0x00FF0000;
                ii |= (b[off++] << 24);
                arr[i] = ii;
            }
        } else {
            int off = 0;
            for (int i = 0; i < arr.length; i++) {
                int ii = b[off++] << 24;
                ii |= (b[off++] << 16) & 0x00FF0000;
                ii |= (b[off++] << 8) & 0x0000FF00;
                ii |= b[off++] & 0x000000FF;
                arr[i] = ii;
            }
        }
    }

    /**
     * A simplified extend array method using default outLen and inLen parameters. Expand an array
     * from compressed format into hash-append value format.
     *
     * <p>More precise extendArray method, allows for better specification of in and outLen
     *
     * @throws NullPointerException when given null input
     */
    private static void extendArray(byte[] in, byte[] out, int bitLen, int bytePad) {

        if (in == null) throw new NullPointerException("null input array");
        else if (out == null) throw new NullPointerException("null output array");

        int outWidth = (bitLen + 7) / 8 + bytePad;
        int bitLenMask = (1 << bitLen) - 1;
        int accBits = 0;
        int accValue = 0;

        int j = 0;
        for (byte anIn : in) {

            accValue = (accValue << 8) | (anIn & 0xff);
            accBits += 8;

            if (accBits >= bitLen) {
                accBits -= bitLen;

                for (int x = bytePad; x < outWidth; x++) {

                    out[j + x] =
                            (byte)
                                    ((
                                            // Big-endian
                                            accValue >>> (accBits + (8 * (outWidth - x - 1))))
                                            & (
                                            // Apply bit_len_mask across byte boundaries
                                            (bitLenMask >>> (8 * (outWidth - x - 1))) & 0xFF));
                }
                j += outWidth;
            }
        }
    }

    private static void extendArray(byte[] in, int inLen, byte[] out, int bitLen, int bytePad) {

        if (in == null) throw new NullPointerException("null input array");
        else if (out == null) throw new NullPointerException("null output array");

        int outWidth = (bitLen + 7) / 8 + bytePad;
        int bitLenMask = (1 << bitLen) - 1;
        int accBits = 0;
        int accValue = 0;

        int j = 0;
        for (int i = 0; i < inLen; i++) {
            accValue = (accValue << 8) | (in[i] & 0xff);
            accBits += 8;

            if (accBits >= bitLen) {
                accBits -= bitLen;

                for (int x = bytePad; x < outWidth; x++) {

                    out[j + x] =
                            (byte)
                                    ((
                                            // Big-endian
                                            accValue >>> (accBits + (8 * (outWidth - x - 1))))
                                            & (
                                            // Apply bit_len_mask across byte boundaries
                                            (bitLenMask >>> (8 * (outWidth - x - 1))) & 0xFF));
                }
                j += outWidth;
            }
        }
    }

    /**
     * Compare len bytes up to lenIndicies of a and b,
     *
     * @param a StepRow a
     * @param b StepRow b
     * @param len Starting position in hashes
     * @param lenIndices Length of indices
     * @return True if a > b, else false
     * @throws NullPointerException when given null input
     */
    private static boolean indicesBefore(FullStepRow a, FullStepRow b, int len, int lenIndices) {
        if (a == null || b == null)
            throw new NullPointerException("null StepRow passed for comparison");

        byte[] hashA = a.getHash();
        byte[] hashB = b.getHash();

        if (hashA == null) throw new NullPointerException("null hash within StepRow a");
        else if (hashB == null) throw new NullPointerException("null hash within StepRow b");

        int i = 0;
        while ((hashA[i + len] & 0xff) == (hashB[i + len] & 0xff) && i < lenIndices) {
            i++;
        }

        return (hashA[i + len] & 0xff) <= (hashB[i + len] & 0xff);
    }

    /**
     * helper class for byte manipulation
     */

    private static class FullStepRow {
        private int width;
        private byte[] hash;

        private FullStepRow(int width, byte[] hashIn, int hInLen, int hLen, int cBitLen, int index)
                throws NullPointerException {
            if (hashIn == null) throw new NullPointerException("Null hashIn");

            this.width = width;
            this.hash = new byte[width];

            // Byte pad is 0 based on the equihash specification
            extendArray(hashIn, hInLen, this.hash, cBitLen, 0);


            byte[] indexBytes = ByteBuffer.allocate(4).putInt(index).array();
            System.arraycopy(indexBytes, 0, this.getHash(), hLen, indexBytes.length);
        }

        public FullStepRow(int width, FullStepRow a, FullStepRow b, int len, int lenIndices, int trim)
                throws NullPointerException {
            this.hash = new byte[width];
            System.arraycopy(a.getHash(), 0, this.getHash(), 0, a.width);

            // Value of a is checked in super()
            if (b == null) {
                throw new NullPointerException("null FullStepRow");
            }

            // Merge a and b
            for (int i = trim; i < len; i++) {
                this.getHash()[i - trim] = (byte) (a.getHash()[i] ^ b.getHash()[i]);
            }

            if (indicesBefore(a, b, len, lenIndices)) {
                System.arraycopy(a.getHash(), len, this.getHash(), len - trim, lenIndices);
                System.arraycopy(b.getHash(), len, this.getHash(), len - trim + lenIndices, lenIndices);
            } else {
                System.arraycopy(b.getHash(), len, this.getHash(), len - trim, lenIndices);
                System.arraycopy(a.getHash(), len, this.getHash(), len - trim + lenIndices, lenIndices);
            }
        }

        public byte[] getHash() {
            return hash;
        }

        public boolean isZero(int len) {
            for (int i = 0; i < len; i++) {
                if (this.getHash()[i] != 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
