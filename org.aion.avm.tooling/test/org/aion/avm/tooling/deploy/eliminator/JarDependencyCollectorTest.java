package org.aion.avm.tooling.deploy.eliminator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassG;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassF;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassE;
import org.aion.avm.tooling.deploy.eliminator.resources.ClassD;
import org.aion.avm.tooling.deploy.eliminator.resources.InterfaceC;
import org.aion.avm.tooling.deploy.eliminator.resources.InterfaceB;
import org.aion.avm.tooling.deploy.eliminator.resources.InterfaceA;
import org.junit.Test;

public class JarDependencyCollectorTest {

    private JarDependencyCollector jarDependencyCollector;

    private static String InterfaceAname = "org/aion/avm/tooling/deploy/eliminator/resources/InterfaceA";
    private static String InterfaceBname = "org/aion/avm/tooling/deploy/eliminator/resources/InterfaceB";
    private static String InterfaceCname = "org/aion/avm/tooling/deploy/eliminator/resources/InterfaceC";
    private static String ClassDname = "org/aion/avm/tooling/deploy/eliminator/resources/ClassD";
    private static String ClassEname = "org/aion/avm/tooling/deploy/eliminator/resources/ClassE";
    private static String ClassFname = "org/aion/avm/tooling/deploy/eliminator/resources/ClassF";
    private static String ClassGname = "org/aion/avm/tooling/deploy/eliminator/resources/ClassG";

    @Test
    public void testTypeRelationships() throws IOException {

        // The type hierarchy looks like this:
        // B --> C ----     ----> G
        //            v     |
        // A -------> D -> E---> F
        Map<String, byte[]> classMap = TestUtil.makeClassMap(ClassG.class, ClassF.class, ClassE.class, ClassD.class,
                InterfaceC.class, InterfaceB.class, InterfaceA.class);

        Map<String, ClassInfo> classInfoMap = JarDependencyCollector.getClassInfoMap(classMap);
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

        // Assertions regarding Interface A

        assertEquals(1, classInfoA.getParents().size());
        assertEquals(4, classInfoA.getChildren().size());
        assertTrue(classInfoA.getChildren().contains(classInfoD));
        assertTrue(classInfoA.getChildren().contains(classInfoE));
        assertTrue(classInfoA.getChildren().contains(classInfoF));
        assertTrue(classInfoA.getChildren().contains(classInfoG));

        // Assertions regarding Interface B

        assertEquals(1, classInfoB.getParents().size());
        assertEquals(5, classInfoB.getChildren().size());
        assertTrue(classInfoB.getChildren().contains(classInfoC));
        assertTrue(classInfoB.getChildren().contains(classInfoD));
        assertTrue(classInfoB.getChildren().contains(classInfoE));
        assertTrue(classInfoB.getChildren().contains(classInfoF));
        assertTrue(classInfoB.getChildren().contains(classInfoG));

        // Assertions regarding Interface C

        assertEquals(2, classInfoC.getParents().size());
        assertEquals(4, classInfoC.getChildren().size());
        assertTrue(classInfoC.getParents().contains(classInfoB));
        assertTrue(classInfoC.getChildren().contains(classInfoD));
        assertTrue(classInfoC.getChildren().contains(classInfoE));
        assertTrue(classInfoC.getChildren().contains(classInfoF));
        assertTrue(classInfoC.getChildren().contains(classInfoG));

        // Assertions regarding Class D

        assertEquals(4, classInfoD.getParents().size());
        assertEquals(3, classInfoD.getChildren().size());
        assertTrue(classInfoD.getParents().contains(classInfoA));
        assertTrue(classInfoD.getParents().contains(classInfoB));
        assertTrue(classInfoD.getParents().contains(classInfoC));
        assertTrue(classInfoD.getChildren().contains(classInfoE));
        assertTrue(classInfoD.getChildren().contains(classInfoF));
        assertTrue(classInfoD.getChildren().contains(classInfoG));

        // Assertions regarding Class E

        assertEquals(5, classInfoE.getParents().size());
        assertEquals(2, classInfoE.getChildren().size());
        assertTrue(classInfoE.getParents().contains(classInfoA));
        assertTrue(classInfoE.getParents().contains(classInfoB));
        assertTrue(classInfoE.getParents().contains(classInfoC));
        assertTrue(classInfoE.getParents().contains(classInfoD));
        assertTrue(classInfoE.getChildren().contains(classInfoF));
        assertTrue(classInfoE.getChildren().contains(classInfoG));

        // Assertions regarding Class F

        assertEquals(6, classInfoF.getParents().size());
        assertEquals(0, classInfoF.getChildren().size());
        assertTrue(classInfoF.getParents().contains(classInfoA));
        assertTrue(classInfoF.getParents().contains(classInfoB));
        assertTrue(classInfoF.getParents().contains(classInfoC));
        assertTrue(classInfoF.getParents().contains(classInfoD));
        assertTrue(classInfoF.getParents().contains(classInfoE));

        // Assertions regarding Class G

        assertEquals(6, classInfoG.getParents().size());
        assertEquals(0, classInfoG.getChildren().size());
        assertTrue(classInfoG.getParents().contains(classInfoA));
        assertTrue(classInfoG.getParents().contains(classInfoB));
        assertTrue(classInfoG.getParents().contains(classInfoC));
        assertTrue(classInfoG.getParents().contains(classInfoD));
        assertTrue(classInfoG.getParents().contains(classInfoE));
    }

    @Test
    public void testMethodInvocations() throws IOException {

        // The type hierarchy looks like this:
        // B --> C ----     ----> G
        //            v     |
        // A -------> D -> E---> F

        Map<String, byte[]> classMap = TestUtil.makeClassMap(ClassG.class, ClassF.class, ClassE.class, ClassD.class,
            InterfaceC.class, InterfaceB.class, InterfaceA.class);

        Map<String, ClassInfo> classInfoMap = JarDependencyCollector.getClassInfoMap(classMap);

        ClassInfo classInfoG = classInfoMap.get(ClassGname);
        MethodInfo mainMethodInfo = classInfoG.getMethodMap().get("main()[B");
        assertNotNull(mainMethodInfo);
        // We expect calls to
        // - ClassF's constructor
        // - ClassG's constructor
        // - InterfaceB::interfaceB()
        // - ClassD::classD()
        // - ClassF::classD()
        // - ClassG::callClassD()
        // - ClassG::invokeFlambda()V
        // - ClassF::interfaceC()V
        // - ClassF::classFStaticMethod()I
        assertEquals(9, mainMethodInfo.methodInvocations.size());
    }
}
