package org.aion.avm.core.rejection;

import org.aion.avm.core.ClassWhiteList;
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
 * 
 * When a violation is detected, throws the RejectedClassException.
 */
public class RejectionMethodVisitor extends MethodVisitor {
    private final ClassWhiteList classWhiteList;

    public RejectionMethodVisitor(MethodVisitor visitor, ClassWhiteList classWhiteList) {
        super(Opcodes.ASM6, visitor);
        this.classWhiteList = classWhiteList;
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
        ClassAccessVerifier.checkOptionallyDecorated(this.classWhiteList, type);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        checkOpcode(opcode);
        ClassAccessVerifier.checkDescriptor(this.classWhiteList, descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        checkOpcode(opcode);
        // NOTE:  "owner" is usually just a class name but it can sometimes be an array (for calls like "clone()"),
        // so decode it like a descriptor, in that case.
        ClassAccessVerifier.checkOptionallyDecorated(this.classWhiteList, owner);
        ClassAccessVerifier.checkDescriptor(this.classWhiteList, descriptor);
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
        ClassAccessVerifier.checkDescriptor(this.classWhiteList, descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (null != type) {
            ClassAccessVerifier.checkClassAccessible(this.classWhiteList, type);
        }
        super.visitTryCatchBlock(start, end, handler, type);
    }


    private void checkOpcode(int opcode) {
        // For now, we just reject on JSR and RET.
        if (false
                || (Opcodes.JSR == opcode)
                || (Opcodes.RET == opcode)
        ) {
            RejectedClassException.blacklistedOpcode(opcode);
        }
    }
}
