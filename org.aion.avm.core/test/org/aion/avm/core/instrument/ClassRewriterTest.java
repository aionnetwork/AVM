package org.aion.avm.core.instrument;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.instrument.ClassRewriter.BasicBlock;
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

    /**
     * Tests that we can add a instrumented callout to the beginning of each block.
     */
    @Test
    public void testWrittenBlockPrefix() throws Exception {
        // Clear the state of our static test class.
        TestEnergy.totalCost = 0;
        TestEnergy.totalCharges = 0;
        // Setup and rewrite the class.
        String runtimeClassName = ClassRewriterTest.class.getCanonicalName().replaceAll("\\.", "/") + "$TestEnergy";
        Function<byte[], byte[]> costBuilder = (inputBytes) -> {
            Map<String, List<ClassRewriter.BasicBlock>> methodBlocks = ClassRewriter.parseMethodBlocks(inputBytes);
            // Just attach some testing cost to all of these.
            long costToAdd = 1;
            for (List<ClassRewriter.BasicBlock> list : methodBlocks.values()) {
                for (ClassRewriter.BasicBlock block : list) {
                    block.setEnergyCost(costToAdd);
                    costToAdd += 1;
                }
            }
            // Note that this test assumes that there are 4 blocks so check our next cost is 5.
            Assert.assertEquals(5, costToAdd);
            // Re-write the class.
            return ClassRewriter.rewriteBlocksInClass(runtimeClassName, inputBytes, methodBlocks);
        };
        String className = TestResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, costBuilder);
        Class<?> clazz = loader.loadClass(className);
        // By this point, we should still have 0 charges.
        Assert.assertEquals(0, TestEnergy.totalCharges);
        Object target = clazz.getConstructor(int.class).newInstance(6);
        // We expect to see 3 charges for init.
        Assert.assertEquals(3, TestEnergy.totalCharges);
        target.hashCode();
        // Now, we should expect to see 4 charges, each of a different value:  1+2+3+4 = 10
        Assert.assertEquals(4, TestEnergy.totalCharges);
        Assert.assertEquals(10, TestEnergy.totalCost);
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

    private static class BlockSnooper implements Function<byte[], byte[]> {
        public Map<String, List<ClassRewriter.BasicBlock>> resultMap;

        @Override
        public byte[] apply(byte[] inputBytes) {
            this.resultMap = ClassRewriter.parseMethodBlocks(inputBytes);
            return inputBytes;
        }

    }


    /**
     * NOTE:  This class is used for the "testWrittenBlockPrefix()" test.
     */
    public static class TestEnergy {
        public static long totalCost;
        public static int totalCharges;

        public static void chargeEnergy(long cost) {
            TestEnergy.totalCost += cost;
            TestEnergy.totalCharges += 1;
        }
    }
}
