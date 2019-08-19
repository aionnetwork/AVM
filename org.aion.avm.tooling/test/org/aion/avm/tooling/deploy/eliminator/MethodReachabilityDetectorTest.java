package org.aion.avm.tooling.deploy.eliminator;

import org.aion.avm.tooling.deploy.eliminator.resources.*;
import org.aion.avm.tooling.util.Utilities;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class MethodReachabilityDetectorTest {

    private static String InterfaceAname = getInternalNameForClass(InterfaceA.class);
    private static String InterfaceBname = getInternalNameForClass(InterfaceB.class);
    private static String InterfaceCname = getInternalNameForClass(InterfaceC.class);
    private static String ClassDname = getInternalNameForClass(ClassD.class);
    private static String ClassEname = getInternalNameForClass(ClassE.class);
    private static String ClassFname = getInternalNameForClass(ClassF.class);
    private static String ClassGname = getInternalNameForClass(ClassG.class);
    private static String FakeMapUsername = getInternalNameForClass(FakeMapUser.class);

    @Test
    public void testMethodReachability() throws Exception {

        Map<String, byte[]> classMap = TestUtil.makeClassMap(ClassG.class, ClassF.class, ClassE.class, ClassD.class,
            InterfaceC.class, InterfaceB.class, InterfaceA.class);

        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(ClassGname, classMap);
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
        // interfaceA() is not reachable
        MethodInfo methodInfo = classInfoA.getMethodMap().get("interfaceA()C");
        assertNotNull(methodInfo);
        assertTrue(!methodInfo.isReachable);

        // Static method is reachable

        methodInfo = classInfoF.getMethodMap().get("classFStaticMethod()I");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        // interfaceB() is reachable in InterfaceB and all its children that override it

        methodInfo = classInfoB.getMethodMap().get("interfaceB()C");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = classInfoF.getMethodMap().get("interfaceB()C");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = classInfoG.getMethodMap().get("interfaceB()C");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        // interfaceC() is reachable in Class D and its children, but NOT InterfaceC itself

        methodInfo = classInfoC.getMethodMap().get("interfaceC()C");
        assertNotNull(methodInfo);
        assertFalse(methodInfo.isReachable);

        methodInfo = classInfoD.getMethodMap().get("interfaceC()C");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        // No such methodInfo should exist, because E doesn't override interfaceC
        methodInfo = classInfoE.getMethodMap().get("interfaceC()C");
        assertNull(methodInfo);

        methodInfo = classInfoF.getMethodMap().get("interfaceC()C");
        assertNull(methodInfo);

        methodInfo = classInfoG.getMethodMap().get("interfaceC()C");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        // ClassD::classD() is never
        methodInfo = classInfoD.getMethodMap().get("classD()C");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = classInfoF.getMethodMap().get("classD()C");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);
    }

    @Test
    public void testInvokeStaticMethodReachability() throws Exception {
        String ClassInvokeStaticEntryName = getInternalNameForClass(InvokeStaticEntry.class);
        String ClassIName = getInternalNameForClass(ClassI.class);

        Map<String, byte[]> classMap = TestUtil.makeClassMap(InvokeStaticEntry.class, ClassH.class, ClassI.class);
        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(ClassInvokeStaticEntryName, classMap);
        ClassInfo classInfoI = classInfoMap.get(ClassIName);

        assertNotNull(classInfoI);

        MethodInfo methodInfo = classInfoI.getMethodMap().get("methodI()V");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);
    }

    @Test
    public void testLambdaReachability() throws Exception {


        Map<String, byte[]> classMap = TestUtil.makeClassMap(ClassG.class, ClassF.class, ClassE.class, ClassD.class,
            InterfaceC.class, InterfaceB.class, InterfaceA.class);

        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(ClassGname, classMap);

        ClassInfo classInfoF = classInfoMap.get(ClassFname);
        assertNotNull(classInfoF);

        String lambda0 = "lambda$getIncrementorLambda$0(Ljava/lang/String;Ljava/lang/Integer;)Ljava/lang/String;";
        String lambda1 = "lambda$getIncrementorLambda$1(Ljava/lang/String;)Ljava/lang/Integer;";

        MethodInfo methodInfo = classInfoF.getMethodMap().get(lambda0);
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = classInfoF.getMethodMap().get(lambda1);
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = classInfoF.getMethodMap().get("onlyCalledByLambda(Ljava/lang/String;)I");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);
    }

    @Test
    public void testMapReachability() throws Exception {

        Map<String, byte[]> classMap = TestUtil.makeClassMap(FakeMapUser.class, FakeMap.class);

        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(FakeMapUsername, classMap);

        ClassInfo fakeMapInfo = classInfoMap.get(getInternalNameForClass(FakeMap.class));
        ClassInfo fakeMapUserInfo = classInfoMap.get(getInternalNameForClass(FakeMapUser.class));
        ClassInfo mapInfo = classInfoMap.get(getInternalNameForClass(Map.class));
        assertNotNull(fakeMapInfo);
        assertNotNull(fakeMapUserInfo);
        assertNotNull(mapInfo);

        MethodInfo methodInfo = mapInfo.getMethodMap().get("size()I");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = mapInfo.getMethodMap().get("remove(Ljava/lang/Object;)Ljava/lang/Object;");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = mapInfo.getMethodMap().get("isEmpty()Z");
        assertNotNull(methodInfo);
        assertFalse(methodInfo.isReachable);

        methodInfo = fakeMapInfo.getMethodMap().get("size()I");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = fakeMapInfo.getMethodMap().get("remove(Ljava/lang/Object;)Ljava/lang/Object;");
        assertNotNull(methodInfo);
        assertTrue(methodInfo.isReachable);

        methodInfo = fakeMapInfo.getMethodMap().get("isEmpty()Z");
        assertNotNull(methodInfo);
        assertFalse(methodInfo.isReachable);
    }


    private static String getInternalNameForClass(Class<?> clazz) {
        return Utilities.fulllyQualifiedNameToInternalName(clazz.getName());
    }
}
