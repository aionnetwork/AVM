package org.aion.avm.core.miscvisitors;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;


public class ClassRenameVisitorTest {
    private static final int PARSING_OPTIONS = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
    private static final int WRITING_OPTIONS = ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;

    @Test
    public void testSmallTarget() throws Exception {
        String targetTestName = ClassRenameVisitorTestTarget.class.getName();
        byte[] targetTestBytes = Helpers.loadRequiredResourceAsBytes(Helpers.fulllyQualifiedNameToInternalName(targetTestName) + ".class");
        
        String newName = "THE_NEW_CLASS";
        byte[] renamedBytes = new ClassToolchain.Builder(targetTestBytes, PARSING_OPTIONS)
                        .addNextVisitor(new ClassRenameVisitor(newName))
                        .addWriter(new ClassWriter(WRITING_OPTIONS))
                        .build()
                        .runAndGetBytecode();
        Class<?> original = SingleLoader.loadClass(targetTestName, targetTestBytes);
        runOnClass(original);
        Class<?> rename = SingleLoader.loadClass(newName, renamedBytes);
        runOnClass(rename);
    }

    @Test
    public void testRealHelper() throws Exception {
        String targetTestName = Helper.class.getName();
        byte[] targetTestBytes = Helpers.loadRequiredResourceAsBytes(Helpers.fulllyQualifiedNameToInternalName(targetTestName) + ".class");
        
        String newName = "THE_NEW_CLASS";
        byte[] renamedBytes = new ClassToolchain.Builder(targetTestBytes, PARSING_OPTIONS)
                        .addNextVisitor(new ClassRenameVisitor(newName))
                        .addWriter(new ClassWriter(WRITING_OPTIONS))
                        .build()
                        .runAndGetBytecode();
        Class<?> original = SingleLoader.loadClass(targetTestName, targetTestBytes);
        Assert.assertNotNull(original);
        Class<?> rename = SingleLoader.loadClass(newName, renamedBytes);
        Assert.assertNotNull(rename);
    }

    private static void runOnClass(Class<?> clazz) throws Exception {
        Object instance = clazz.getDeclaredMethod("staticBuilder").invoke(null);
        Object array = clazz.getDeclaredMethod("staticWrap", clazz).invoke(null, instance);
        clazz.getDeclaredMethod("staticOne", array.getClass()).invoke(null, array);
        clazz.getDeclaredMethod("instanceOne", clazz).invoke(instance, instance);
    }
}
