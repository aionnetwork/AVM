package org.aion.avm.core.crypto;

import org.spongycastle.util.encoders.Hex;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtil {

    /**
     * Converts string hex representation to data bytes Accepts following hex: - with or without 0x
     * prefix - with no leading 0, like 0xabc -> 0x0abc
     *
     * @param data String like '0xa5e..' or just 'a5e..'
     * @return decoded bytes array
     */
    public static byte[] hexStringToBytes(String data) {
        if (data == null) {
            return new byte[0];
        }
        if (data.startsWith("0x")) {
            data = data.substring(2);
        }
        if (data.length() % 2 == 1) {
            data = "0" + data;
        }
        return Hex.decode(data);
    }

    /**
     * Convert a byte-array into a hex String.
     *
     * @param data - byte-array to convert to a hex-string
     * @return hex representation of the data.
     */
    public static String toHexString(byte[] data) {
        return Hex.toHexString(data);
    }

    /**
     * Sign a byte array of data given the private key.
     */
    public static byte[] signEdDSA(byte[] data, byte[] privateKey) throws InvalidKeySpecException, InvalidKeyException, SignatureException {
        return Ed25519Key.sign(data, privateKey);
    }

    /**
     * Verify a message with given signature and public key.
     */
    public static boolean verifyEdDSA(byte[] data, byte[] signature, byte[] publicKey) throws InvalidKeySpecException, InvalidKeyException, SignatureException {
        return Ed25519Key.verify(data, signature, publicKey);
    }
}
