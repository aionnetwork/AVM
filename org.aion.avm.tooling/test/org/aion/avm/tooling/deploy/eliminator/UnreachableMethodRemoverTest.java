package org.aion.avm.tooling.deploy.eliminator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.aion.avm.tooling.deploy.eliminator.ClassInfo;
import org.aion.avm.tooling.deploy.eliminator.MethodInfo;
import org.aion.avm.tooling.deploy.eliminator.MethodReachabilityDetector;
import org.aion.avm.tooling.deploy.eliminator.UnreachableMethodRemover;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassD;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassE;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassF;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassG;
import org.aion.avm.tooling.deploy.eliminator.resources.InterfaceA;
import org.aion.avm.tooling.deploy.eliminator.resources.InterfaceB;
import org.aion.avm.tooling.deploy.eliminator.resources.InterfaceC;
import org.aion.avm.tooling.util.Utilities;
import org.junit.Test;

public class UnreachableMethodRemoverTest {
    private static String InterfaceAname = getInternalNameForClass(InterfaceA.class);
    private static String InterfaceBname = getInternalNameForClass(InterfaceB.class);
    private static String InterfaceCname = getInternalNameForClass(InterfaceC.class);
    private static String ClassDname = getInternalNameForClass(ClassD.class);
    private static String ClassEname = getInternalNameForClass(ClassE.class);
    private static String ClassFname = getInternalNameForClass(ClassF.class);
    private static String ClassGname = getInternalNameForClass(ClassG.class);

    @Test
    public void testMethodRemoval() throws Exception {

        byte[] jar = TestUtil.serializeClassesAsJar(ClassG.class, ClassF.class, ClassE.class, ClassD.class,
                InterfaceC.class, InterfaceB.class, InterfaceA.class);
        byte[] optimizedJar = UnreachableMethodRemover.optimize(jar);
        assertNotNull(optimizedJar);

        Map<String, byte[]> classMap = extractClasses(optimizedJar);

        assertNotNull(optimizedJar);
        assertEquals(7, classMap.size());



        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(ClassGname, turnDotsToSlashes(classMap));
        assertEquals(25, classInfoMap.size());
        ClassInfo classInfoA = classInfoMap.get(InterfaceAname);
        ClassInfo classInfoB = classInfoMap.get(InterfaceBname);
        ClassInfo classInfoC = classInfoMap.get(InterfaceCname);
        ClassInfo classInfoD = classInfoMap.get(ClassDname);
        ClassInfo classInfoE = classInfoMap.get(ClassEname);
        ClassInfo classInfoF = classInfoMap.get(ClassFname);
        ClassInfo classInfoG = classInfoMap.get(ClassGname);
        assertNotNull(classInfoA);
        assertNotNull(classInfoB);
        assertNotNull(classInfoC);
        assertNotNull(classInfoD);
        assertNotNull(classInfoE);
        assertNotNull(classInfoF);
        assertNotNull(classInfoG);

        Map<String, MethodInfo> methodInfoMapA = classInfoA.getMethodMap();
        Map<String, MethodInfo> methodInfoMapB = classInfoB.getMethodMap();
        Map<String, MethodInfo> methodInfoMapC = classInfoC.getMethodMap();
        Map<String, MethodInfo> methodInfoMapD = classInfoD.getMethodMap();
        Map<String, MethodInfo> methodInfoMapE = classInfoE.getMethodMap();
        Map<String, MethodInfo> methodInfoMapF = classInfoF.getMethodMap();
        Map<String, MethodInfo> methodInfoMapG = classInfoG.getMethodMap();

        assertEquals(0, methodInfoMapA.size());
        assertEquals(1, methodInfoMapB.size());
        assertEquals(1, methodInfoMapC.size());
        assertEquals(3, methodInfoMapD.size());
        assertEquals(1, methodInfoMapE.size());
        assertEquals(8, methodInfoMapF.size());
        assertEquals(8, methodInfoMapG.size());
    }


    private static Map<String, byte[]> extractClasses(byte[] jarBytes) throws IOException {
        Map<String, byte[]> classMap = new HashMap<>();

        try (JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true)) {
            byte[] tempReadingBuffer = new byte[1024 * 1024];
            JarEntry entry;
            while (null != (entry = jarReader.getNextJarEntry())) {
                String name = entry.getName();

                if (name.endsWith(".class")
                    && !name.equals("package-info.class")
                    && !name.equals("module-info.class")) {

                    String internalClassName = name.replaceAll(".class$", "");
                    String qualifiedClassName = Utilities.internalNameToFulllyQualifiedName(internalClassName);
                    int readSize = jarReader.readNBytes(tempReadingBuffer, 0, tempReadingBuffer.length);

                    if (0 != jarReader.available()) {
                        throw new RuntimeException("Class file too big: " + name);
                    }

                    byte[] classBytes = new byte[readSize];
                    System.arraycopy(tempReadingBuffer, 0, classBytes, 0, readSize);
                    classMap.put(qualifiedClassName, classBytes);
                }
            }
        }

        return classMap;
    }

    private static Map<String, byte[]> turnDotsToSlashes(Map<String, byte[]> inputClassMap) {
        Map<String, byte[]> outputClassMap = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : inputClassMap.entrySet()) {
            outputClassMap.put(Utilities.fulllyQualifiedNameToInternalName(entry.getKey()), entry.getValue());
        }
        return outputClassMap;
    }

    private static String getInternalNameForClass(Class<?> clazz) {
        return Utilities.fulllyQualifiedNameToInternalName(clazz.getName());
    }
}
