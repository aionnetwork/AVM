package org.aion.avm.tooling.util;

import java.io.IOException;
import java.io.InputStream;


/**
 * Generic utilities.
 */
public class Utilities {
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

    /**
     * A helper which will attempt to load the given resource path as bytes.
     * Returns null if the resource could not be found.
     *
     * @param resourcePath The path to this resource, within the parent class loader.
     * @return The resource as bytes, or null if not found.
     */
    public static byte[] loadRequiredResourceAsBytes(String resourcePath) {
        InputStream stream = Utilities.class.getClassLoader().getResourceAsStream(resourcePath);
        byte[] raw = null;
        if (null != stream) {
            try {
                raw = stream.readAllBytes();
            } catch (IOException e) {
                // We treat this as a fatal error, within this simple utility class.
                throw new AssertionError(e);
            }
        }
        return raw;
    }
}
