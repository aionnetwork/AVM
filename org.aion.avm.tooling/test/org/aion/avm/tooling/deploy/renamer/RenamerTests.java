package org.aion.avm.tooling.deploy.renamer;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.deploy.eliminator.ClassInfo;
import org.aion.avm.tooling.deploy.eliminator.MethodReachabilityDetector;
import org.aion.avm.tooling.deploy.renamer.resources.*;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;

import static org.aion.avm.tooling.deploy.renamer.Renamer.extractClasses;

public class RenamerTests {

    @Test
    public void testRenameInnerClasses() throws IOException {
        byte[] jarBytes = JarBuilder.buildJarForMainAndClasses(RenameTarget.class);
        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true);

        Map<String, ClassNode> classMap = Renamer.sortBasedOnInnerClassLevel(extractClasses(jarReader));

        NameGenerator generator = new NameGenerator();
        String mainClassName = Helpers.fulllyQualifiedNameToInternalName(RenameTarget.class.getName());
        Map<String, String> newClasses = ClassRenamer.renameClasses(classMap, mainClassName);

        Assert.assertEquals(generator.getNewMainClassName(), newClasses.get(mainClassName));
        Assert.assertEquals(generator.getNewMainClassName() + "$J", newClasses.get(mainClassName + "$ClassB"));
        Assert.assertEquals(generator.getNewMainClassName() + "$J$K", newClasses.get(mainClassName + "$ClassB$ClassC"));
        Assert.assertEquals(generator.getNewMainClassName() + "$J$K$L", newClasses.get(mainClassName + "$ClassB$ClassC$ClassD"));
        Assert.assertEquals(generator.getNewMainClassName() + "$J$K$L$M", newClasses.get(mainClassName + "$ClassB$ClassC$ClassD$ClassE"));
    }

    @Test
    public void testJclMethodNotRenamed() throws Exception {
        byte[] jarBytes = JarBuilder.buildJarForMainAndClasses(RenameTarget.class, ClassA.class, ClassB.class, ClassC.class, InterfaceD.class);
        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true);

        String mainClassName = Helpers.fulllyQualifiedNameToInternalName(RenameTarget.class.getName());
        Map<String, ClassNode> classMap = Renamer.sortBasedOnInnerClassLevel(extractClasses(jarReader));

        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(mainClassName, getClassBytes(classMap));
        Map<String, String> newMethodMap = MethodRenamer.renameMethods(classMap, classInfoMap);

        String comparatorMethodKeyString = "compareTo(Ljava/lang/String;)I";
        String classAName = ClassA.class.getName();
        String classBName = ClassB.class.getName();
        String classCName = ClassC.class.getName();

        Assert.assertTrue(!newMethodMap.containsKey(makeMethodFullName(classAName, comparatorMethodKeyString)));
        Assert.assertTrue(!newMethodMap.containsKey(makeMethodFullName(classBName, comparatorMethodKeyString)));
        Assert.assertTrue(!newMethodMap.containsKey(makeMethodFullName(classCName, comparatorMethodKeyString)));
    }

    @Test
    public void testRenameInheritedMethods() throws Exception {
        byte[] jarBytes = JarBuilder.buildJarForMainAndClasses(RenameTarget.class);
        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true);

        String mainClassName = Helpers.fulllyQualifiedNameToInternalName(RenameTarget.class.getName());
        Map<String, ClassNode> classMap = Renamer.sortBasedOnInnerClassLevel(extractClasses(jarReader));

        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(mainClassName, getClassBytes(classMap));
        Map<String, String> newMethodMap = MethodRenamer.renameMethods(classMap, classInfoMap);

        String ParentInterfaceOneName = RenameTarget.ParentInterfaceOne.class.getName();
        String ChildInterfaceOneName = RenameTarget.ChildInterfaceOne.class.getName();
        String ParentInterfaceTwoName = RenameTarget.ParentInterfaceTwo.class.getName();
        String ConcreteChildOneName = RenameTarget.ConcreteChildOne.class.getName();

        String getIntValMethodKeyString = "getIntVal()I";
        String mappedName = newMethodMap.get(makeMethodFullName(ParentInterfaceOneName, getIntValMethodKeyString));
        Assert.assertNotNull(mappedName);
        Assert.assertEquals(mappedName, newMethodMap.get(makeMethodFullName(ChildInterfaceOneName, getIntValMethodKeyString)));
        Assert.assertEquals(mappedName, newMethodMap.get(makeMethodFullName(ConcreteChildOneName, getIntValMethodKeyString)));

        getIntValMethodKeyString = "getIntVal(I)I";
        Assert.assertNotNull(mappedName);
        Assert.assertEquals(mappedName, newMethodMap.get(makeMethodFullName(ChildInterfaceOneName, getIntValMethodKeyString)));
        Assert.assertEquals(mappedName, newMethodMap.get(makeMethodFullName(ConcreteChildOneName, getIntValMethodKeyString)));

        String getLongValMethodKeyString = "getLongVal()J";
        mappedName = newMethodMap.get(makeMethodFullName(ChildInterfaceOneName, getLongValMethodKeyString));
        Assert.assertNotNull(mappedName);
        Assert.assertEquals(mappedName, newMethodMap.get(makeMethodFullName(ConcreteChildOneName, getLongValMethodKeyString)));

        String getCharValMethodKeyString = "getCharVal()C";
        mappedName = newMethodMap.get(makeMethodFullName(ParentInterfaceTwoName, getCharValMethodKeyString));
        Assert.assertNotNull(mappedName);
        Assert.assertEquals(mappedName, newMethodMap.get(makeMethodFullName(ConcreteChildOneName, getCharValMethodKeyString)));
    }

    @Test
    public void testRenameFields() throws Exception {
        byte[] jarBytes = JarBuilder.buildJarForMainAndClasses(RenameTarget.class);
        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(jarBytes), true);

        String mainClassName = Helpers.fulllyQualifiedNameToInternalName(RenameTarget.class.getName());
        Map<String, ClassNode> classMap = Renamer.sortBasedOnInnerClassLevel(extractClasses(jarReader));

        Map<String, ClassInfo> classInfoMap = MethodReachabilityDetector.getClassInfoMap(mainClassName, getClassBytes(classMap));
        Map<String, String> renamedFields = FieldRenamer.renameFields(classMap, classInfoMap);

        String ParentInterfaceOneName = RenameTarget.ParentInterfaceOne.class.getName();
        String ConcreteChildOneName = RenameTarget.ConcreteChildOne.class.getName();
        String ClassBName = RenameTarget.ClassB.class.getName();
        String ClassCName = RenameTarget.ClassB.ClassC.class.getName();

        String mappedName = renamedFields.get(makeFullFieldName(ConcreteChildOneName, "b"));
        Assert.assertNotNull(mappedName);
        Assert.assertEquals(mappedName, renamedFields.get(makeMethodFullName(ParentInterfaceOneName, "b")));

        mappedName = renamedFields.get(makeFullFieldName(ClassBName, "f"));
        Assert.assertNotNull(mappedName);
        Assert.assertNotEquals(mappedName, renamedFields.get(makeMethodFullName(ClassCName, "f")));
    }

    private static String makeMethodFullName(String className, String method) {
        return Helpers.fulllyQualifiedNameToInternalName(className) + '.' + method;
    }

    private static String makeFullFieldName(String className, String fieldName) {
        return Helpers.fulllyQualifiedNameToInternalName(className) + "." + fieldName;
    }

    private static Map<String, byte[]> getClassBytes(Map<String, ClassNode> classMap) {
        Map<String, byte[]> byteMap = new HashMap<>();
        for (ClassNode node : classMap.values()) {
            ClassWriter writer = new ClassWriter(0);
            node.accept(writer);
            byte[] classBytes = writer.toByteArray();
            byteMap.put(node.name, classBytes);
        }
        return byteMap;
    }
}
