package org.aion.avm.core.instrument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Collects information regarding BasicBlocks within a method.
 * Specifically, this refers to the opcodes and allocated types within a given block.
 * TODO:  Expose the number of options in the switch opcodes within this.
 * 
 * Note that this was adapted from the ClassRewriter.BlockMethodReader.
 */
public class BlockBuildingMethodVisitor extends MethodVisitor {
    private final List<BasicBlock> buildingList;
    private List<Integer> currentBuildingBlock;
    private List<String> currentAllocationList;

    public BlockBuildingMethodVisitor() {
        super(Opcodes.ASM6);
        this.buildingList = new ArrayList<>();
    }

    public List<BasicBlock> getBlockList() {
        return Collections.unmodifiableList(this.buildingList);
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
