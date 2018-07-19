package org.aion.avm.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.aion.avm.core.dappreading.LoadedJar;


/**
 * Represents the DApp code once it has been validated and transformed but before it has been deployed and stored.
 * All fields are public since this object is effectively an immutable struct.
 * See issue-134 for more details on this design.
 */
public class TransformedDappModule {
    // Note that we currently limit the size of an in-memory JAR to 1 MiB.
    private static final int MAX_JAR_BYTES = 1024 * 1024;

    public static TransformedDappModule readFromJar(byte[] jar) throws IOException {
        LoadedJar loadedJar = LoadedJar.fromBytes(jar);
        Map<String, byte[]> classes = loadedJar.classBytesByQualifiedNames;
        String mainClass = loadedJar.mainClassName;
        return new TransformedDappModule(classes, mainClass);
    }

    public static TransformedDappModule fromTransformedClasses(Map<String, byte[]> classes, String mainClass)  {
        return new TransformedDappModule(classes, mainClass);
    }


    public final Map<String, byte[]> classes;
    public final String mainClass;

    private TransformedDappModule(Map<String, byte[]> classes, String mainClass) {
        this.classes = classes;
        this.mainClass = mainClass;
    }

    /**
     * Create the in-memory JAR containing all the classes in this module.
     */
    public byte[] createJar(byte[] address) throws IOException {
        // manifest
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, this.mainClass);

        // Create a temporary memory location for this JAR.
        ByteArrayOutputStream tempJarStream = new ByteArrayOutputStream(MAX_JAR_BYTES);

        // create the jar file
        try (JarOutputStream target = new JarOutputStream(tempJarStream, manifest)) {
            // add the classes
            for (String clazz : this.classes.keySet()) {
                JarEntry entry = new JarEntry(clazz.replace('.', '/') + ".class");
                target.putNextEntry(entry);
                target.write(this.classes.get(clazz));
                target.closeEntry();
            }
        }
        return tempJarStream.toByteArray();
    }
}
