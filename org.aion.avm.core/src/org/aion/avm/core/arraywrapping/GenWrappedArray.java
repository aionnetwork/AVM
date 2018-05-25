package org.aion.avm.core.arraywrapping;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

public class GenWrappedArray implements Opcodes {

    public static byte[] dump (String target, String wrapper) throws Exception {
        //target    [I
        //wrapper   $I
        //component  I
        String component = target.substring(1);

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        String wrapperClassName = "org/aion/avm/arraywrapper/" + wrapper;
        String wrapperDescName = "Lorg/aion/avm/arraywrapper/" + wrapper + ";";
        String wrapperFactoryDesc = "(I)Lorg/aion/avm/arraywrapper/" + wrapper + ";";
        String wrapperGetterDesc =  "(I)" + component;
        String wrapperSetterDesc =  "(I"+ component + ")V";


        System.out.println(wrapperClassName);
        System.out.println(wrapperDescName);
        System.out.println(wrapperFactoryDesc);
        System.out.println(wrapperGetterDesc);
        System.out.println(wrapperSetterDesc);
        System.out.println("********************************************");

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, wrapperClassName, null, "org/aion/avm/arraywrapper/Array", null);

        classWriter.visitSource(null, null);

        //Field
        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE, "underlying", target, null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", wrapperFactoryDesc, null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(8, label0);
            methodVisitor.visitTypeInsn(NEW, wrapperClassName);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, wrapperClassName, "<init>", "(I)V", false);
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("c", "I", null, label0, label1, 0);
            methodVisitor.visitMaxs(3, 1);
            methodVisitor.visitEnd();
        }
        //constructor
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(I)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(11, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/aion/avm/arraywrapper/Array", "<init>", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(12, label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitIntInsn(NEWARRAY, T_INT);
            methodVisitor.visitFieldInsn(PUTFIELD, wrapperClassName, "underlying", target);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(13, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", wrapperDescName, null, label0, label3, 0);
            methodVisitor.visitLocalVariable("c", "I", null, label0, label3, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        //length
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "length", "()I", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(20, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, wrapperClassName, "underlying", target);
            methodVisitor.visitInsn(ARRAYLENGTH);
            methodVisitor.visitInsn(IRETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", wrapperDescName, null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        //get
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "get", wrapperGetterDesc, null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(24, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, wrapperClassName, "underlying", target);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitInsn(IALOAD);
            methodVisitor.visitInsn(IRETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", wrapperDescName, null, label0, label1, 0);
            methodVisitor.visitLocalVariable("idx", "I", null, label0, label1, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        //set
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "set", wrapperSetterDesc, null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(28, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, wrapperClassName, "underlying", target);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitInsn(IASTORE);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(29, label1);
            methodVisitor.visitInsn(RETURN);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", wrapperDescName, null, label0, label2, 0);
            methodVisitor.visitLocalVariable("idx", "I", null, label0, label2, 1);
            methodVisitor.visitLocalVariable("val", component, null, label0, label2, 2);
            methodVisitor.visitMaxs(3, 3);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
