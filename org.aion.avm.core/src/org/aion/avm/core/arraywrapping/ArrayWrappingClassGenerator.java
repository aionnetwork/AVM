package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

public class ArrayWrappingClassGenerator implements Opcodes {

    public static byte[] genWrapperClass(String wName) {

        //Wrapper name always starts org.aion.avm.arraywrapper.$
        if (!wName.startsWith("org.aion.avm.arraywrapper.$")){
            return null;
        }

        String wrapper = wName.replace('.', '/');
        String tmpD = wrapper.substring("org.aion.avm.arraywrapper.".length());
        int d = 0;

        //TODO: What if user package starts with $?
        while (tmpD.charAt(d) == '$'){
            d++;
        }

//        System.out.println("*********************************");
//        System.out.println("Generating wrapper class for : " + wrapper);
//        System.out.println("Dimension is : " + d);

        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        // TODO:: array hierarchy
        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, wrapper, null, "org/aion/avm/arraywrapper/ObjectArray", null);

        if (d == 1)
        //Static factory 1D
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

        else
        //Static factory MD
        {
            String facDesc = ArrayWrappingBytecodeFactory.getFacDesc(wrapper, d);
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", facDesc, null, null);
            methodVisitor.visitCode();

            // Create new wrapper object with d0 LVT[0]
            methodVisitor.visitTypeInsn(NEW, wrapper);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);

            // Wrapper OBJ to return
            // Now LVT[0] ~ LVT[d-1] hold all dimension data, LVT[d] hold wrapper object.
            methodVisitor.visitVarInsn(ASTORE, d);

            // Initialize counter to LVT[d+1]
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ISTORE, d + 1);
            
            // For loop head label
            Label forLoopHead = new Label();
            methodVisitor.visitLabel(forLoopHead);

            // Stack map frame for for loop
            // Append [wrapper, int] to current frame
            methodVisitor.visitFrame(Opcodes.F_APPEND,2, new Object[] {wrapper, Opcodes.INTEGER}, 0, null);

            // Load counter LVT[d + 1]
            // Load current dimension LVT[0]
            methodVisitor.visitVarInsn(ILOAD, d + 1);
            methodVisitor.visitVarInsn(ILOAD, 0);

            // compare counter to current dimension
            Label forLoopTail = new Label();
            methodVisitor.visitJumpInsn(IF_ICMPGE, forLoopTail);

            // Load wrapper object LVT[d]
            methodVisitor.visitVarInsn(ALOAD, d);
            // Load counter LVT[d+1]
            methodVisitor.visitVarInsn(ILOAD, d + 1);
            // Load rest of the dimension data LVT[1] ~ LVT[d-1]
            for (int j = 1; j < d; j++) {
                methodVisitor.visitVarInsn(ILOAD, j);
            }

            // Call child wrapper factory, child wrapper will pop last d - 1 stack slot as argument.
            // Child wrapper factory descriptor can be constructed here.
            String childWrapper;
            String childFacDesc;
            childWrapper = wrapper.substring("org/aion/avm/arraywrapper/$".length());
            Assert.assertTrue(childWrapper.startsWith("$"));
            switch (childWrapper){
                case "$I":
                    childWrapper = "IntArray";
                    break;
                case "$B":
                    childWrapper = "ByteArray";
                    break;
                case "$Z":
                    childWrapper = "ByteArray";
                    break;
                case "$C":
                    childWrapper = "CharArray";
                    break;
                case "$F":
                    childWrapper = "FloatArray";
                    break;
                case "$J":
                    childWrapper = "LongArray";
                    break;
                case "$S":
                    childWrapper = "ShortArray";
                    break;
                case "$D":
                    childWrapper = "DoubleArray";
                    break;
                case "$Ljava/lang/Object":
                    childWrapper = "ObjectArray";
                    break;

            }
            childWrapper = "org/aion/avm/arraywrapper/" + childWrapper;
            childFacDesc = ArrayWrappingBytecodeFactory.getFacDesc(childWrapper, d - 1);

//            System.out.println("Child Wrapper Name: " + childWrapper);
//            System.out.println("Child init desc: " + childFacDesc);

            methodVisitor.visitMethodInsn(INVOKESTATIC, childWrapper, "initArray", childFacDesc, false);

            // Call set
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, wrapper, "set", "(ILjava/lang/Object;)V", false);

            // Increase counter LVT[d+1]
            methodVisitor.visitIincInsn(d+1, 1);

            methodVisitor.visitJumpInsn(GOTO, forLoopHead);
            methodVisitor.visitLabel(forLoopTail);

            // Chop off the counter from stack map frame
            methodVisitor.visitFrame(Opcodes.F_CHOP,1, null, 0, null);

            // Load wrapper object LVT[d]
            methodVisitor.visitVarInsn(ALOAD, d);
            methodVisitor.visitInsn(ARETURN);

            // maxStack is d + 1
            // maxLVT is d + 2
            // We can use class writer to calculate them anyway
            methodVisitor.visitMaxs(d + 1, d + 2);
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

//        System.out.println("*********************************");

        return classWriter.toByteArray();
    }
}
