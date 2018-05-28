package org.aion.avm.core.instrument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.instrument.BasicBlock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.*;


/**
 * Split from ClassMeteringTest to handle the read-only testing, to keep things simpler.
 */
public class ClassMeteringReadOnlyTest {
    private static Map<String, List<BasicBlock>> METHOD_BLOCKS;

    @Before
    public void setup() throws Exception {
        // Setup and rewrite the class.
        String className = TestResource.class.getName();
        byte[] raw = TestClassLoader.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        BlockSnooper snooper = new BlockSnooper();
        Map<String, byte[]> classes = new HashMap<>(CommonGenerators.generateExceptionShadowsAndWrappers());
        classes.put(className, snooper.apply(raw));
        TestClassLoader loader = new TestClassLoader(classes);
        loader.loadClass(className);
        ClassMeteringReadOnlyTest.METHOD_BLOCKS = snooper.resultMap;
        Assert.assertNotNull(ClassMeteringReadOnlyTest.METHOD_BLOCKS);
    }

    /**
     * Parses a test class into extents.
     */
    @Test
    public void testMethodBlocks() throws Exception {
        List<BasicBlock> initBlocks = METHOD_BLOCKS.get("<init>(I)V");
        int[][] expectedInitBlocks = new int[][]{
                {Opcodes.ALOAD, Opcodes.INVOKESPECIAL, Opcodes.ALOAD, Opcodes.ILOAD, Opcodes.PUTFIELD, Opcodes.RETURN}
        };
        boolean didMatch = compareBlocks(expectedInitBlocks, initBlocks);
        Assert.assertTrue(didMatch);
        List<BasicBlock> hashCodeBlocks = METHOD_BLOCKS.get("hashCode()I");
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
        List<BasicBlock> factoryBlocks = METHOD_BLOCKS.get("testFactory()Lorg/aion/avm/core/instrument/TestResource;");
        // We expect this case to have a single block.
        Assert.assertEquals(1, factoryBlocks.size());
        // With a single allocation.
        BasicBlock block = factoryBlocks.get(0);
        Assert.assertEquals(1, block.allocatedTypes.size());
        // Of our TestResource type.
        Assert.assertEquals(TestResource.class.getName(), block.allocatedTypes.get(0).replaceAll("/", "."));
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
}
