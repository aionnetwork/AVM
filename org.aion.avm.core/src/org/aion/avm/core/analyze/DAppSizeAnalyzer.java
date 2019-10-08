package org.aion.avm.core.analyze;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.aion.avm.NameStyle;
import org.aion.avm.core.util.Helpers;

public class DAppSizeAnalyzer {
    private static final int MAX_CLASS_BYTES = 1024 * 1024;
    private static int fatals;

    public static boolean analyze(byte[] jarBytes) {
        boolean success = true;
        try {
            synchronized(ConstantPoolBuilder.lock) {
                ConstantPoolBuilder.addCount(ConstantPoolBuilder.LIMIT_jarSize, ConstantPoolBuilder.jarSize, jarBytes.length);
            }
            JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true);
            Map<String, byte[]> classMap = extractClasses(jarReader, NameStyle.SLASH_NAME);
            for (Map.Entry<String, byte[]> classEntry : classMap.entrySet()) {
                byte[] classFile = classEntry.getValue();
                synchronized(ConstantPoolBuilder.lock) {
                    ConstantPoolBuilder.addCount(ConstantPoolBuilder.LIMIT_classSize, ConstantPoolBuilder.classSize, classFile.length);
                }
                ConstantPoolBuilder.ClassConstantSizeInfo result = ConstantPoolBuilder.getConstantPoolInfo(classFile);
                if (null == result) {
                    success = false;
                }
            }
        } catch (Throwable e) {
            fatals += 1;
            success = false;
        }
        return success;
    }

    public static void dumpData() {
        System.out.println("REPORT ***** " + (ConstantPoolBuilder.classCount - 1) + " classes (" + ConstantPoolBuilder.failCount + " errors)");
        if (fatals > 0) {
            System.out.println("\tFATALS: " + fatals);
        }
        printSequence("Constants ", ConstantPoolBuilder.LIMIT_constantCount, ConstantPoolBuilder.constantCount);
        printSequence("Methods   ", ConstantPoolBuilder.LIMIT_methodCount, ConstantPoolBuilder.methodCount);
        printSequence("Interfaces", ConstantPoolBuilder.LIMIT_interfaceCount, ConstantPoolBuilder.interfaceCount);
        printSequence("Fields    ", ConstantPoolBuilder.LIMIT_fieldCount, ConstantPoolBuilder.fieldCount);
        printSequence("CodeSize  ", ConstantPoolBuilder.LIMIT_codeSize, ConstantPoolBuilder.codeSize);
        printSequence("E.Tables  ", ConstantPoolBuilder.LIMIT_exceptionCount, ConstantPoolBuilder.exceptionCount);
        printSequence("ClassSize ", ConstantPoolBuilder.LIMIT_classSize, ConstantPoolBuilder.classSize);
        printSequence("JAR Size  ", ConstantPoolBuilder.LIMIT_jarSize, ConstantPoolBuilder.jarSize);
    }

    private static void printSequence(String name, int[] limits, int[] counts) {
        System.out.print("\t" + name);
        for (int limit : limits) {
            System.out.print("\t<=" + limit);
        }
        System.out.println("\tOVER\tMAX");
        System.out.print("\t");
        for (int i = 0; i < name.length(); ++i) {
            System.out.print(" ");
        }
        for (int count : counts) {
            System.out.print("\t" + count);
        }
        System.out.println();
    }

    private static Map<String, byte[]> extractClasses(JarInputStream jarReader, NameStyle nameStyle) throws IOException {

        Map<String, byte[]> classMap = new HashMap<>();
        byte[] tempReadingBuffer = new byte[MAX_CLASS_BYTES];

        JarEntry entry;
        while (null != (entry = jarReader.getNextJarEntry())) {
            String name = entry.getName();

            if (name.endsWith(".class")
                    && !name.equals("package-info.class")
                    && !name.equals("module-info.class")) {

                String internalClassName = name.replaceAll(".class$", "");
                if (nameStyle.equals(NameStyle.DOT_NAME)) {
                    internalClassName = Helpers.internalNameToFulllyQualifiedName(internalClassName);
                }
                int readSize = jarReader.readNBytes(tempReadingBuffer, 0, tempReadingBuffer.length);

                if (0 != jarReader.available()) {
                    throw new RuntimeException("Class file too big: " + name);
                }

                byte[] classBytes = new byte[readSize];
                System.arraycopy(tempReadingBuffer, 0, classBytes, 0, readSize);
                classMap.put(internalClassName, classBytes);
            }
        }
        return classMap;
    }
}
