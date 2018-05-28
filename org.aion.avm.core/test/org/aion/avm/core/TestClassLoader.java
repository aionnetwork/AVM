package org.aion.avm.core;

import java.io.IOException;
import java.io.InputStream;

import org.aion.avm.core.util.Assert;


/**
 * This used to be a class loader for tests but now AvmClassLoader handles that and this only contains a helper which will eventually be moved.
 */
public class TestClassLoader {
    /**
     * A helper which will attempt to load the given resource path as bytes.
     * Any failure in the load is considered fatal.
     * 
     * @param resourcePath The path to this resource, within the parent class loader.
     * @return The bytes
     */
    public static byte[] loadRequiredResourceAsBytes(String resourcePath) {
        InputStream stream = TestClassLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        byte[] raw = null;
        try {
            raw = stream.readAllBytes();
        } catch (IOException e) {
            Assert.unexpected(e);
        }
        return raw;
    }
}
