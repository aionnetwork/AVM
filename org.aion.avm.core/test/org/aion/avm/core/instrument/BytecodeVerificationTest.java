package org.aion.avm.core.instrument;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.*;

/*
*
*  The purpose of BVT is to ensure that we can completely trust JVM verifier
*  to verify the bytecode from user as well as our instrumented code.
*
*/
public class BytecodeVerificationTest {

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

        String className = original.getClass().getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, "hashCode", replacer, 0);
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

        String className = original.getClass().getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, "hashCode", replacer, 0);
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

        String className = original.getClass().getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, "hashCode", replacer, 1);
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

    /**
     * We use this classloader, within the test, to get the raw bytes of the test we want to modify and then pass
     * into the ClassRewriter, for the test.
     */
    private static class TestClassLoader extends ClassLoader {
        private final String classNameToProvide;
        private final String methodName;
        private final ClassRewriter.IMethodReplacer replacer;
        private final int computeFrameFlag;

        public TestClassLoader(ClassLoader parent, String classNameToProvide, String methodName, ClassRewriter.IMethodReplacer replacer, int computeFrameFlag) {
            super(parent);
            this.classNameToProvide = classNameToProvide;
            this.methodName = methodName;
            this.replacer = replacer;
            this.computeFrameFlag = computeFrameFlag;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> result = null;
            if (this.classNameToProvide.equals(name)) {
                InputStream stream = getParent().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
                byte[] raw = null;
                try {
                    raw = stream.readAllBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                    Assert.fail();
                }
                byte[] rewrittten = ClassRewriter.rewriteOneMethodInClass(raw, this.methodName, this.replacer, 0);
                result = defineClass(name, rewrittten, 0, rewrittten.length);
            } else {
                result = getParent().loadClass(name);
            }
            return result;
        }
    }
}
