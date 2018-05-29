package org.aion.avm.core.instrument;

import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.*;

/*
*
*  The purpose of BVT is to ensure that we can completely trust JVM verifier
*  to verify the bytecode from user as well as our instrumented code.
*
*/
public class BytecodeVerificationTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() throws Exception {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }


    @Test
    public void testMaxStackSize() throws Exception {

        int originalHash = 1;

        TestResource original = new TestResource(originalHash);
        ClassRewriter.IMethodReplacer replacer = new ClassRewriter.IMethodReplacer() {
            @Override
            public void populatMethod(MethodVisitor visitor) {
                visitor.visitCode();
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitInsn(Opcodes.IRETURN);
                visitor.visitMaxs(3, 1);
                visitor.visitEnd();
            }};

        String className = original.getClass().getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        byte[] rewrittten = ClassRewriter.rewriteOneMethodInClass(raw, "hashCode", replacer, 0);
        
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, rewrittten);
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        Class<?> clazz = loader.loadClass(className);

        try{
            Object target = clazz.getConstructor(int.class).newInstance(Integer.valueOf(originalHash));
            target.hashCode();
            Assert.assertEquals(1,0);
        }catch (Error e){
            Boolean expectedError =  e.getMessage().contains("Operand stack overflow") ? true : false;
            Assert.assertEquals(expectedError, true);
        }

        //Assert.assertEquals(10, 10);
    }

    @Test
    public void testNumOfLocals() throws Exception {
        int originalHash = 1;

        TestResource original = new TestResource(originalHash);
        ClassRewriter.IMethodReplacer replacer = new ClassRewriter.IMethodReplacer() {
            @Override
            public void populatMethod(MethodVisitor visitor) {
                visitor.visitCode();
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitVarInsn(Opcodes.ISTORE, 1);
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitVarInsn(Opcodes.ISTORE, 2);
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitVarInsn(Opcodes.ISTORE, 3);
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitVarInsn(Opcodes.ISTORE, 4);
                visitor.visitVarInsn(Opcodes.BIPUSH, 1);
                visitor.visitInsn(Opcodes.IRETURN);
                visitor.visitMaxs(3, 3);
                visitor.visitEnd();
            }};

        String className = original.getClass().getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        byte[] rewrittten = ClassRewriter.rewriteOneMethodInClass(raw, "hashCode", replacer, 0);
        
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, rewrittten);
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        Class<?> clazz = loader.loadClass(className);

        try{
            Object target = clazz.getConstructor(int.class).newInstance(Integer.valueOf(originalHash));
            target.hashCode();
            Assert.assertEquals(1,0);
        }catch (Error e){
            Boolean expectedError =  e.getMessage().contains("Local variable table overflow") ? true : false;
            Assert.assertEquals(expectedError, true);
        }
    }


    @Test
    public void testMaxStackSizeWithLoop() throws Exception {
        int originalHash = 1;

        TestResource original = new TestResource(originalHash);
        ClassRewriter.IMethodReplacer replacer = new ClassRewriter.IMethodReplacer() {
            @Override
            public void populatMethod(MethodVisitor visitor) {
                visitor.visitCode();
                Label start = new Label();
                Object[] newstack = new Object[1];
                newstack[0] = Opcodes.INTEGER;

                visitor.visitLabel(start);
                //visitor.visitFrame(Opcodes.F_FULL, 0, null, 1, newstack);
                visitor.visitFrame(Opcodes.F_FULL, 0, null, 0, null);
                visitor.visitVarInsn(Opcodes.BIPUSH, 0);
                visitor.visitJumpInsn(Opcodes.GOTO, start);
                visitor.visitInsn(Opcodes.IRETURN);
                visitor.visitMaxs(100, 100);
                visitor.visitEnd();
            }};

        String className = original.getClass().getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        byte[] rewrittten = ClassRewriter.rewriteOneMethodInClass(raw, "hashCode", replacer, 0);
        
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, rewrittten);
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        Class<?> clazz = loader.loadClass(className);

        try{
            Object target = clazz.getConstructor(int.class).newInstance(Integer.valueOf(originalHash));
            target.hashCode();
            Assert.assertEquals(1,0);
        }catch (Error e){
            Boolean expectedError =  e.getMessage().contains("Inconsistent stackmap frames at branch target") ? true : false;
            Assert.assertEquals(expectedError, true);
        }
    }
}
