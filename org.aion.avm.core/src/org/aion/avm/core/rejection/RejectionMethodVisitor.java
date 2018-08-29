package org.aion.avm.core.rejection;

import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;


/**
 * Does a simple read-only pass over the loaded method, ensuring it isn't doing anything it isn't allowed to do:
 * -uses bytecode in blacklist
 * -references class not in whitelist
 * -overrides methods which we will not support as the user may expect
 * -issue an invoke initially defined on a class not in whitelist
 * 
 * When a violation is detected, throws the RejectedClassException.
 */
public class RejectionMethodVisitor extends MethodVisitor {
    private final PreRenameClassAccessRules classAccessRules;

    public RejectionMethodVisitor(MethodVisitor visitor, PreRenameClassAccessRules classAccessRules) {
        super(Opcodes.ASM6, visitor);
        this.classAccessRules = classAccessRules;
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        // Filter this.
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        // "Non-standard attributes" are not supported, so filter them.
    }

    @Override
    public void visitInsn(int opcode) {
        checkOpcode(opcode);
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        checkOpcode(opcode);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        checkOpcode(opcode);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        checkOpcode(opcode);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        checkOpcode(opcode);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (!this.classAccessRules.canUserAccessClass(owner)) {
            RejectedClassException.nonWhiteListedClass(owner);
        }
        checkOpcode(opcode);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        checkOpcode(opcode);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        // This is debug data, so filter it out.
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        // This is debug data, so filter it out.
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
    }


    private void checkOpcode(int opcode) {
        if (false
                // We reject JSR and RET (although these haven't been generated in a long time, anyway, and aren't allowed in new class files).
                || (Opcodes.JSR == opcode)
                || (Opcodes.RET == opcode)
                
                // We also want to reject instructions which could interact with the thread state:  MONITORENTER, MONITOREXIT.
                || (Opcodes.MONITORENTER == opcode)
                || (Opcodes.MONITOREXIT == opcode)
        ) {
            RejectedClassException.blacklistedOpcode(opcode);
        }
    }
}
