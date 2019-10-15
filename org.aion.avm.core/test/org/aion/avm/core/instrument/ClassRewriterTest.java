package org.aion.avm.core.instrument;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


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
        ClassRewriter.IMethodReplacer replacer = visitor -> {
            visitor.visitCode();
            visitor.visitVarInsn(Opcodes.BIPUSH, changedHash);
            visitor.visitInsn(Opcodes.IRETURN);
            visitor.visitMaxs(1, 0);
            visitor.visitEnd();
        };
        String className = original.getClass().getName();
        byte[] raw = Utilities.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Function<byte[], byte[]> rewriterCall = (inputBytes) -> ClassRewriter.
                rewriteOneMethodInClass(inputBytes, "hashCode", replacer, ClassWriter.COMPUTE_FRAMES);
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, rewriterCall.apply(raw));
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        Class<?> clazz = loader.loadClass(className);
        Object target = clazz.getConstructor(int.class).newInstance(originalHash);

        // We expect these to both be the same class name.
        Assert.assertEquals(original.getClass().getName(), target.getClass().getName());
        // But be different actual class instances.
        Assert.assertNotEquals(original.getClass(), target.getClass());
        // Verify that the hashcode response changed.
        Assert.assertEquals(originalHash, original.hashCode());
        Assert.assertEquals(changedHash, target.hashCode());
    }
}
