package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

import java.util.Arrays;

public class ArrayWrappingClassGenerator implements Opcodes {

    static String[] primList = {"I", "J", "Z", "B", "S", "D", "F", "C"};
    static private String HELPER = "org/aion/avm/internal/Helper";

    public static byte[] genWrapperClass(String wName) {

        //Wrapper name always starts org.aion.avm.arraywrapper.$
        if (!wName.startsWith("org.aion.avm.arraywrapper.$")){
            return null;
        }

        String wrapper = wName.replace('.', '/');
        String compName = wrapper.substring("org.aion.avm.arraywrapper.".length());
        int d = 0;

        //TODO: What if user package starts with $?
        while (compName.charAt(d) == '$'){
            d++;
        }

//        System.out.println("*********************************");
//        System.out.println("Generating wrapper class for : " + wrapper);
//        System.out.println("Dimension is : " + d);

        // Find the super class of the component type
        String superName = "org/aion/avm/arraywrapper/ObjectArray";
        compName = compName.substring(d).replace('/', '.');
        if (compName.startsWith("L")){compName = compName.substring(1);}
        if (! Arrays.asList(primList).contains(compName)) {
            Class<?> c = null;
            try {
                c = Class.forName(compName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Assert.unreachable("No valid component : " + compName);
            }

            if (!c.getName().equals("java.lang.Object")){
                c = c.getSuperclass();
                superName = (new String(new char[d]).replace("\0", "[")) + 'L' + c.getName() + ";";
                superName = superName.replace('.', '/');
                superName = ArrayWrappingBytecodeFactory.getWrapper(superName);
            }
        }

       //System.out.println("Superclass name is : " + superName);

        ClassWriter classWriter = new ClassWriter(0);

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, wrapper, null, superName, null);

        if (d == 1){
            //Static factory for one dimensional array
            genSDFac(classWriter, wrapper, d);
        }
        else{
            //Static factory for multidimensional array
            genMDFac(classWriter, wrapper, d);
        }

        //Constructor

        genConstructor(classWriter, superName);

        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private static void genSDFac(ClassWriter cw, String wrapper, int d){
        String facDesc = ArrayWrappingBytecodeFactory.getFacDesc(wrapper, d);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", facDesc, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitTypeInsn(NEW, wrapper);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ILOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);

        // Charge energy
        methodVisitor.visitVarInsn(ILOAD, 0);
        //OBJREF size is 64 per slot
        methodVisitor.visitIntInsn(BIPUSH, 64);
        methodVisitor.visitInsn(IMUL);
        methodVisitor.visitInsn(I2L);
        methodVisitor.visitMethodInsn(INVOKESTATIC, HELPER, "chargeEnergy", "(J)V", false);

        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(3, 1);
        methodVisitor.visitEnd();
    }

    private static void genMDFac(ClassWriter cw, String wrapper, int d){
        // Code template for $$$MyObject.initArray (3D array of MyObject)
        // Note that for D = n array, n dimension parameter will be passed into initArray
        //
        // public static $$$MyObj initArray(int d0, int d1, int d2){
        //    $$$MyObj ret = new $$$MyObj(d0);
        //    for (int i = 0; i < d0; i++) {
        //        ret.set(i, $$MyObj.initArray(d1, d2));
        //    }
        //    return ret;
        //}

        String facDesc = ArrayWrappingBytecodeFactory.getFacDesc(wrapper, d);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", facDesc, null, null);
        methodVisitor.visitCode();

        // Create new wrapper object with d0 LVT[0]
        methodVisitor.visitTypeInsn(NEW, wrapper);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ILOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);

        // Charge energy
        methodVisitor.visitVarInsn(ILOAD, 0);
        //OBJREF size is 64 per slot
        methodVisitor.visitIntInsn(BIPUSH, 64);
        methodVisitor.visitInsn(IMUL);
        methodVisitor.visitInsn(I2L);
        methodVisitor.visitMethodInsn(INVOKESTATIC, HELPER, "chargeEnergy", "(J)V", false);

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
        // Child wrapper factory descriptor will be constructed here.
        String childWrapper;
        String childFacDesc;
        childWrapper = wrapper.substring("org/aion/avm/arraywrapper/$".length());
        Assert.assertTrue(childWrapper.startsWith("$"));

        // If child is predefined array in rt, replace them.
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

    private static void genConstructor(ClassWriter cw, String superName){
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, "<init>", "(I)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ILOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "(I)V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
    }
}
