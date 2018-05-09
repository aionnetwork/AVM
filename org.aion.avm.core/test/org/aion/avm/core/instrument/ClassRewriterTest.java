package org.aion.avm.core.instrument;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.*;


public class ClassRewriterTest {
    /**
     * We want to prove that we can instantiate one version of the TestResource and see its original implementation
     * and then change it in the custom classloader.
     */
    @Test
    public void testReplaceHashCodeMethod() throws Exception {
        int originalHash = 5;
        int changedHash = 42;
        TestResource original = new TestResource(originalHash);
        ClassRewriter.IMethodReplacer replacer = new ClassRewriter.IMethodReplacer() {
            @Override
            public void populatMethod(MethodVisitor visitor) {
                visitor.visitCode();
                visitor.visitVarInsn(Opcodes.BIPUSH, changedHash);
                visitor.visitInsn(Opcodes.IRETURN);
                visitor.visitMaxs(1, 0);
                visitor.visitEnd();
            }};
        String className = original.getClass().getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, (inputBytes) -> ClassRewriter.rewriteOneMethodInClass(inputBytes, "hashCode", replacer, ClassWriter.COMPUTE_FRAMES));
        Class<?> clazz = loader.loadClass(className);
        Object target = clazz.getConstructor(int.class).newInstance(Integer.valueOf(originalHash));

        // We expect these to both be the same class name.
        Assert.assertEquals(original.getClass().getCanonicalName(), target.getClass().getCanonicalName());
        // But be different actual class instances.
        Assert.assertNotEquals(original.getClass(), target.getClass());
        // Verify that the hashcode response changed.
        Assert.assertEquals(originalHash, original.hashCode());
        Assert.assertEquals(changedHash, target.hashCode());
    }


    /**
     * We use this classloader, within the test, to get the raw bytes of the test we want to modify and then pass
     * into the ClassRewriter, for the test.
     */
    private static class TestClassLoader extends ClassLoader {
        private final String classNameToProvide;
        private final Function<byte[], byte[]> loadHandler;
        
        public TestClassLoader(ClassLoader parent, String classNameToProvide, Function<byte[], byte[]> loadHandler) {
            super(parent);
            this.classNameToProvide = classNameToProvide;
            this.loadHandler = loadHandler;
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
                byte[] rewrittten = this.loadHandler.apply(raw);
                result = defineClass(name, rewrittten, 0, rewrittten.length);
            } else {
                result = getParent().loadClass(name);
            }
            return result;
        }
    }
}
