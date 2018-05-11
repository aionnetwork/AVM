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
import org.objectweb.asm.Type;


/**
 * A wrapper over our common ASM routines.
 *
 * This class has no explicit design, as it is still evolving.
 */
public class ClassRewriter {
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
                MethodVisitor originalVisitor = super.visitMethod(access & ~ACC_NATIVE, name, descriptor, signature, exceptions);
                ReplacedMethodVisitor replacedVisitor = new ReplacedMethodVisitor(originalVisitor, this.replacer);

                // Note that we need to explicitly call the visitCode on the replaced visitory if we have converted it from native to bytecode.
                if (0 != (access & ACC_NATIVE)) {
                    replacedVisitor.visitCode();
                }
                resultantVisitor = replacedVisitor;
            } else {
                // In this case, we basically just want to pass this through.
                resultantVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
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
                MethodVisitor originalVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                resultantVisitor = new MethodInstrumentationVisitor(this.runtimeClassName, originalVisitor, blocks);
            } else {
                // In this case, we basically just want to pass this through.
                resultantVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            }
            return resultantVisitor;
        }
    }


    /**
     * A helper class used internally, by ClassInstrumentationVisitor.
     * It is responsible for re-writing the methods with the various call-outs and other manipulations.
     * 
     * Prepending instrumentation is one of the more complex ASM interactions, so it warrants some explanation:
     * -we will advance through the block list we were given while walking the blocks, much like BlockMethodReader.
     * -when we reach the beginning of a new block, we will inject the energy accounting helper before passing the
     * method through to the writer.
     * 
     * Array allocation replacement is also one the more complex cases, worth explaining:
     * -newarray - call to special static helpers, based on underlying native:  no change to stack shape
     * -anewarray - call to special static helper, requires pushing the associated class constant onto the stack
     * -multianewarray - call to special static helpers, requires pushing the associated class constant onto the stack
     * Only anewarray is done without argument introspection.  Note that multianewarray can be called for any [2..255]
     * dimension array.
     * TODO: Generate the source for these helpers.  For now, we will just define that [2..4] are allowed.
     */
    private static class MethodInstrumentationVisitor extends MethodVisitor implements Opcodes {
        private final String runtimeClassName;
        private final MethodVisitor target;
        private final List<BasicBlock> blocks;
        private boolean scanningToNewBlockStart;
        private int nextBlockIndexToWrite;

        public MethodInstrumentationVisitor(String runtimeClassName, MethodVisitor target, List<BasicBlock> blocks) {
            super(Opcodes.ASM6, null);
            this.runtimeClassName = runtimeClassName;
            this.target = target;
            this.blocks = blocks;
        }

        @Override
        public void visitCode() {
            // We initialize the state machine.
            this.scanningToNewBlockStart = true;
            this.nextBlockIndexToWrite = 0;
            // We also need to tell the writer to advance.
            this.target.visitCode();
        }
        @Override
        public void visitEnd() {
            // We never have empty blocks, in our implementation, so we should always be done when we reach this point.
            Assert.assertTrue(this.blocks.size() == this.nextBlockIndexToWrite);
            // Tell the writer we are done.
            this.target.visitEnd();
        }
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            checkInject();
            this.target.visitFieldInsn(opcode, owner, name, descriptor);
        }
        @Override
        public void visitIincInsn(int var, int increment) {
            checkInject();
            this.target.visitIincInsn(var, increment);
        }
        @Override
        public void visitInsn(int opcode) {
            checkInject();
            this.target.visitInsn(opcode);
        }
        @Override
        public void visitIntInsn(int opcode, int operand) {
            checkInject();
            // This is where the newarray bytecode might appear:  the operand is a specially-defined list of primitive types.
            if (Opcodes.NEWARRAY == opcode) {
                // We will handle this through the common multianewarray1 helper instead of creating a new helper for every case.
                String type = null;
                String descriptor = null;
                switch (operand) {
                case 4: {
                    // boolean
                    type = "java/lang/Boolean";
                    descriptor = "[Z";
                    break;
                }
                case 5: {
                    // char
                    type = "java/lang/Character";
                    descriptor = "[C";
                    break;
                }
                case 6: {
                    // float
                    type = "java/lang/Float";
                    descriptor = "[F";
                    break;
                }
                case 7: {
                    // double
                    type = "java/lang/Double";
                    descriptor = "[D";
                    break;
                }
                case 8: {
                    // byte
                    type = "java/lang/Byte";
                    descriptor = "[B";
                    break;
                }
                case 9: {
                    // short
                    type = "java/lang/Short";
                    descriptor = "[S";
                    break;
                }
                case 10: {
                    // int
                    type = "java/lang/Integer";
                    descriptor = "[I";
                    break;
                }
                case 11: {
                    // long
                    type = "java/lang/Long";
                    descriptor = "[J";
                    break;
                }
                default:
                    Assert.unreachable("Unknown newarray operand: " + operand);
                }
                this.target.visitFieldInsn(Opcodes.GETSTATIC, type, "TYPE", "Ljava/lang/Class;");
                String methodName = "multianewarray1";
                String signature = "(ILjava/lang/Class;)Ljava/lang/Object;";
                this.target.visitMethodInsn(Opcodes.INVOKESTATIC, this.runtimeClassName, methodName, signature, false);
                this.target.visitTypeInsn(Opcodes.CHECKCAST, descriptor);
            } else {
                this.target.visitIntInsn(opcode, operand);
            }
        }
        @Override
        public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
            Assert.unreachable("invokedynamic must be filtered prior to updating basic blocks");
        }
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            checkInject();
            this.target.visitJumpInsn(opcode, label);
        }
        @Override
        public void visitLabel(Label label) {
            // The label means that we found a new block (although there might be several labels before it actually starts)
            // so enter the state machine mode where we are looking for that beginning of a block.
            this.scanningToNewBlockStart = true;
            this.target.visitLabel(label);
        }
        @Override
        public void visitLdcInsn(Object value) {
            checkInject();
            this.target.visitLdcInsn(value);
        }
        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            checkInject();
            this.target.visitLookupSwitchInsn(dflt, keys, labels);
        }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            checkInject();
            this.target.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
        @Override
        public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
            checkInject();
            // We don't actually want to write multianewarray bytecodes, but always replace them with call-outs.
            if (numDimensions > 3) {
                // TODO:  Build something to generate the rest of the helpers we may need, here.  We will only go to 3 for the initial tests.
                Assert.unimplemented("TODO:  Build something to generate the rest of the helpers we may need, here.  We will only go to 3 for the initial tests.");
            }
            // TODO:  Can we be certain numDimensions is at least 2 (if it can't be one, we have an unused method in the static)?
            // This is just like anewarray, except that the invokestatic target differs, based on numDimensions.
            
            // NOTE:  This bytecode is also used for primitive multi-arrays which DO NOT have "L" or ";" in their descriptors.
            // For example, "long[][]" is "[[J" so we need to handle those a little differently.
            // Also note that primitive array construction is done very oddly:  since primitives don't have classes, a placeholder is loaded from
            // the corresponding capital wrapper class, as a static.  We need to generate those special-cases here.
            
            int indexOfL = descriptor.indexOf("L");
            boolean isObjectType = (-1 != indexOfL);
            // Note that we load class references via ldc but primitive types via getstatic.
            if (isObjectType) {
                // The descriptor we are given here is the arrayclass descriptor, along the lines of "[[[Ljava/lang/String;" but our helper
                // wants to receive the raw class so convert it, here, by pruning "[*L" and ";" from the descriptor.
                String prunedClassName = descriptor.substring(indexOfL + 1, descriptor.length() - 1);
                this.target.visitLdcInsn(Type.getObjectType(prunedClassName));
            } else {
                // java/lang/Long.TYPE:Ljava/lang/Class;
                String typeName = descriptor.replaceAll("\\[", "");
                // We expect that this is only 1 char (or we parsed this wrong or were passed something we couldn't interpret).
                Assert.assertTrue(1 == typeName.length());
                String type = null;
                switch (typeName.charAt(0)) {
                case 'Z': {
                    type = "java/lang/Boolean";
                    break;
                }
                case 'B': {
                    type = "java/lang/Byte";
                    break;
                }
                case 'C': {
                    type = "java/lang/Char";
                    break;
                }
                case 'S': {
                    type = "java/lang/Short";
                    break;
                }
                case 'I': {
                    type = "java/lang/Integer";
                    break;
                }
                case 'J': {
                    type = "java/lang/Long";
                    break;
                }
                case 'F': {
                    type = "java/lang/Float";
                    break;
                }
                case 'D': {
                    type = "java/lang/Double";
                    break;
                }
                default:
                    Assert.unreachable("Unknown primitive type: \"" + typeName + "\"");
                }
                this.target.visitFieldInsn(Opcodes.GETSTATIC, type, "TYPE", "Ljava/lang/Class;");
            }
            String argList = "(";
            for (int i = 0; i < numDimensions; ++i) {
                argList += "I";
            }
            argList += "Ljava/lang/Class;)";
            String methodName = "multianewarray" + numDimensions;
            String signature = argList + "Ljava/lang/Object;";
            this.target.visitMethodInsn(Opcodes.INVOKESTATIC, this.runtimeClassName, methodName, signature, false);
            this.target.visitTypeInsn(Opcodes.CHECKCAST, descriptor);
        }
        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            checkInject();
            this.target.visitTableSwitchInsn(min, max, dflt, labels);
        }
        @Override
        public void visitTypeInsn(int opcode, String type) {
            checkInject();
            // This is where we might see anewarray, so see if we need to replace it with a helper.
            if (Opcodes.ANEWARRAY == opcode) {
                // Inject our special idiom:  ldc then invokestatic, finally checkcast.
                this.target.visitLdcInsn(Type.getObjectType(type));
                this.target.visitMethodInsn(INVOKESTATIC, this.runtimeClassName, "anewarray", "(ILjava/lang/Class;)Ljava/lang/Object;", false);
                this.target.visitTypeInsn(Opcodes.CHECKCAST, "[L" + type + ";");
            } else {
                this.target.visitTypeInsn(opcode, type);
            }
        }
        @Override
        public void visitVarInsn(int opcode, int var) {
            checkInject();
            this.target.visitVarInsn(opcode, var);
        }
        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            this.target.visitMaxs(maxStack, maxLocals);
        }
        /**
         * Common state machine advancing call.  Called at every instruction to see if we need to inject and/or advance
         * the state machine.
         */
        private void checkInject() {
            if (this.scanningToNewBlockStart) {
                // We were witing for this so see if we have to do anything.
                BasicBlock currentBlock = this.blocks.get(this.nextBlockIndexToWrite);
                if (currentBlock.energyCost > 0) {
                    // Inject the bytecodes.
                    this.target.visitLdcInsn(Long.valueOf(currentBlock.energyCost));
                    this.target.visitMethodInsn(INVOKESTATIC, this.runtimeClassName, "chargeEnergy", "(J)V", false);
                }
                // Reset the state machine for the next block.
                this.scanningToNewBlockStart = false;
                this.nextBlockIndexToWrite += 1;
            }
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
        }
        @Override
        public void visitEnd() {
            // This is called after all the code has been walked, so seal the final block.
            if (!this.currentBuildingBlock.isEmpty()) {
                this.buildingList.add(new BasicBlock(this.currentBuildingBlock));
                this.currentBuildingBlock = null;
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
                this.buildingList.add(new BasicBlock(this.currentBuildingBlock));
            }
            // Start the new block.
            this.currentBuildingBlock = new ArrayList<>();
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


    /**
     * Describes a single basic block within a method.
     * Note that only the opcodeSequence is meant to be immutable.  Other instance variables are mutable, deliberately, to allow for mutation requests.
     */
    public static class BasicBlock {
        public final List<Integer> opcodeSequence;
        private long energyCost;
        
        public BasicBlock(List<Integer> opcodes) {
            this.opcodeSequence = Collections.unmodifiableList(opcodes);
        }
        
        /**
         * Sets the cost of the block, so that the accounting idiom will be prepended when the block is next serialized.
         * @param energyCost The energy cost.
         */
        public void setEnergyCost(long energyCost) {
            this.energyCost = energyCost;
        }
        
        /**
         * Called when serializing the block to determine if the accounting idiom should be prepended.
         * @return The energy cost of the block.
         */
        public long getEnergyCost() {
            return this.energyCost;
        }
    }
}
