package org.aion.avm.core.instrument;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.instrument.BasicBlock;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;


public class BlockBuildingMethodVisitorTest {
    private static Map<String, List<BasicBlock>> METHOD_BLOCKS;

    @BeforeClass
    public static void setup() throws Exception {
        // All of these cases are about cracking the same test class so just get the common data we all need.
        String className = BlockTestResource.class.getCanonicalName();
        BlockSnooper snooper = new BlockSnooper();
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), className, snooper, Collections.emptyMap());
        loader.loadClass(className);
        BlockBuildingMethodVisitorTest.METHOD_BLOCKS = snooper.resultMap;
        Assert.assertNotNull(BlockBuildingMethodVisitorTest.METHOD_BLOCKS);
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
        boolean didMatch = compareBlocks(expectedHashCodeBlocks, hashCodeBlocks);
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
        boolean didMatch = compareBlocks(expectedHashCodeBlocks, hashCodeBlocks);
        Assert.assertTrue(didMatch);
    }


    // This method is provided here for debugging, etc, but normally isn't used.
    @SuppressWarnings("unused")
    private static void writeAllBlocks() {
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
                    // We need a MethodNode to grab the result when the method visitation is finished.
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
