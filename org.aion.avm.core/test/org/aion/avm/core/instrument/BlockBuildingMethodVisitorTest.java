package org.aion.avm.core.instrument;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlockBuildingMethodVisitorTest {

    private Map<String, List<BasicBlock>> METHOD_BLOCKS;

    @Before
    public void setup() throws Exception {
        // All of these cases are about cracking the same test class so just get the common data we all need.
        String className = BlockTestResource.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, raw);
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        loader.loadClass(className);
        this.METHOD_BLOCKS = BlockSnooper.findPerMethodBlocksFor(raw);
        Assert.assertNotNull(this.METHOD_BLOCKS);
    }

    @Test
    public void test_returnInt() throws Exception {
        List<BasicBlock> initBlocks = METHOD_BLOCKS.get("returnInt()I");
        int[][] expectedInitBlocks = new int[][]{
                {Opcodes.ICONST_5, Opcodes.IRETURN},
        };
        boolean didMatch = compareBlocks(expectedInitBlocks, initBlocks);
        Assert.assertTrue(didMatch);
        BytecodeFeeScheduler s = new BytecodeFeeScheduler();
        s.initialize();
    }

    @Test
    public void test_throwException() throws Exception {
        List<BasicBlock> initBlocks = METHOD_BLOCKS.get("throwException()I");
        int[][] expectedInitBlocks = new int[][]{
                {Opcodes.NEW, Opcodes.DUP, Opcodes.INVOKESPECIAL, Opcodes.ATHROW},
        };
        boolean didMatch = compareBlocks(expectedInitBlocks, initBlocks);
        Assert.assertTrue(didMatch);
        BytecodeFeeScheduler s = new BytecodeFeeScheduler();
        s.initialize();
    }

    @Test
    public void test_checkBranch() throws Exception {
        List<BasicBlock> hashCodeBlocks = METHOD_BLOCKS.get("checkBranch(I)I");
        int[][] expectedHashCodeBlocks = new int[][]{
                {Opcodes.ICONST_5, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.BIPUSH, Opcodes.IF_ICMPLE},
                {Opcodes.BIPUSH, Opcodes.ISTORE, Opcodes.GOTO},
                {Opcodes.ICONST_4, Opcodes.ISTORE},
                {Opcodes.ILOAD, Opcodes.IRETURN},
        };
        boolean didMatch = compareBlocks(expectedHashCodeBlocks, hashCodeBlocks);
        Assert.assertTrue(didMatch);
    }

    @Test
    public void test_checkTableSwitch() throws Exception {
        List<BasicBlock> hashCodeBlocks = METHOD_BLOCKS.get("checkTableSwitch(I)I");
        int[][] expectedHashCodeBlocks = new int[][]{
                {Opcodes.ICONST_5, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.TABLESWITCH},
                {Opcodes.ICONST_1, Opcodes.ISTORE, Opcodes.GOTO},
                {Opcodes.ICONST_2, Opcodes.ISTORE, Opcodes.GOTO},
                {Opcodes.ICONST_3, Opcodes.ISTORE, Opcodes.GOTO},
                {Opcodes.ICONST_0, Opcodes.ISTORE},
                {Opcodes.ILOAD, Opcodes.IRETURN},
        };
        int[][] expectedSwitchCounts = new int[][]{
            {4},
            {},
            {},
            {},
            {},
            {},
        };
        
        // Verify the shape of the blocks.
        boolean didMatch = compareBlocks(expectedHashCodeBlocks, hashCodeBlocks);
        Assert.assertTrue(didMatch);
        
        // Verify the switch option value.
        didMatch = compareSwitches(expectedSwitchCounts, hashCodeBlocks);
        Assert.assertTrue(didMatch);
    }

    @Test
    public void test_checkLookupSwitch() throws Exception {
        List<BasicBlock> hashCodeBlocks = METHOD_BLOCKS.get("checkLookupSwitch(I)I");
        int[][] expectedHashCodeBlocks = new int[][]{
                {Opcodes.ICONST_5, Opcodes.ISTORE, Opcodes.ILOAD, Opcodes.LOOKUPSWITCH},
                {Opcodes.ICONST_1, Opcodes.ISTORE, Opcodes.GOTO},
                {Opcodes.ICONST_2, Opcodes.ISTORE, Opcodes.GOTO},
                {Opcodes.ICONST_3, Opcodes.ISTORE, Opcodes.GOTO},
                {Opcodes.ICONST_0, Opcodes.ISTORE},
                {Opcodes.ILOAD, Opcodes.IRETURN},
        };
        int[][] expectedSwitchCounts = new int[][]{
            {4},
            {},
            {},
            {},
            {},
            {},
        };
        
        // Verify the shape of the blocks.
        boolean didMatch = compareBlocks(expectedHashCodeBlocks, hashCodeBlocks);
        Assert.assertTrue(didMatch);
        
        // Verify the switch option value.
        didMatch = compareSwitches(expectedSwitchCounts, hashCodeBlocks);
        Assert.assertTrue(didMatch);
    }


    // This method is provided here for debugging, etc, but normally isn't used.
    @SuppressWarnings("unused")
    private void writeAllBlocks() {
        for (Map.Entry<String, List<BasicBlock>> entry : METHOD_BLOCKS.entrySet()) {
            System.out.println(entry.getKey());
            for (BasicBlock block : entry.getValue()) {
                for (int opcode : block.opcodeSequence) {
                    System.out.print(" " + Integer.toHexString(opcode));
                }
                System.out.println();
            }
        }
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

    private boolean compareSwitches(int[][] expectedSwitches, List<BasicBlock> actualBlocks) {
        boolean didMatch = true;
        if (expectedSwitches.length == actualBlocks.size()) {
            for (int i = 0; didMatch && (i < expectedSwitches.length); ++i) {
                int[] expectedCases = expectedSwitches[i];
                List<Integer> actualCases = actualBlocks.get(i).switchCases;
                if (expectedCases.length == actualCases.size()) {
                    for (int j = 0; didMatch && (j < expectedCases.length); ++j) {
                        if (expectedCases[j] != actualCases.get(j)) {
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
