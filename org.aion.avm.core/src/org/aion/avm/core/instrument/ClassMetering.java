package org.aion.avm.core.instrument;

import org.aion.avm.core.ClassHierarchyForest;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;

public class ClassMetering extends ClassVisitor {
    private final String runtimeClassName;
    private ClassHierarchyForest classHierarchy;
    private Map<String, Integer> objectSizes;
    private final BytecodeFeeScheduler bytecodeFeeScheduler;

    public ClassMetering(ClassVisitor visitor, String runtimeClassName, ClassHierarchyForest classHierarchy, Map<String, Integer> objectSizes) {
        super(Opcodes.ASM6, visitor);

        this.runtimeClassName = runtimeClassName;
        this.classHierarchy = classHierarchy;
        this.objectSizes = objectSizes;
        
        // Note that we construct the fee scheduler, internally.
        this.bytecodeFeeScheduler = new BytecodeFeeScheduler();
        this.bytecodeFeeScheduler.initialize();
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        // Capture the visitor which actually constitutes the pipeline - we will need to do another pass before this one.
        MethodVisitor realVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        
        return new MethodNode(Opcodes.ASM6, access, name, descriptor, signature, exceptions) {
            @Override
            public void visitEnd() {
                // Let the superclass do what it wants to finish this.
                super.visitEnd();
                
                // Create the read-only visitor and use it to extract the block data.
                BlockBuildingMethodVisitor readingVisitor = new BlockBuildingMethodVisitor();
                this.accept(readingVisitor);
                List<BasicBlock> blocks = readingVisitor.getBlockList();
                
                // Apply the block fee for the bytecodes it contains.
                for (BasicBlock block : blocks) {
                    long feeForBlock = calculateBlockFee(block);
                    block.setEnergyCost(feeForBlock);
                    // TODO:  Apply static allocation-related cost.
                }
                
                // We can now build the wrapper over the real visitor, and accept it in order to add the instrumentation.
                BlockInstrumentationVisitor instrumentingVisitor = new BlockInstrumentationVisitor(ClassMetering.this.runtimeClassName, realVisitor, blocks);
                this.accept(instrumentingVisitor);
            }
        };
    }

    /**
     * Walks the opcodes in a given block, returning the total fee they will cost the block.
     * Note:  This was original implemented as part of FeeChargingCodeInjector.
     * 
     * @param block A code block.
     * @return The block fee.
     */
    private long calculateBlockFee(BasicBlock block) {
        long blockFee = 0;

        // Sum up the bytecode fee in the code block
        for (Integer opcode : block.opcodeSequence) {
            blockFee += this.bytecodeFeeScheduler.getFee(opcode);
        }

        return blockFee;
    }
}
