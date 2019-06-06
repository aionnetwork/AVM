package org.aion.avm.tooling.deploy;

import org.aion.avm.core.dappreading.JarBuilder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static org.aion.avm.core.util.Helpers.internalNameToFulllyQualifiedName;

public class JarOptimizer {

    private static final int MAX_CLASS_BYTES = 1024 * 1024;

    private static boolean loggingEnabled = false;

    private boolean preserveDebugInfo;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Input the path to the jar file.");
            System.exit(0);
        }

        //remove debug information when the tool is run
        JarOptimizer optimizer = new JarOptimizer(false);

        try (FileInputStream fileInputStream = new FileInputStream(args[0])) {
            byte[] optimizedJarBytes = optimizer.optimize(fileInputStream.readAllBytes());

            int pathLength = args[0].lastIndexOf("/") + 1;
            String outputJarName = args[0].substring(0, pathLength) + "minimized_" + args[0].substring(pathLength);
            optimizer.writeOptimizedJar(outputJarName, optimizedJarBytes);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public JarOptimizer(boolean preserveDebugInfo) {
        this.preserveDebugInfo = preserveDebugInfo;
    }

    public byte[] optimize(byte[] jarBytes) {
        Map<String, byte[]> classMap;
        Set<String> visitedClasses = new HashSet<>();

        try {
            JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true);
            String mainClassName = extractMainClassName(jarReader);
            classMap = extractClasses(jarReader);

            traverse(mainClassName, visitedClasses, classMap);

            return buildOptimizedJar(visitedClasses, classMap, mainClassName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private String extractMainClassName(JarInputStream jarReader) {

        Manifest manifest = jarReader.getManifest();
        if (null != manifest && manifest.getMainAttributes() != null) {
            return manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        } else {
            throw new RuntimeException("Manifest file required");
        }
    }

    private Map<String, byte[]> extractClasses(JarInputStream jarReader) throws IOException {

        Map<String, byte[]> classMap = new HashMap<>();
        byte[] tempReadingBuffer = new byte[MAX_CLASS_BYTES];

        JarEntry entry;
        while (null != (entry = jarReader.getNextJarEntry())) {
            String name = entry.getName();

            if (name.endsWith(".class")
                    && !name.equals("package-info.class")
                    && !name.equals("module-info.class")) {

                String internalClassName = name.replaceAll(".class$", "");
                String qualifiedClassName = internalNameToFulllyQualifiedName(internalClassName);
                int readSize = jarReader.readNBytes(tempReadingBuffer, 0, tempReadingBuffer.length);

                if (0 != jarReader.available()) {
                    throw new RuntimeException("Class file too big: " + name);
                }

                byte[] classBytes = new byte[readSize];
                System.arraycopy(tempReadingBuffer, 0, classBytes, 0, readSize);
                classMap.put(qualifiedClassName, classBytes);
            }
        }
        return classMap;
    }


    private void traverse(String className, Set<String> visitedClasses, Map<String, byte[]> classMap) {
        visitedClasses.add(className);
        Set<String> referencedClasses = visitClass(className, classMap);

        if (loggingEnabled) {
            System.out.println("visited " + className);
            for (String c : referencedClasses) {
                System.out.println("  referenced " + c);
            }
        }

        for (String c : referencedClasses) {
            if (classMap.containsKey(c) && !visitedClasses.contains(c)) {
                traverse(c, visitedClasses, classMap);
            }
        }
    }

    private Set<String> visitClass(String className, Map<String, byte[]> classMap) {

        DependencyCollector dependencyCollector = new DependencyCollector();

        ClassReader reader = new ClassReader(classMap.get(className));

        SignatureVisitor signatureVisitor = new SignatureDependencyVisitor(dependencyCollector);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ClassDependencyVisitor(signatureVisitor, dependencyCollector, writer, preserveDebugInfo);
        reader.accept(classVisitor, 0);

        classMap.put(className, writer.toByteArray());
        return dependencyCollector.getDependencies();
    }

    private byte[] buildOptimizedJar(Set<String> visitedClasses, Map<String, byte[]> classMap, String mainClassName) {
        if (loggingEnabled) {
            System.out.println("Need to remove " + (classMap.entrySet().size() - visitedClasses.size()) + " out of " + classMap.entrySet().size() + " classes.");
            classMap.forEach((key, value) -> {
                if (!visitedClasses.contains(key)) {
                    System.out.println(" - " + key);
                }
            });
        }

        classMap.entrySet().removeIf(e -> !visitedClasses.contains(e.getKey()));
        assertTrue(classMap.entrySet().size() == visitedClasses.size());

        byte[] mainClassBytes = classMap.get(mainClassName);
        classMap.remove(mainClassName, mainClassBytes);
        return JarBuilder.buildJarForExplicitClassNamesAndBytecode(mainClassName, mainClassBytes, classMap);
    }

    private void writeOptimizedJar(String jarName, byte[] jarBytes) {
        try {
            DataOutputStream dout = new DataOutputStream(new FileOutputStream(jarName));
            dout.write(jarBytes);
            dout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Successfully created jar. \n" + jarName);
    }

    private static void assertTrue(boolean flag) {
        // We use a private helper to manage the assertions since the JDK default disables them.
        if (!flag) {
            throw new AssertionError("Case must be true");
        }
    }
}

