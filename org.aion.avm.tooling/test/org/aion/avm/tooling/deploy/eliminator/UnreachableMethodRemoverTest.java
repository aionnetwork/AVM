package org.aion.avm.tooling.deploy.eliminator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.jar.JarInputStream;

import org.aion.avm.tooling.deploy.eliminator.resources.*;
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

        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(optimizedJar), true);
        Map<String, byte[]> classMap = Utilities.extractClasses(jarReader, Utilities.NameStyle.SLASH_NAME);

        assertNotNull(optimizedJar);
        assertEquals(7, classMap.size());



        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(ClassGname, classMap);
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

        assertEquals(1, methodInfoMapA.size());
        assertEquals(1, methodInfoMapB.size());
        assertEquals(1, methodInfoMapC.size());
        assertEquals(3, methodInfoMapD.size());
        assertEquals(1, methodInfoMapE.size());
        assertEquals(9, methodInfoMapF.size());
        assertEquals(9, methodInfoMapG.size());
    }

    @Test
    public void testInvokeStaticMethodRemoval() throws Exception {
        String ClassInvokeStaticEntryName = getInternalNameForClass(InvokeStaticEntry.class);

        byte[] jar = TestUtil.serializeClassesAsJar(InvokeStaticEntry.class, ClassH.class, ClassI.class);
        byte[] optimizedJar = UnreachableMethodRemover.optimize(jar);
        assertNotNull(optimizedJar);

        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(optimizedJar), true);
        Map<String, byte[]> classMap = Utilities.extractClasses(jarReader, Utilities.NameStyle.SLASH_NAME);

        assertNotNull(optimizedJar);
        assertEquals(3, classMap.size());

        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(ClassInvokeStaticEntryName, classMap);
        assertEquals(21, classInfoMap.size());
        ClassInfo classInfoH = classInfoMap.get(getInternalNameForClass(ClassH.class));
        ClassInfo classInfoI = classInfoMap.get(getInternalNameForClass(ClassI.class));

        assertNotNull(classInfoI);
        assertNotNull(classInfoH);

        assertEquals(1, classInfoI.getMethodMap().size());
        assertEquals(0, classInfoH.getMethodMap().size());
    }

    private static String getInternalNameForClass(Class<?> clazz) {
        return Utilities.fulllyQualifiedNameToInternalName(clazz.getName());
    }
}
