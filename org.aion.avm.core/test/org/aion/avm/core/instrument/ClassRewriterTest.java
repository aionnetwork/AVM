package org.aion.avm.core.instrument;

import org.aion.avm.core.instrument.ClassRewriter.BasicBlock;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
        String className = original.getClass().getCanonicalName();
        Function<byte[], byte[]> rewriterCall = (inputBytes) -> ClassRewriter.
                rewriteOneMethodInClass(inputBytes, "hashCode", replacer, ClassWriter.COMPUTE_FRAMES);
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, rewriterCall);
        Class<?> clazz = loader.loadClass(className);
        Object target = clazz.getConstructor(int.class).newInstance(originalHash);

        // We expect these to both be the same class name.
        Assert.assertEquals(original.getClass().getCanonicalName(), target.getClass().getCanonicalName());
        // But be different actual class instances.
        Assert.assertNotEquals(original.getClass(), target.getClass());
        // Verify that the hashcode response changed.
        Assert.assertEquals(originalHash, original.hashCode());
        Assert.assertEquals(changedHash, target.hashCode());
    }

    /**
     * Parses a test class into extents.
     */
    @Test
    public void testMethodBlocks() throws Exception {
        String className = TestResource.class.getCanonicalName();
        BlockSnooper snooper = new BlockSnooper();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, snooper);
        loader.loadClass(className);
        Map<String, List<ClassRewriter.BasicBlock>> resultMap = snooper.resultMap;
        Assert.assertNotNull(resultMap);
        List<ClassRewriter.BasicBlock> initBlocks = resultMap.get("<init>(I)V");
        int[][] expectedInitBlocks = new int[][]{
                {Opcodes.ALOAD, Opcodes.INVOKESPECIAL},
                {Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.PUTFIELD},
                {Opcodes.RETURN}
        };
        boolean didMatch = compareBlocks(expectedInitBlocks, initBlocks);
        Assert.assertTrue(didMatch);
        List<ClassRewriter.BasicBlock> hashCodeBlocks = resultMap.get("hashCode()I");
        int[][] expectedHashCodeBlocks = new int[][]{
                {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.IRETURN}
        };
        didMatch = compareBlocks(expectedHashCodeBlocks, hashCodeBlocks);
        Assert.assertTrue(didMatch);
    }

    private boolean compareBlocks(int[][] expectedBlocks, List<BasicBlock> actualBlocks) {
        boolean didMatch = true;
        if (expectedBlocks.length == actualBlocks.size()) {
            for (int i = 0; didMatch && (i < expectedBlocks.length); ++i) {
                int[] expectedBytecodes = expectedBlocks[i];
                List<Integer> actualBytecodes = actualBlocks.get(i).opcodeSequence;
                if (expectedBytecodes.length == actualBytecodes.size()) {
                    for (int j = 0; didMatch && (j < expectedBytecodes.length); ++j) {
                        if (expectedBytecodes[j] != actualBytecodes.get(j)) {
                            didMatch = false;
                        }
                    }
                } else {
                    didMatch = false;
                }
            }
        } else {
            didMatch = false;
        }
        return didMatch;
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


    private static class BlockSnooper implements Function<byte[], byte[]> {
        public Map<String, List<ClassRewriter.BasicBlock>> resultMap;

        @Override
        public byte[] apply(byte[] inputBytes) {
            this.resultMap = ClassRewriter.parseMethodBlocks(inputBytes);
            return inputBytes;
        }

    }
}
