package org.aion.avm.tooling.shadowing.testEnum;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
public class TestEnumForJavacDump implements Opcodes {

    public static byte[] generateBytecode() {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;
        AnnotationVisitor annotationVisitor0;

        classWriter.visit(V10, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_ENUM, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues", "Ljava/lang/Enum<Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;>;", "java/lang/Enum", null);

        {
            fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC | ACC_ENUM, "TEST", "Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC | ACC_SYNTHETIC, "$VALUES", "[Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "values", "()[Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitFieldInsn(GETSTATIC, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues", "$VALUES", "[Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "[Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;", "clone", "()Ljava/lang/Object;", false);
            methodVisitor.visitTypeInsn(CHECKCAST, "[Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;");
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(1, 0);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "valueOf", "(Ljava/lang/String;)Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitLdcInsn(Type.getType("Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;"));
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
            methodVisitor.visitTypeInsn(CHECKCAST, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues");
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
            methodVisitor.visitTypeInsn(NEW, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitLdcInsn("TEST");
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues", "<init>", "(Ljava/lang/String;I)V", false);
            methodVisitor.visitFieldInsn(PUTSTATIC, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues", "TEST", "Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;");
            methodVisitor.visitInsn(ICONST_1);
            methodVisitor.visitTypeInsn(ANEWARRAY, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitFieldInsn(GETSTATIC, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues", "TEST", "Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;");
            methodVisitor.visitInsn(AASTORE);
            methodVisitor.visitFieldInsn(PUTSTATIC, "org/aion/avm/tooling/shadowing/testEnum/TestEnumForValues", "$VALUES", "[Lorg/aion/avm/tooling/shadowing/testEnum/TestEnumForValues;");
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(4, 0);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
