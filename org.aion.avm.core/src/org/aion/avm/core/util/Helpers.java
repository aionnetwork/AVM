package org.aion.avm.core.util;

import java.io.*;
import java.security.SecureRandom;


/**
 * Common utilities we often want to use in various tests (either temporarily or permanently).
 * These are kept here just to avoid duplication.
 */
public class Helpers {
    /**
     * Writes the given bytes to the file at the given path.
     * This is effective for dumping re-written bytecode to file for offline analysis.
     *
     * @param bytes The bytes to write.
     * @param file  The path where the file should be written.
     */
    public static void writeBytesToFile(byte[] bytes, String file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        } catch (IOException e) {
            // This is for tests so we aren't expecting the failure.
            Assert.unexpected(e);
        }
    }

    /**
     * Reads file as a byte array.
     *
     * @param path
     * @return
     */
    public static byte[] readFileToBytes(String path) {
        File f = new File(path);
        byte[] b = new byte[(int) f.length()];

        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            in.readFully(b);
        } catch (IOException e) {
            Assert.unexpected(e);
        }

        return b;
    }

    /**
     * A helper which will attempt to load the given resource path as bytes.
     * Any failure in the load is considered fatal.
     * 
     * @param resourcePath The path to this resource, within the parent class loader.
     * @return The bytes
     */
    public static byte[] loadRequiredResourceAsBytes(String resourcePath) {
        InputStream stream = Helpers.class.getClassLoader().getResourceAsStream(resourcePath);
        byte[] raw = null;
        try {
            raw = stream.readAllBytes();
        } catch (IOException e) {
            Assert.unexpected(e);
        }
        return raw;
    }

    private static SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate random byte array of the specified length.
     *
     * @param n
     * @return
     */
    public static byte[] randomBytes(int n) {
        byte[] bytes = new byte[n];
        secureRandom.nextBytes(bytes);

        return bytes;
    }

    /**
     * Converts a fully qualified class name into it's JVM internal form.
     *
     * @param fullyQualifiedName
     * @return
     */
    public static String fulllyQualifiedNameToInternalName(String fullyQualifiedName) {
        return fullyQualifiedName.replaceAll("\\.", "/");
    }

    /**
     * Converts a JVM internal class name into a fully qualified name.
     *
     * @param internalName
     * @return
     */
    public static String internalNameToFulllyQualifiedName(String internalName) {
        return internalName.replaceAll("/", ".");
    }
}
