package org.aion.avm.core;

import java.io.FileOutputStream;
import java.io.IOException;

import org.aion.avm.core.util.Assert;


/**
 * Common utilities we often want to use in various tests (either temporarily or permanently).
 * These are kept here just to avoid duplication.
 */
public class TestHelpers {
    /**
     * Writes the given bytes to the file at the given path.
     * This is effective for dumping re-written bytecode to file for offline analysis.
     * 
     * @param bytes The bytes to write.
     * @param file The path where the file should be written.
     */
    public static void writeBytesToFile(byte[] bytes, String file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        } catch (IOException e) {
            // This is for tests so we aren't expecting the failure.
            Assert.unexpected(e);
        }
    }
}
