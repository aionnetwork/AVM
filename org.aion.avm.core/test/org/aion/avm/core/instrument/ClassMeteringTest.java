package org.aion.avm.core.instrument;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.instrument.BasicBlock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;


public class ClassMeteringTest {
    // We keep this here just because a lot of cases want to use this sort of "do no instrumentation" cost builder for testing other rewrites.
    private final Function<byte[], byte[]> commonCostBuilder = (inputBytes) -> {
        ClassReader in = new ClassReader(inputBytes);
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        ClassMetering classMetering = new ClassMetering(out, TestEnergy.CLASS_NAME, null, null);
        in.accept(classMetering, ClassReader.SKIP_DEBUG);
        
        return out.toByteArray();
    };

    @Before
    public void setup() {
        // Clear the state of our static test class.
        TestEnergy.totalCost = 0;
        TestEnergy.totalCharges = 0;
        TestEnergy.totalArrayElements = 0;
        TestEnergy.totalArrayInstances = 0;
    }

    /**
     * Parses a test class into extents.
     */
    @Test
    public void testMethodBlocks() throws Exception {
        String className = TestResource.class.getCanonicalName();
        BlockSnooper snooper = new BlockSnooper();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, snooper, Collections.emptyMap());
        loader.loadClass(className);
        Map<String, List<BasicBlock>> resultMap = snooper.resultMap;
        Assert.assertNotNull(resultMap);
        List<BasicBlock> initBlocks = resultMap.get("<init>(I)V");
        int[][] expectedInitBlocks = new int[][]{
                {Opcodes.ALOAD, Opcodes.INVOKESPECIAL, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.PUTFIELD, Opcodes.RETURN}
        };
        boolean didMatch = compareBlocks(expectedInitBlocks, initBlocks);
        Assert.assertTrue(didMatch);
        List<BasicBlock> hashCodeBlocks = resultMap.get("hashCode()I");
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
        // Setup and rewrite the class.
        String className = TestResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, this.commonCostBuilder, Collections.emptyMap());
        Class<?> clazz = loader.loadClass(className);
        // By this point, we should still have 0 charges.
        Assert.assertEquals(0, TestEnergy.totalCharges);
        Object target = clazz.getConstructor(int.class).newInstance(6);
        // We expect to see 1 charge for init - it is only 1 block.
        Assert.assertEquals(1, TestEnergy.totalCharges);
        long expectedCost = getFees(
                Opcodes.ALOAD
                , Opcodes.INVOKESPECIAL
                , Opcodes.ALOAD
                , Opcodes.ILOAD
                , Opcodes.PUTFIELD
                , Opcodes.RETURN
        );
        Assert.assertEquals(expectedCost, TestEnergy.totalCost);
        target.hashCode();
        // Now, we should see the additional charge for the hashcode block.
        Assert.assertEquals(2, TestEnergy.totalCharges);
        expectedCost += getFees(
                Opcodes.ALOAD
                , Opcodes.GETFIELD
                , Opcodes.IRETURN
        );
        Assert.assertEquals(expectedCost, TestEnergy.totalCost);
    }

    /**
     * Tests that we can replace anewarray bytecodes with call-out routines.
     */
    @Test
    public void testAnewarrayCallOut() throws Exception {
        // Setup and rewrite the class.
        String className = TestResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, this.commonCostBuilder, Collections.emptyMap());
        Class<?> clazz = loader.loadClass(className);
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildStringArray = clazz.getMethod("buildStringArray", int.class);
        
        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        String[] one = (String[]) buildStringArray.invoke(target, 2);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(2, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Create another.
        String[] two = (String[]) buildStringArray.invoke(target, 25);
        Assert.assertEquals(25, two.length);
        Assert.assertEquals(27, TestEnergy.totalArrayElements);
        Assert.assertEquals(2, TestEnergy.totalArrayInstances);
    }

    /**
     * Tests that we can replace multianewarray bytecodes with call-out routines.
     */
    @Test
    public void testMultianewarrayCallOut() throws Exception {
        // Setup and rewrite the class.
        String className = TestResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, this.commonCostBuilder, Collections.emptyMap());
        Class<?> clazz = loader.loadClass(className);
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildMultiStringArray3 = clazz.getMethod("buildMultiStringArray3", int.class, int.class, int.class);
        
        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        String[][][] one = (String[][][]) buildMultiStringArray3.invoke(target, 2, 3, 4);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(3, one[0].length);
        Assert.assertEquals(4, one[0][1].length);
        Assert.assertEquals(24, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Verify our assumption that this is the same as the original implementation.
        String[][][] original = new TestResource(5).buildMultiStringArray3(2, 3, 4);
        Assert.assertEquals(2, original.length);
        Assert.assertEquals(3, original[0].length);
        Assert.assertEquals(4, original[0][1].length);
        // We shouldn't see an energy increase in the original class.
        Assert.assertEquals(24, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
    }

    /**
     * Tests that we can replace primitive multianewarray bytecodes with call-out routines.
     */
    @Test
    public void testMultianewarrayPrimitive() throws Exception {
        // Setup and rewrite the class.
        String className = TestResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, this.commonCostBuilder, Collections.emptyMap());
        Class<?> clazz = loader.loadClass(className);
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildLongArray2 = clazz.getMethod("buildLongArray2", int.class, int.class);
        
        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        long[][] one = (long[][]) buildLongArray2.invoke(target, 2, 3);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(3, one[0].length);
        Assert.assertEquals(6, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Verify our assumption that this is the same as the original implementation.
        long[][] original = (long[][]) new TestResource(5).buildLongArray2(2, 3);
        Assert.assertEquals(2, original.length);
        Assert.assertEquals(3, original[0].length);
        // We shouldn't see an energy increase in the original class.
        Assert.assertEquals(6, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
    }

    /**
     * Tests that we can replace primitive newarray bytecodes with call-out routines.
     */
    @Test
    public void testNewarrayChar() throws Exception {
        // Setup and rewrite the class.
        String className = TestResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, this.commonCostBuilder, Collections.emptyMap());
        Class<?> clazz = loader.loadClass(className);
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildCharArray = clazz.getMethod("buildCharArray", int.class);
        
        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        char[] one = (char[]) buildCharArray.invoke(target, 2);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(2, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Verify our assumption that this is the same as the original implementation.
        char[] original = (char[]) new TestResource(5).buildCharArray(2);
        Assert.assertEquals(2, original.length);
        // We shouldn't see an energy increase in the original class.
        Assert.assertEquals(2, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
    }

    /**
     * Tests that we can successfully walk allocation sites.
     */
    @Test
    public void testAllocationTypes() throws Exception {
        String className = TestResource.class.getCanonicalName();
        BlockSnooper snooper = new BlockSnooper();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, snooper, Collections.emptyMap());
        loader.loadClass(className);
        Map<String, List<BasicBlock>> resultMap = snooper.resultMap;
        List<BasicBlock> factoryBlocks = resultMap.get("testFactory()Lorg/aion/avm/core/instrument/TestResource;");
        // We expect this case to have a single block.
        Assert.assertEquals(1, factoryBlocks.size());
        // With a single allocation.
        BasicBlock block = factoryBlocks.get(0);
        Assert.assertEquals(1, block.allocatedTypes.size());
        // Of our TestResource type.
        Assert.assertEquals(TestResource.class.getCanonicalName(), block.allocatedTypes.get(0).replaceAll("/", "."));
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

    private long getFees(int... opcodes) {
        long total = 0;
        
        BytecodeFeeScheduler fees = new BytecodeFeeScheduler();
        fees.initialize();
        for (int opcode : opcodes) {
            total += fees.getFee(opcode);
        }
        return total;
    }


    /**
     * This function changes nothing but does read the allocation and opcode data from the class.
     */
    private static class BlockSnooper implements Function<byte[], byte[]> {
        public Map<String, List<BasicBlock>> resultMap;

        @Override
        public byte[] apply(byte[] inputBytes) {
            ClassReader in = new ClassReader(inputBytes);
            Map<String, List<BasicBlock>> result = new HashMap<>();
            
            ClassVisitor reader = new ClassVisitor(Opcodes.ASM6) {
                public MethodVisitor visitMethod(
                        final int access,
                        final String name,
                        final String descriptor,
                        final String signature,
                        final String[] exceptions) {
                    return new MethodNode(Opcodes.ASM6, access, name, descriptor, signature, exceptions) {
                        @Override
                        public void visitEnd() {
                            // Let the superclass do what it wants to finish this.
                            super.visitEnd();
                            
                            // Create the read-only visitor and use it to extract the block data.
                            BlockBuildingMethodVisitor readingVisitor = new BlockBuildingMethodVisitor();
                            this.accept(readingVisitor);
                            List<BasicBlock> blocks = readingVisitor.getBlockList();
                            result.put(name + descriptor, blocks);
                        }
                    };
                }
            };
            in.accept(reader, ClassReader.SKIP_DEBUG);
            
            this.resultMap = result;
            return inputBytes;
        }
    }


    /**
     * NOTE:  This class is used for the "testWrittenBlockPrefix()" test.
     */
    public static class TestEnergy {
        public static String CLASS_NAME = ClassMeteringTest.class.getCanonicalName().replaceAll("\\.", "/") + "$TestEnergy";
        public static long totalCost;
        public static int totalCharges;
        public static int totalArrayElements;
        public static int totalArrayInstances;

        public static void chargeEnergy(long cost) {
            TestEnergy.totalCost += cost;
            TestEnergy.totalCharges += 1;
        }

        public static Object multianewarray1(int d1, Class<?> cl) {
            TestEnergy.totalArrayElements += d1;
            TestEnergy.totalArrayInstances += 1;
            return Array.newInstance(cl, d1);
        }

        public static Object multianewarray2(int d1, int d2, Class<?> cl) {
            TestEnergy.totalArrayElements += d1 * d2;
            TestEnergy.totalArrayInstances += 1;
            return Array.newInstance(cl, d1, d2);
        }

        public static Object multianewarray3(int d1, int d2, int d3, Class<?> cl) {
            TestEnergy.totalArrayElements += d1 * d2 * d3;
            TestEnergy.totalArrayInstances += 1;
            return Array.newInstance(cl, d1, d2, d3);
        }
    }
}
