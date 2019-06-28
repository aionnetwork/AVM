package org.aion.avm.tooling.shadowing.testEnum;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class TestEnumForJavacDump implements Opcodes {
    private static final String PACKAGE_NAME_INTERNAL = "org/aion/avm/tooling/shadowing/testEnum";

    public static byte[] generateBytecode() {
        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_ENUM, PACKAGE_NAME_INTERNAL + "/TestEnumForValues", "Ljava/lang/Enum<L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;>;", "java/lang/Enum", null);

        {
            fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC | ACC_ENUM, "TEST", "L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC | ACC_SYNTHETIC, "$VALUES", "[L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "values", "()[L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitFieldInsn(GETSTATIC, PACKAGE_NAME_INTERNAL + "/TestEnumForValues", "$VALUES", "[L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "[L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;", "clone", "()Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(CHECKCAST, "[L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;");
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(1, 0);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "valueOf", "(Ljava/lang/String;)L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitLdcInsn(Type.getType("L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;"));
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
            methodVisitor.visitTypeInsn(CHECKCAST, PACKAGE_NAME_INTERNAL + "/TestEnumForValues");
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", "()V", null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(3, 3);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitTypeInsn(NEW, PACKAGE_NAME_INTERNAL + "/TestEnumForValues");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitLdcInsn("TEST");
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, PACKAGE_NAME_INTERNAL + "/TestEnumForValues", "<init>", "(Ljava/lang/String;I)V", false);
            methodVisitor.visitFieldInsn(PUTSTATIC, PACKAGE_NAME_INTERNAL + "/TestEnumForValues", "TEST", "L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;");
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitTypeInsn(ANEWARRAY, PACKAGE_NAME_INTERNAL + "/TestEnumForValues");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitFieldInsn(GETSTATIC, PACKAGE_NAME_INTERNAL + "/TestEnumForValues", "TEST", "L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;");
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitFieldInsn(PUTSTATIC, PACKAGE_NAME_INTERNAL + "/TestEnumForValues", "$VALUES", "[L" + PACKAGE_NAME_INTERNAL + "/TestEnumForValues;");
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(4, 0);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
