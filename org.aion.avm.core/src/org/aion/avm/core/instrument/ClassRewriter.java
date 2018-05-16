package org.aion.avm.core.instrument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * A wrapper over our common ASM routines.
 *
 * This class has no explicit design, as it is still evolving.
 */
public class ClassRewriter  {
    /**
     * Rewrites the given class, changing the named method by calling replacer.  Note that this will still succeed
     * even if the method is not found.
     *
     * @param classBytes The raw bytes of the class to modify.
     * @param methodName The method to replace.
     * @param replacer The callback to invoke to build the replacement method.
     * @return The raw bytes of the updated class.
     */
    public static byte[] rewriteOneMethodInClass(byte[] classBytes, String methodName, IMethodReplacer replacer, int computeFrameFlag) {
        ClassWriter cw = new ClassWriter(computeFrameFlag);
        FullClassVisitor adapter = new FullClassVisitor(cw, methodName, replacer);

        ClassReader cr = new ClassReader(classBytes);
        cr.accept(adapter, ClassReader.SKIP_FRAMES);

        return cw.toByteArray();
    }

    /**
     * Reads a given class from raw bytes, parsing it into the instruction blocks.  Returns these as a map of method
     * identifiers to lists of blocks.  Blocks internally store a list of a instructions.
     * NOTE:  This will later change shape to support modification of the blocks and resultant methods but this
     * initial implementation is read-only.
     * 
     * @param classBytes The raw bytes of the class to modify.
     * @return The map of method names+descriptors to block lists.
     */
    public static Map<String, List<BasicBlock>> parseMethodBlocks(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        BlockClassReader reader = new BlockClassReader();
        classReader.accept(reader, ClassReader.SKIP_FRAMES);
        return reader.getBlockMap();
    }

    /**
     * Rewrites the given class, taking in a modified structure from parseMethodBlocks(byte[]), above.
     * Any methods found in that map will be modified, to include the energy consumption prefix, if they were given a non-zero energy cost.
     *
     * @param runtimeClassName The name of the class with the "chargeEnergy(J)V" static method.
     * @param classBytes The raw bytes of the class to modify.
     * @param methodData The map of method name+descriptors to the list of blocks in a given method (previously returned by parseMethodBlocks(byte[]).
     * @return The raw bytes of the updated class.
     */
    public static byte[] rewriteBlocksInClass(String runtimeClassName, byte[] classBytes, Map<String, List<BasicBlock>> methodData) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassInstrumentationVisitor adapter = new ClassInstrumentationVisitor(runtimeClassName, cw, methodData);

        ClassReader cr = new ClassReader(classBytes);
        cr.accept(adapter, ClassReader.SKIP_FRAMES);

