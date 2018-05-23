package org.aion.avm.core.instrument;

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


/**
 * Split from ClassMeteringTest to handle the read-only testing, to keep things simpler.
 */
public class ClassMeteringReadOnlyTest {
    private BlockSnooper snooper;

    @Before
    public void setup() throws Exception {
        // Setup and rewrite the class.
        String className = TestResource.class.getCanonicalName();
        this.snooper = new BlockSnooper();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), this.snooper);
        byte[] raw = loader.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        loader.addClassForRewrite(className, raw);
        loader.loadClass(className);
    }

    /**
     * Parses a test class into extents.
     */
    @Test
    public void testMethodBlocks() throws Exception {
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
     * Tests that we can successfully walk allocation sites.
     */
    @Test
    public void testAllocationTypes() throws Exception {
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
}
