package org.aion.avm.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static byte[] sha256(byte[] msg) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(msg);

            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
