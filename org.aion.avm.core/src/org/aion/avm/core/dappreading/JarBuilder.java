package org.aion.avm.core.dappreading;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * A utility to build in-memory JAR representations for tests and examples.
 * 
 * This is kept purely private and only the top-level factory method operates on the instances since they are stateful in ways which
 * would be complicated to communicate (streams are closed when reading the bytes, for example).
 */
public class JarBuilder {
    /**
     * Creates the in-memory representation of a JAR with the given main class and other classes.
     * 
     * @param mainClass The main class to include and list in manifest (can be null).
     * @param otherClasses The other classes to include (main is already included).
     * @return The bytes representing this JAR.
     */
    public static byte[] buildJarForMainAndClasses(Class<?> mainClass, Class<?> ...otherClasses) {
        JarBuilder builder = new JarBuilder(mainClass);
        for (Class<?> clazz : otherClasses) {
            builder.addClassAndInners(clazz);
        }
        return builder.toBytes();
    }


    private final ByteArrayOutputStream byteStream;
    private final JarOutputStream jarStream;
    private final Set<String> entriesInJar;

    private JarBuilder(Class<?> mainClass) {
        // Build the manifest.
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        // Note that the manifest version seems to be required.  If it isn't specified, we don't see the main class.
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        // The main class is technically optional (we mostly use a null main for testing cases).
        if (null != mainClass) {
            mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClass.getName());
        }
        
        // Create the underlying byte stream (hold onto this for serialization).
        this.byteStream = new ByteArrayOutputStream();
        JarOutputStream stream = null;
        try {
            // We always write the manifest into the high-level JAR stream.
            stream = new JarOutputStream(this.byteStream, manifest);
        } catch (IOException e) {
            // We are using a byte array so this can't happen.
            throw RuntimeAssertionError.unexpected(e);
        }
        this.jarStream = stream;
        this.entriesInJar = new HashSet<>();
        
        // Finally, add this class.
        if (null != mainClass) {
            addClassAndInners(mainClass);
        }
    }

    /**
     * Loads the given class, any declared classes (named inner classes), and any anonymous inner classes.
     * 
     * @param clazz The class to load.
     * @return this, for easy chaining.
     */
    public JarBuilder addClassAndInners(Class<?> clazz) {
        try {
            // Load everything related to this class.
            loadClassAndAnonymous(clazz);
            // Now, include any declared classes.
            for (Class<?> one : clazz.getDeclaredClasses()) {
                addClassAndInners(one);
            }
        } catch (IOException e) {
            // We are serializing to a byte array so this is unexpected.
            throw RuntimeAssertionError.unexpected(e);
        }
        return this;
    }

    private void loadClassAndAnonymous(Class<?> clazz) throws IOException {
        // Start with the fully-qualified class name, since we use that for addressing it.
        String className = clazz.getName();
        byte[] bytes = Helpers.loadRequiredResourceAsBytes(Helpers.fulllyQualifiedNameToInternalName(className) + ".class");
        RuntimeAssertionError.assertTrue(null != bytes);
        saveClassToStream(className, bytes);
        
        // Load any inner classes which might exist (these are just decimal suffixes, starting at 1.
        int i = 1;
        String innerName = className + "$" + Integer.toString(i);
        byte[] innerBytes = Helpers.loadRequiredResourceAsBytes(Helpers.fulllyQualifiedNameToInternalName(innerName) + ".class");
        while (null != innerBytes) {
            saveClassToStream(innerName, innerBytes);
            
            i += 1;
            innerName = className + "$" + Integer.toString(i);
            innerBytes = Helpers.loadRequiredResourceAsBytes(Helpers.fulllyQualifiedNameToInternalName(innerName) + ".class");
        }
    }

    private void saveClassToStream(String qualifiedClassName, byte[] bytes) throws IOException {
        // Convert this fully-qualified name into an internal name, since that is the serialized name it needs.
        String internalName = Helpers.fulllyQualifiedNameToInternalName(qualifiedClassName);
        RuntimeAssertionError.assertTrue(!this.entriesInJar.contains(internalName));
        JarEntry entry = new JarEntry(internalName + ".class");
        this.jarStream.putNextEntry(entry);
        this.jarStream.write(bytes);
        this.jarStream.closeEntry();
        this.entriesInJar.add(internalName);
    }

    public byte[] toBytes() {
        try {
            this.jarStream.finish();
            this.jarStream.close();
            this.byteStream.close();
        } catch (IOException e) {
            // We are using a byte array so this can't happen.
            throw RuntimeAssertionError.unexpected(e);
        }
        return this.byteStream.toByteArray();
    }
}
