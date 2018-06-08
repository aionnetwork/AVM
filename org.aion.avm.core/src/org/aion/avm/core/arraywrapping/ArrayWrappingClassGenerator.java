package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrayWrappingClassGenerator implements Opcodes {
    private static boolean DEBUG = false;

    static private String[] PRIMS = {"I", "J", "Z", "B", "S", "D", "F", "C"};
    static private String HELPER = "org/aion/avm/internal/Helper";
    static private HashMap<String, String> WRAPPER_MAP = new HashMap<>();

    static{
        WRAPPER_MAP.put("[I", "org/aion/avm/arraywrapper/IntArray");
        WRAPPER_MAP.put("[B", "org/aion/avm/arraywrapper/ByteArray");
        WRAPPER_MAP.put("[Z", "org/aion/avm/arraywrapper/ByteArray");
        WRAPPER_MAP.put("[C", "org/aion/avm/arraywrapper/CharArray");
        WRAPPER_MAP.put("[F", "org/aion/avm/arraywrapper/FloatArray");
        WRAPPER_MAP.put("[S", "org/aion/avm/arraywrapper/ShortArray");
        WRAPPER_MAP.put("[J", "org/aion/avm/arraywrapper/LongArray");
        WRAPPER_MAP.put("[D", "org/aion/avm/arraywrapper/DoubleArray");
        WRAPPER_MAP.put("[Ljava/lang/Object", "org/aion/avm/arraywrapper/ObjectArray");
    }

    public static byte[] genWrapperClass(String wName) {

        //Wrapper name always starts org.aion.avm.arraywrapper.$
        if (!wName.startsWith("org.aion.avm.arraywrapper.$")){
            return null;
        }

        String wrapper = wName.replace('.', '/');
        String compName = wrapper.substring("org.aion.avm.arraywrapper.".length());
        int d = 0;

        //TODO: What if user package starts with $?
        while (compName.charAt(d) == '$') {
            d++;
        }

        // Find the super class of the component type
        String superName = "org/aion/avm/arraywrapper/ObjectArray";
        compName = compName.substring(d).replace('/', '.');
        if (compName.startsWith("L")){compName = compName.substring(1);}
        if (! Arrays.asList(PRIMS).contains(compName)) {
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
                superName = ArrayWrappingClassGenerator.getWrapper(superName);
            }
        }

        if (DEBUG) {
            System.out.println("*********************************");
            System.out.println("Generating class : " + wrapper);
            System.out.println("Superclass class : " + superName);
            System.out.println("Wrapper Dimension : " + d);
            System.out.println("*********************************");
        }

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
        String facDesc = ArrayWrappingClassGenerator.getFacDesc(wrapper, d);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", facDesc, null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, wrapper);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);

        // Charge energy
        mv.visitVarInsn(ILOAD, 0);
        // OBJREF size is 64 per slot
        mv.visitIntInsn(BIPUSH, 64);
        mv.visitInsn(IMUL);
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, HELPER, "chargeEnergy", "(J)V", false);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
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
        // }

        String facDesc = ArrayWrappingClassGenerator.getFacDesc(wrapper, d);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", facDesc, null, null);
        mv.visitCode();

        // Create new wrapper object with d0 LVT[0]
        mv.visitTypeInsn(NEW, wrapper);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);

        // Charge energy
        mv.visitVarInsn(ILOAD, 0);
        //OBJREF size is 64 per slot
        mv.visitIntInsn(BIPUSH, 64);
        mv.visitInsn(IMUL);
        mv.visitInsn(I2L);
        mv.visitMethodInsn(INVOKESTATIC, HELPER, "chargeEnergy", "(J)V", false);

        // Wrapper OBJ to return
        // Now LVT[0] ~ LVT[d-1] hold all dimension data, LVT[d] hold wrapper object.
        mv.visitVarInsn(ASTORE, d);

        // Initialize counter to LVT[d+1]
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, d + 1);

        // For loop head label
        Label forLoopHead = new Label();
        mv.visitLabel(forLoopHead);

        // Stack map frame for for loop
        // Append [wrapper, int] to current frame
        mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {wrapper, Opcodes.INTEGER}, 0, null);

        // Load counter LVT[d + 1]
        // Load current dimension LVT[0]
        mv.visitVarInsn(ILOAD, d + 1);
        mv.visitVarInsn(ILOAD, 0);

        // compare counter to current dimension
        Label forLoopTail = new Label();
        mv.visitJumpInsn(IF_ICMPGE, forLoopTail);

        // Load wrapper object LVT[d]
        mv.visitVarInsn(ALOAD, d);
        // Load counter LVT[d+1]
        mv.visitVarInsn(ILOAD, d + 1);
        // Load rest of the dimension data LVT[1] ~ LVT[d-1]
        for (int j = 1; j < d; j++) {
            mv.visitVarInsn(ILOAD, j);
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
        childFacDesc = ArrayWrappingClassGenerator.getFacDesc(childWrapper, d - 1);

        mv.visitMethodInsn(INVOKESTATIC, childWrapper, "initArray", childFacDesc, false);

        // Call set
        mv.visitMethodInsn(INVOKEVIRTUAL, wrapper, "set", "(ILjava/lang/Object;)V", false);

        // Increase counter LVT[d+1]
        mv.visitIincInsn(d+1, 1);

        mv.visitJumpInsn(GOTO, forLoopHead);
        mv.visitLabel(forLoopTail);

        // Chop off the counter from stack map frame
        mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);

        // Load wrapper object LVT[d]
        mv.visitVarInsn(ALOAD, d);
        mv.visitInsn(ARETURN);

        // maxStack is d + 1
        // maxLVT is d + 2
        // We can use class writer to calculate them anyway
        mv.visitMaxs(d + 1, d + 2);
        mv.visitEnd();
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

    static java.lang.String updateMethodDesc(java.lang.String desc) {
        //\[*L[^;]+;|\[[ZBCSIFDJ]|[ZBCSIFDJ]
        StringBuilder sb = new StringBuilder();
        java.lang.String wrapperName;
        java.lang.String cur;

        int beginIndex = desc.indexOf('(');
        int endIndex = desc.lastIndexOf(')');

        // Method descriptor has to contain () pair
        if(beginIndex == -1 || endIndex == -1) {
            System.err.println(beginIndex);
            System.err.println(endIndex);
            throw new RuntimeException();
        }
        sb.append(desc, 0, beginIndex);
        sb.append('(');

        // Parse param
        java.lang.String para = desc.substring(beginIndex + 1, endIndex);
        Pattern pattern = Pattern.compile("\\[*L[^;]+;|\\[[ZBCSIFDJ]|[ZBCSIFDJ]");
        Matcher paraMatcher = pattern.matcher(para);

        while(paraMatcher.find())
        {
            cur = paraMatcher.group();
            if(cur.startsWith("[")) {
                cur = "L" + getWrapper(cur) + ";";
            }
            sb.append(cur);
        }
        sb.append(')');

        // Parse return type if there is any
        if (endIndex < (desc.length() - 1)){
            java.lang.String ret = desc.substring(endIndex + 1);
            if (ret.equals("V")){
                sb.append(ret);
            }
            Matcher retMatcher = pattern.matcher(ret);
            if (retMatcher.find()){
                cur = retMatcher.group();
                if(cur.startsWith("[")) {
                    cur = "L" + getWrapper(cur) + ";";
                }
                sb.append(cur);
            }
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }

    // Return the wrapper descriptor of an array
    static java.lang.String getWrapper(java.lang.String desc){
        if (desc.endsWith(";")){
            desc = desc.substring(0, desc.length() - 1);
        }

        java.lang.String ret;
        if (desc.charAt(0) != '['){
            ret = desc;
        }else if (WRAPPER_MAP.containsKey(desc)){
            ret = WRAPPER_MAP.get(desc);
        }else{
            WRAPPER_MAP.put(desc, newWrapper(desc));
            ret = WRAPPER_MAP.get(desc);
        }

        //System.out.println("Wrapper name : " + ret);
        return ret;
    }

    // Return the wrapper descriptor of an array
    static java.lang.String getFacDesc(java.lang.String wrapper, int d){
        String facDesc = new String(new char[d]).replace("\0", "I");
        facDesc = "(" + facDesc + ")L" + wrapper + ";";
        return facDesc;
    }


    //TODO:: is this enough?
    private static java.lang.String newWrapper(java.lang.String desc){
        //System.out.println(desc);
        StringBuilder sb = new StringBuilder();
        sb.append("org/aion/avm/arraywrapper/");

        //Check if the desc is a ref array
        if((desc.charAt(1) == 'L') || (desc.charAt(1) == '[')){
            sb.append(getRefWrapperName(desc));
        }else{
            Assert.unreachable("genWrapperName :" + desc);
        }

        return sb.toString();
    }

    // Return the element descriptor of an array
    private static java.lang.String getRefWrapperName(java.lang.String desc){
        return desc.replace('[', '$');
    }


    // Return the element type of an array
    // 1D Primitive array will not be called with this method since there will be no aaload
    static java.lang.String getElementType(java.lang.String desc){

        Assert.assertTrue(desc.startsWith("["));
        String ret = desc.substring(1);

        if (ret.startsWith("L")){
            ret = ret.substring(1, ret.length() - 1);
        }

        return ret;
    }
}