        return cw.toByteArray();
    }


    /**
     * A helper class used internally, by rewriteOneMethodInClass.
     */
    private static class FullClassVisitor extends ClassVisitor implements Opcodes {
        private final String methodName;
        private final IMethodReplacer replacer;

        public FullClassVisitor(ClassVisitor cv, String methodName, IMethodReplacer replacer) {
            super(Opcodes.ASM6, cv);
            this.methodName = methodName;
            this.replacer = replacer;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor resultantVisitor = null;
            if (this.methodName.equals(name)) {
                // This is the method we want to replace.
                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                MethodVisitor originalVisitor = super.visitMethod(access & ~ACC_NATIVE, name, descriptor, null, exceptions);
                ReplacedMethodVisitor replacedVisitor = new ReplacedMethodVisitor(originalVisitor, this.replacer);

                // Note that we need to explicitly call the visitCode on the replaced visitory if we have converted it from native to bytecode.
                if (0 != (access & ACC_NATIVE)) {
                    replacedVisitor.visitCode();
                }
                resultantVisitor = replacedVisitor;
            } else {
                // In this case, we basically just want to pass this through.
                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                resultantVisitor = super.visitMethod(access, name, descriptor, null, exceptions);
            }
            return resultantVisitor;
        }
    }


    /**
     * A helper class used internally, by FullClassVisitor.
     */
    private static class ReplacedMethodVisitor extends MethodVisitor implements Opcodes {
        private final MethodVisitor target;
        private final IMethodReplacer replacer;

        public ReplacedMethodVisitor(MethodVisitor target, IMethodReplacer replacer) {
            super(Opcodes.ASM6, null);
            this.target = target;
            this.replacer = replacer;
        }

        @Override
        public void visitCode() {
            this.replacer.populatMethod(this.target);
        }
    }


    /**
     * A helper class used internally, by rewriteOneMethodInClass.
     * This is the final phase visitor, where bytecodes are updated in the stream.
     */
    private static class ClassInstrumentationVisitor extends ClassVisitor implements Opcodes {
        private final String runtimeClassName;
        private final Map<String, List<BasicBlock>> methodData;

        public ClassInstrumentationVisitor(String runtimeClassName, ClassVisitor cv, Map<String, List<BasicBlock>> methodData) {
            super(Opcodes.ASM6, cv);
            this.runtimeClassName = runtimeClassName;
            this.methodData = methodData;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            String methodKey = name + descriptor;
            List<BasicBlock> blocks = this.methodData.get(methodKey);
            
            MethodVisitor resultantVisitor = null;
            if (null != blocks) {
                // We want to rewrite this method, augmenting the blocks in the original by prepending any energy cost.
                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                MethodVisitor originalVisitor = super.visitMethod(access, name, descriptor, null, exceptions);
                resultantVisitor = new BlockInstrumentationVisitor(this.runtimeClassName, originalVisitor, blocks);
            } else {
                // In this case, we basically just want to pass this through.
                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                resultantVisitor = super.visitMethod(access, name, descriptor, null, exceptions);
            }
            return resultantVisitor;
        }
    }


    /**
     * A helper class used internally, by parseMethodBlocks.
     */
    private static class BlockClassReader extends ClassVisitor {
        private final Map<String, List<BasicBlock>> buildingMap;
        
        public BlockClassReader() {
            super(Opcodes.ASM6);
            this.buildingMap = new HashMap<>();
        }
        public Map<String, List<BasicBlock>> getBlockMap() {
            return Collections.unmodifiableMap(this.buildingMap);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            String uniqueName = name + descriptor;
            BlockMethodReader visitor = new BlockMethodReader(this, uniqueName);
            return visitor;
        }

        public void finishMethod(String key, List<BasicBlock> value) {
            List<BasicBlock> previous = this.buildingMap.put(key, value);
            // If we over-wrote something, this is a serious bug.
            Assert.assertNull(previous);
        }
    }


    /**
     * A helper class used internally, by BlockClassReader.
     */
    private static class BlockMethodReader extends MethodVisitor {
        private final BlockClassReader parent;
        private final String uniqueKey;
        private final List<BasicBlock> buildingList;
        private List<Integer> currentBuildingBlock;
        private List<String> currentAllocationList;
        
        public BlockMethodReader(BlockClassReader parent, String uniqueKey) {
            super(Opcodes.ASM6);
            this.parent = parent;
            this.uniqueKey = uniqueKey;
            this.buildingList = new ArrayList<>();
        }
        @Override
        public void visitCode() {
            // This is just useful for internal sanity checking.
            this.currentBuildingBlock = new ArrayList<>();
            this.currentAllocationList = new ArrayList<>();
        }
        @Override
        public void visitEnd() {
            // This is called after all the code has been walked, so seal the final block.
            if (!this.currentBuildingBlock.isEmpty()) {
                this.buildingList.add(new BasicBlock(this.currentBuildingBlock, this.currentAllocationList));
                this.currentBuildingBlock = null;
                this.currentAllocationList = null;
            }
            // And write-back our result;
            this.parent.finishMethod(this.uniqueKey, this.buildingList);
        }
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            this.currentBuildingBlock.add(opcode);
        }
        @Override
        public void visitIincInsn(int var, int increment) {
            this.currentBuildingBlock.add(Opcodes.IINC);
        }
        @Override
        public void visitInsn(int opcode) {
            this.currentBuildingBlock.add(opcode);
        }
        @Override
        public void visitIntInsn(int opcode, int operand) {
            this.currentBuildingBlock.add(opcode);
        }
        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            Assert.unreachable("invokedynamic must be filtered prior to reading basic blocks");
        }
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            this.currentBuildingBlock.add(opcode);
        }
        @Override
        public void visitLabel(Label label) {
            // Seal the previous block (avoid the case where the block is empty).
            if (!this.currentBuildingBlock.isEmpty()) {
                this.buildingList.add(new BasicBlock(this.currentBuildingBlock, this.currentAllocationList));
            }
            // Start the new block.
            this.currentBuildingBlock = new ArrayList<>();
            this.currentAllocationList = new ArrayList<>();
        }
        @Override
        public void visitLdcInsn(Object value) {
            this.currentBuildingBlock.add(Opcodes.LDC);
        }
        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            this.currentBuildingBlock.add(Opcodes.LOOKUPSWITCH);
        }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            this.currentBuildingBlock.add(opcode);
        }
        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            this.currentBuildingBlock.add(Opcodes.MULTIANEWARRAY);
        }
        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            this.currentBuildingBlock.add(Opcodes.TABLESWITCH);
        }
        @Override
        public void visitTypeInsn(int opcode, String type) {
            this.currentBuildingBlock.add(opcode);
            // If this is a new, att the type to the allocation list for the block.
            if (Opcodes.NEW == opcode) {
                this.currentAllocationList.add(type);
            }
        }
        @Override
        public void visitVarInsn(int opcode, int var) {
            this.currentBuildingBlock.add(opcode);
        }
    }


    /**
     * The interface we call back into to actually build the replacement bytecode for a method.
     * Note that this will probably evolve since it is currently a pretty leaky abstraction:  pushes MethodVisitor knowledge and responsibility to
     * implementation.
     */
    public static interface IMethodReplacer {
        void populatMethod(MethodVisitor visitor);
    }
}
