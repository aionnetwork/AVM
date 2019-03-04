package org.aion.avm.tooling.abi;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.aion.avm.core.dappreading.JarBuilder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class ABICompiler {

    private static final int MAX_CLASS_BYTES = 1024 * 1024;
    private static final float VERSION_NUMBER = 0.0F;

    private String mainClassName;
    private byte[] mainClassBytes;
    private byte[] outputJarFile;
    private List<String> callables = new ArrayList<>();
    private Map<String, byte[]> classMap = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid parameters!");
            usage();
            System.exit(1);
        }

        String jarPath = args[0];
        ABICompiler compiler = new ABICompiler();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(jarPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        compiler.compile(fileInputStream);

        List<String> callables = compiler.getCallables();
        System.out.println(VERSION_NUMBER);
        for (String s : callables) System.out.println(s);

        try {
            DataOutputStream dout =
                    new DataOutputStream(new FileOutputStream("outputJar" + ".jar"));
            dout.write(compiler.getJarFileBytes());
            dout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void usage() {
        System.out.println("Usage: ABICompiler <DApp jar path>");
    }

    public void compile(byte[] jarBytes) {
        compile(new ByteArrayInputStream(jarBytes));
    }

    public void compile(InputStream byteReader) {
        try {
            safeLoadFromBytes(byteReader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClassReader reader = new ClassReader(mainClassBytes);
        ClassWriter classWriter = new ClassWriter(0);
        ABICompilerClassVisitor classVisitor = new ABICompilerClassVisitor(classWriter) {};
        try {
            reader.accept(classVisitor, 0);
        } catch (Exception e) {
            throw e;
        }
        callables = classVisitor.getCallableSignatures();
        mainClassBytes = classWriter.toByteArray();
        outputJarFile =
                JarBuilder.buildJarForExplicitClassNamesAndBytecode(
                        mainClassName, mainClassBytes, classMap);

//        DataOutputStream dout = null;
//        try {
//            dout = new DataOutputStream(
//                new FileOutputStream(getMainClassName() + ".class"));
//            dout.write(getMainClassBytes());
//            dout.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void safeLoadFromBytes(InputStream byteReader) throws Exception {
        classMap = new HashMap<>();
        mainClassName = null;

        try (JarInputStream jarReader = new JarInputStream(byteReader, true)) {

            Manifest manifest = jarReader.getManifest();
            if (null != manifest) {
                Attributes mainAttributes = manifest.getMainAttributes();
                if (null != mainAttributes) {
                    mainClassName = mainAttributes.getValue(Attributes.Name.MAIN_CLASS);
                }
            }

            JarEntry entry;
            byte[] tempReadingBuffer = new byte[MAX_CLASS_BYTES];
            while (null != (entry = jarReader.getNextJarEntry())) {
                String name = entry.getName();
                // We already ready the manifest so now we only want to work on classes and not any
                // of the
                // special modularity ones.
                if (name.endsWith(".class")
                        && !name.equals("package-info.class")
                        && !name.equals("module-info.class")) {
                    // replaceAll gives us the regex so we use "$".
                    String internalClassName = name.replaceAll(".class$", "");
                    String qualifiedClassName =
                            internalNameToFulllyQualifiedName(internalClassName);
                    int readSize =
                            jarReader.readNBytes(tempReadingBuffer, 0, tempReadingBuffer.length);
                    // Now, copy this part of the array as a correctly-sized classBytes.
                    byte[] classBytes = new byte[readSize];
                    if (0 != jarReader.available()) {
                        // This entry is too big.
                        throw new Exception("Class file too big: " + name);
                    }
                    System.arraycopy(tempReadingBuffer, 0, classBytes, 0, readSize);
                    if (qualifiedClassName.equals(mainClassName)) mainClassBytes = classBytes;
                    else classMap.put(qualifiedClassName, classBytes);
                }
            }
        }
    }

    private static String internalNameToFulllyQualifiedName(String internalName) {
        return internalName.replaceAll("/", ".");
    }

    public List<String> getCallables() {
        return callables;
    }

    public byte[] getMainClassBytes() {
        return mainClassBytes;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public Map<String, byte[]> getClassMap() {
        return classMap;
    }

    public static float getVersionNumber() {
        return VERSION_NUMBER;
    }

    public byte[] getJarFileBytes() {
        return outputJarFile;
    }
}
