package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayWrapping extends ClassVisitor {

    private Logger logger = LoggerFactory.getLogger(ArrayWrapping.class);

    private String helperClass;

    public ArrayWrapping(ClassVisitor visitor, String helperClass) {
        super(Opcodes.ASM6, visitor);

        this.helperClass = helperClass;
    }

    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        logger.info("Method: access = {}, name = {}, descriptor = {}, signature = {}, exceptions = {}", access, name, descriptor, signature, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {

            @Override
            public void visitInsn(final int opcode) {
                switch (opcode) {
                    case Opcodes.IALOAD:
                        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/aion/avm/arraywrapper/IntArray", "get", "(I)I", false);
                        break;
                    default:
                        this.mv.visitInsn(opcode);
                }
            }

            @Override
            public void visitIntInsn(final int opcode, final int operand) {
                if (opcode == Opcodes.NEWARRAY) {
                    switch (operand) {
                        case Opcodes.T_BOOLEAN:
                        case Opcodes.T_BYTE:
                        case Opcodes.T_SHORT:
                        case Opcodes.T_INT:
                        case Opcodes.T_LONG:
                            // TODO: wrap based on type
                            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, helperClass, "newIntArray", "(I)Lorg/aion/avm/arraywrapper/IntArray;", false);
                    }
                }
                switch (opcode) {
                    case Opcodes.NEWARRAY:
                }
            }
        };
    }
}
