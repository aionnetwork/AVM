package org.aion.avm.core.arraywrapping;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.core.util.Assert;
import org.aion.avm.internal.PackageConstants;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

class ArrayWrappingMethodAdapter extends AdviceAdapter implements Opcodes {

    private Type typeA = Type.getType(org.aion.avm.arraywrapper.Array.class);
    private Type typeBA = Type.getType(ByteArray.class);
    private Type typeCA = Type.getType(CharArray.class);
    private Type typeDA = Type.getType(DoubleArray.class);
    private Type typeFA = Type.getType(FloatArray.class);
    private Type typeIA = Type.getType(IntArray.class);
    private Type typeLA = Type.getType(LongArray.class);
    private Type typeSA = Type.getType(ShortArray.class);


    ArrayWrappingMethodAdapter(final MethodVisitor mv, final int access, final String name, final String desc)
    {
        super(Opcodes.ASM6, mv, access, name, desc);
    }

    @Override
    public void visitInsn(final int opcode) {

        Method m;

        switch (opcode) {
            // Static type
            case Opcodes.BALOAD:
                m = Method.getMethod("byte get(int)");
                invokeVirtual(typeBA, m);
                break;
            case Opcodes.CALOAD:
                m = Method.getMethod("char get(int)");
                invokeVirtual(typeCA, m);
                break;
            case Opcodes.DALOAD:
                m = Method.getMethod("double get(int)");
                invokeVirtual(typeDA, m);
                break;
            case Opcodes.FALOAD:
                m = Method.getMethod("float get(int)");
                invokeVirtual(typeFA, m);
                break;
            case Opcodes.IALOAD:
                m = Method.getMethod("int get(int)");
                invokeVirtual(typeIA, m);
                break;
            case Opcodes.LALOAD:
                m = Method.getMethod("long get(int)");
                invokeVirtual(typeLA, m);
                break;
            case Opcodes.SALOAD:
                m = Method.getMethod("short get(int)");
                invokeVirtual(typeSA, m);
                break;
            case Opcodes.BASTORE:
                m = Method.getMethod("void set(int, byte)");
                invokeVirtual(typeBA, m);
                break;
            case Opcodes.CASTORE:
                m = Method.getMethod("void set(int, char)");
                invokeVirtual(typeCA, m);
                break;
            case Opcodes.DASTORE:
                m = Method.getMethod("void set(int, double)");
                invokeVirtual(typeDA, m);
                break;
            case Opcodes.FASTORE:
                m = Method.getMethod("void set(int, float)");
                invokeVirtual(typeFA, m);
                break;
            case Opcodes.IASTORE:
                m = Method.getMethod("void set(int, int)");
                invokeVirtual(typeIA, m);
                break;
            case Opcodes.LASTORE:
                m = Method.getMethod("void set(int, long)");
                invokeVirtual(typeLA, m);
                break;
            case Opcodes.SASTORE:
                m = Method.getMethod("void set(int, short)");
                invokeVirtual(typeSA, m);
                break;
            case Opcodes.ARRAYLENGTH:
                m = Method.getMethod("int length()");
                invokeVirtual(typeA, m);
                break;

            case Opcodes.AALOAD:
                Assert.unreachable("Primitive array wrapping adapter catch AALOAD");
            case Opcodes.AASTORE:
                Assert.unreachable("Primitive array wrapping adapter catch AASTORE");

            default:
                this.mv.visitInsn(opcode);
        }
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        Method m;

        if (opcode == Opcodes.NEWARRAY) {
            switch (operand) {
                case Opcodes.T_BOOLEAN:
                case Opcodes.T_BYTE:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "ByteArray initArray(int)");
                    invokeStatic(typeBA, m);
                    break;
                case Opcodes.T_SHORT:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "ShortArray initArray(int)");
                    invokeStatic(typeSA, m);
                    break;
                case Opcodes.T_INT:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "IntArray initArray(int)");
                    invokeStatic(typeIA, m);
                    break;
                case Opcodes.T_LONG:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "LongArray initArray(int)");
                    invokeStatic(typeLA, m);
                    break;
                case Opcodes.T_CHAR:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "CharArray initArray(int)");
                    invokeStatic(typeCA, m);
                    break;
                case Opcodes.T_FLOAT:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "FloatArray initArray(int)");
                    invokeStatic(typeFA, m);
                    break;
                case Opcodes.T_DOUBLE:
                    m = Method.getMethod(PackageConstants.kArrayWrapperDotPrefix + "DoubleArray initArray(int)");
                    invokeStatic(typeDA, m);
                    break;
                default:
                    this.mv.visitIntInsn(opcode, operand);
            }
        }else{
            this.mv.visitIntInsn(opcode, operand);
        }
    }

    @Override
    public void visitTypeInsn(int opcode, java.lang.String type){

        String wName;

        switch(opcode){
            case Opcodes.ANEWARRAY:
                if (type.startsWith("[")){
                    wName = ArrayWrappingClassGenerator.getClassWrapper("[" + type);
                }else{
                    wName = ArrayWrappingClassGenerator.getClassWrapper("[L" + type);
                }

                this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, wName, "initArray", "(I)L" + wName + ";", false);
                break;

            case Opcodes.CHECKCAST:
            case Opcodes.INSTANCEOF:
                wName = type;
                if (type.startsWith("[")) {
                    wName = ArrayWrappingClassGenerator.getClassWrapper(type);
                }
                this.mv.visitTypeInsn(opcode, wName);
                break;
            default:
                this.mv.visitTypeInsn(opcode, type);
        }
    }

    @Override
    //TODO: invokedynamic?
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        String desc = ArrayWrappingClassGenerator.updateMethodDesc(descriptor);
        String newOwner = ArrayWrappingClassGenerator.getClassWrapper(owner);
        this.mv.visitMethodInsn(opcode, newOwner, name, desc, isInterface);
    }

    @Override
    public void visitLocalVariable(java.lang.String name,
                               java.lang.String descriptor,
                               java.lang.String signature,
                               Label start,
                               Label end,
                               int index)
    {
        String desc = descriptor;
        if (descriptor.startsWith("[")) {
            desc = "L" + ArrayWrappingClassGenerator.getClassWrapper(descriptor) + ";";
        }

        this.mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitFieldInsn(int opcode,
                           java.lang.String owner,
                           java.lang.String name,
                           java.lang.String descriptor)
    {
        String desc = descriptor;
        if (descriptor.startsWith("[")) {
            desc = "L" + ArrayWrappingClassGenerator.getClassWrapper(descriptor) + ";";
        }

        this.mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMultiANewArrayInsn(java.lang.String descriptor, int d)
    {
        int sd = ArrayWrappingClassGenerator.getDimension(descriptor);
        while (d < sd){
            this.mv.visitIntInsn(Opcodes.BIPUSH, 0);
            d++;
        }
        String wName = ArrayWrappingClassGenerator.getClassWrapper(descriptor);
        String facDesc = ArrayWrappingClassGenerator.getFacDesc(wName, sd);

        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, wName, "initArray", facDesc, false);
    }


}
