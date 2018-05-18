package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ArrayWrappingMethodAdapter extends AdviceAdapter implements Opcodes {

    private String helperClass;
    
    public ArrayWrappingMethodAdapter(final MethodVisitor mv, final int access, final String name, final String desc, String hc)
    {
        super(Opcodes.ASM6, mv, access, name, desc);
        this.helperClass = hc;
    }

    @Override
    public void visitInsn(final int opcode) {
        switch (opcode) {
            // Static type
            case Opcodes.LALOAD:
            case Opcodes.FALOAD:
            case Opcodes.DALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
            case Opcodes.IALOAD:
                this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/aion/avm/arraywrapper/IntArray", "get", "(I)I", false);
                break;

            // Generic type
            case Opcodes.AALOAD:
                break;

            case Opcodes.LASTORE:
            case Opcodes.FASTORE:
            case Opcodes.DASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
            case Opcodes.IASTORE:
                break;

            case Opcodes.AASTORE:
                break;

            case Opcodes.ARRAYLENGTH:
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
}