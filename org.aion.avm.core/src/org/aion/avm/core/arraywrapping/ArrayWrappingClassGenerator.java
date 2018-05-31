package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ArrayWrappingClassGenerator implements Opcodes {

    public static byte[] genWrapperClass(String wName) {

        //Wrapper name always starts org.aion.avm.arraywrapper.$
        if (!wName.startsWith("org.aion.avm.arraywrapper.$")){
            return null;
        }
        String wrapper = wName.replace('.', '/');
        //System.out.println("Generating wrapper class for : " + wrapper);

        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        // TODO:: multidim hier
        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, wrapper, null, "org/aion/avm/arraywrapper/ObjectArray", null);

        //Static factory
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", "(I)L" + wrapper + ";", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitTypeInsn(NEW, wrapper);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(3, 1);
            methodVisitor.visitEnd();
        }

        //Constructor
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(I)V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/aion/avm/arraywrapper/ObjectArray", "<init>", "(I)V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
