package org.aion.avm.core.arraywrapping;

import org.aion.avm.arraywrapper.ArrayElement;
import org.aion.avm.core.util.DescriptorParser;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ArrayWrappingClassGenerator implements Opcodes {
    static private boolean DEBUG = false;
    static private boolean ENERGY_METERING = true;

    static private String SHADOW_ARRAY = PackageConstants.kArrayWrapperSlashPrefix + "Array";

    static private Set<String> PRIMITIVES = Stream.of("I", "J", "Z", "B", "S", "D", "F", "C").collect(Collectors.toSet());
    static private HashMap<String, String> CLASS_WRAPPER_MAP = new HashMap<>();
    static private HashMap<String, String> INTERFACE_WRAPPER_MAP = new HashMap<>();

    static{
        CLASS_WRAPPER_MAP.put("[I", PackageConstants.kArrayWrapperSlashPrefix + "IntArray");
        CLASS_WRAPPER_MAP.put("[B", PackageConstants.kArrayWrapperSlashPrefix + "ByteArray");
        CLASS_WRAPPER_MAP.put("[Z", PackageConstants.kArrayWrapperSlashPrefix + "ByteArray");
        CLASS_WRAPPER_MAP.put("[C", PackageConstants.kArrayWrapperSlashPrefix + "CharArray");
        CLASS_WRAPPER_MAP.put("[F", PackageConstants.kArrayWrapperSlashPrefix + "FloatArray");
        CLASS_WRAPPER_MAP.put("[S", PackageConstants.kArrayWrapperSlashPrefix + "ShortArray");
        CLASS_WRAPPER_MAP.put("[J", PackageConstants.kArrayWrapperSlashPrefix + "LongArray");
        CLASS_WRAPPER_MAP.put("[D", PackageConstants.kArrayWrapperSlashPrefix + "DoubleArray");
        CLASS_WRAPPER_MAP.put("[Ljava/lang/Object", PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray");
        CLASS_WRAPPER_MAP.put("[L" + PackageConstants.kShadowSlashPrefix + "java/lang/Object", PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray");
        CLASS_WRAPPER_MAP.put("[L" + PackageConstants.kInternalSlashPrefix + "IObject", PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray");

        CLASS_WRAPPER_MAP.put("[[I", PackageConstants.kArrayWrapperSlashPrefix + "IntArray2D");
        CLASS_WRAPPER_MAP.put("[[B", PackageConstants.kArrayWrapperSlashPrefix + "ByteArray2D");
        CLASS_WRAPPER_MAP.put("[[Z", PackageConstants.kArrayWrapperSlashPrefix + "ByteArray2D");
        CLASS_WRAPPER_MAP.put("[[C", PackageConstants.kArrayWrapperSlashPrefix + "CharArray2D");
        CLASS_WRAPPER_MAP.put("[[F", PackageConstants.kArrayWrapperSlashPrefix + "FloatArray2D");
        CLASS_WRAPPER_MAP.put("[[S", PackageConstants.kArrayWrapperSlashPrefix + "ShortArray2D");
        CLASS_WRAPPER_MAP.put("[[J", PackageConstants.kArrayWrapperSlashPrefix + "LongArray2D");
        CLASS_WRAPPER_MAP.put("[[D", PackageConstants.kArrayWrapperSlashPrefix + "DoubleArray2D");

    }

    public static byte[] arrayWrappingFactory(String request, ClassLoader loader){

        if (request.startsWith(PackageConstants.kArrayWrapperDotPrefix + "interface._")){
            return genWrapperInterface(request, loader);
        }

        // we only handle class generation request prefixed with org.aion.avm.arraywrapper.$
        if (request.startsWith(PackageConstants.kArrayWrapperDotPrefix + "$")){
            return genWrapperClass(request, loader);
        }

        return null;
    }

    private static byte[] genWrapperInterface(String requestInterface, ClassLoader loader) {

        if (DEBUG) {
            System.out.println("*********************************");
            System.out.println("requestInterface : " + requestInterface);
        }

        String wrapperInterfaceSlashName = Helpers.fulllyQualifiedNameToInternalName(requestInterface);
        // Get element class and array dim
        String elementInterfaceSlashName = wrapperInterfaceSlashName.substring((PackageConstants.kArrayWrapperSlashPrefix + "interface/").length());
        int dim = getPrefixSize(elementInterfaceSlashName, '_');
        String elementInterfaceDotName = Helpers.internalNameToFulllyQualifiedName(elementInterfaceSlashName.substring(dim));
        if (elementInterfaceDotName.startsWith("L")){elementInterfaceDotName = elementInterfaceDotName.substring(1);}

        Class<?> elementClass = null;
        try {
            elementClass = loader.loadClass(elementInterfaceDotName);
        } catch (ClassNotFoundException e) {
            throw RuntimeAssertionError.unreachable("No valid component : " + elementInterfaceDotName);
        }

        Class<?>[] superInterfaceClasses =  elementClass.getInterfaces();
        String[] superInterfaces = new String[superInterfaceClasses.length];
        int i = 0;
        for (Class<?> curI : superInterfaceClasses){
            String superInterfaceDotName = buildArrayDescriptor(dim, typeDescriptorForClass(curI));
            String superInterfaceSlashName = Helpers.fulllyQualifiedNameToInternalName(superInterfaceDotName);
            String superInterfaceWrapperSlashName = ArrayWrappingClassGenerator.getInterfaceWrapper(superInterfaceSlashName);
            superInterfaces[i++] = superInterfaceWrapperSlashName;
        }

        if (DEBUG) {
            System.out.println("Generating interface : " + wrapperInterfaceSlashName);
            for (String s : superInterfaces) {
                System.out.println("Interfaces : " + s);
            }
            System.out.println("Wrapper Dimension : " + dim);
            System.out.println("*********************************");
        }

        return generateInterfaceBytecode(wrapperInterfaceSlashName, superInterfaces);

    }

    private static byte[] generateInterfaceBytecode(String wrapperInterfaceSlashName, String[] superInterfaces) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V10, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE , wrapperInterfaceSlashName, null, "java/lang/Object", superInterfaces);
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private static byte[] genWrapperClass(String requestClass, ClassLoader loader) {
        if (DEBUG) {
            System.out.println("*********************************");
            System.out.println("requestClass : " + requestClass);
        }

        // Class name in bytecode
        String wrapperClassSlashName = Helpers.fulllyQualifiedNameToInternalName(requestClass);

        // Get element class and array dim
        String elementClassSlashName = wrapperClassSlashName.substring(PackageConstants.kArrayWrapperDotPrefix.length());
        int dim = getPrefixSize(elementClassSlashName, '$');
        String elementClassDotName = Helpers.internalNameToFulllyQualifiedName(elementClassSlashName.substring(dim));
        if (elementClassDotName.startsWith("L")){elementClassDotName = elementClassDotName.substring(1);}

        // Default super class is ObjectArray

        byte[] bytecode = null;
        // If element is not primitive type, we need to find its super class
        if (!PRIMITIVES.contains(elementClassDotName)) {
            // Element is NOT primitive.
            Class<?> elementClass = null;
            try {
                elementClass = loader.loadClass(elementClassDotName);
            } catch (ClassNotFoundException e) {
                throw RuntimeAssertionError.unreachable("No valid component : " + elementClassDotName);
            }

            Class<?>[] superInterfaceClasses =  elementClass.getInterfaces();
            String[] superInterfaces = new String[superInterfaceClasses.length];
            if (elementClass.isInterface()){superInterfaces = new String[superInterfaceClasses.length + 1];}

            int i = 0;
            for (Class<?> curI : superInterfaceClasses){
                String superInterfaceDotName = buildArrayDescriptor(dim, typeDescriptorForClass(curI));
                String superInterfaceSlashName = Helpers.fulllyQualifiedNameToInternalName(superInterfaceDotName);
                String superInterfaceWrapperSlashName = ArrayWrappingClassGenerator.getInterfaceWrapper(superInterfaceSlashName);
                superInterfaces[i++] = superInterfaceWrapperSlashName;
            }

            // Element is an interface
            if (elementClass.isInterface()){
                String curInterfaceDotName = buildArrayDescriptor(dim, typeDescriptorForClass(elementClass));
                String curInterfaceSlashName = Helpers.fulllyQualifiedNameToInternalName(curInterfaceDotName);
                String curInterfaceWrapperSlashName = ArrayWrappingClassGenerator.getInterfaceWrapper(curInterfaceSlashName);
                superInterfaces[i++] = curInterfaceWrapperSlashName;

                // Generate
                bytecode = generateClassBytecode(wrapperClassSlashName, PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray", dim, superInterfaces);
                if (DEBUG) {
                    System.out.println("Generating Interface wrapper class : " + wrapperClassSlashName);
                    System.out.println("Wrapper Dimension : " + dim);
                    for (String s : superInterfaces) {
                        System.out.println("Interfaces : " + s);
                    }
                    System.out.println("*********************************");
                }

            }
            // Element is a class
            else{
                String superClassSlashName = PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray";
                if (!elementClass.getName().equals("java.lang.Object")) {
                    Class<?> elementSuperClass = elementClass.getSuperclass();
                    String superClassDotName = buildArrayDescriptor(dim, typeDescriptorForClass(elementSuperClass));
                    String slashName = Helpers.fulllyQualifiedNameToInternalName(superClassDotName);
                    superClassSlashName = ArrayWrappingClassGenerator.getClassWrapper(slashName);
                }
                bytecode = generateClassBytecode(wrapperClassSlashName, superClassSlashName, dim, superInterfaces);

                if (DEBUG) {
                    System.out.println("Generating class : " + wrapperClassSlashName);
                    System.out.println("Superclass class : " + superClassSlashName);
                    for (String s : superInterfaces) {
                        System.out.println("Interfaces : " + s);
                    }
                    System.out.println("Wrapper Dimension : " + dim);
                    System.out.println("*********************************");
                }
            }
        }else{
            // Element IS primitive
            bytecode = generateClassBytecode(wrapperClassSlashName, PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray", dim, null);
            if (DEBUG) {
                System.out.println("Generating Prim Class : " + wrapperClassSlashName);
                System.out.println("Wrapper Dimension : " + dim);
                System.out.println("*********************************");
            }
        }
        
        // If this is null, an incomplete code path was added.
        RuntimeAssertionError.assertTrue(null != bytecode);
        return bytecode;
    }

    private static byte[] generateClassBytecode(String wrapperClassSlashName, String superClassSlashName, int dimensions, String[] superInterfaceSlashNames){
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, wrapperClassSlashName, null, superClassSlashName, superInterfaceSlashNames);
        // Static factory for one dimensional array
        // We always generate one D factory for corner case like int[][][][] a = new int[10][][][];
        genSingleDimensionFactory(classWriter, wrapperClassSlashName, 1);

        if (dimensions > 1) {
            //Static factory for multidimensional array
            genMultiDimensionFactory(classWriter, wrapperClassSlashName, dimensions);
        }

        //Constructor
        genConstructor(classWriter, superClassSlashName);

        //Clone
        genClone(classWriter, wrapperClassSlashName);

        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private static void genSingleDimensionFactory(ClassWriter cw, String wrapper, int d){
        String facDesc = ArrayWrappingClassGenerator.getFactoryDescriptor(wrapper, d);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", facDesc, null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, wrapper);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);

        if (ENERGY_METERING) {
            // Charge energy
            mv.visitVarInsn(ILOAD, 0);
            mv.visitIntInsn(BIPUSH, (int) ArrayElement.REF.getEnergy());
            mv.visitInsn(IMUL);
            mv.visitInsn(I2L);
            mv.visitMethodInsn(INVOKESTATIC, SHADOW_ARRAY, "chargeEnergy", "(J)V", false);
        }

        mv.visitInsn(ARETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
    }

    private static void genMultiDimensionFactory(ClassWriter cw, String wrapper, int d){
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

        String facDesc = ArrayWrappingClassGenerator.getFactoryDescriptor(wrapper, d);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "initArray", facDesc, null, null);
        mv.visitCode();

        // Create new wrapper object with d0 LVT[0]
        mv.visitTypeInsn(NEW, wrapper);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "(I)V", false);

        if (ENERGY_METERING) {
            // Charge energy
            mv.visitVarInsn(ILOAD, 0);
            mv.visitIntInsn(BIPUSH, (int) ArrayElement.REF.getEnergy());
            mv.visitInsn(IMUL);
            mv.visitInsn(I2L);
            mv.visitMethodInsn(INVOKESTATIC, SHADOW_ARRAY, "chargeEnergy", "(J)V", false);
        }

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
        childWrapper = wrapper.substring((PackageConstants.kArrayWrapperSlashPrefix + "$").length());
        RuntimeAssertionError.assertTrue(childWrapper.startsWith("$"));
        char[] childArray = childWrapper.toCharArray();
        for(int i = 0; childArray[i] == '$' ; i++){
            childArray[i] = '[';
        }
        childWrapper = new String(childArray);
        childWrapper = getClassWrapper(childWrapper);
        childFacDesc = ArrayWrappingClassGenerator.getFactoryDescriptor(childWrapper, d - 1);

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
        String initName = "<init>";
        
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, initName, "(I)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ILOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, superName, initName, "(I)V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();

        methodVisitor = cw.visitMethod(ACC_PUBLIC, initName, "([Ljava/lang/Object;)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, superName, initName, "()V", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(PUTFIELD, PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray", "underlying", "[Ljava/lang/Object;");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();

        methodVisitor = cw.visitMethod(ACC_PUBLIC, initName, "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, superName, initName, "()V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();

        // Create the deserialization constructor (as seen in AutomaticGraphVisitor).
        String deserializationConstructorDescriptor = "(Lorg/aion/avm/internal/IDeserializer;J)V";
        methodVisitor = cw.visitMethod(Opcodes.ACC_PUBLIC, initName, deserializationConstructorDescriptor, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitVarInsn(Opcodes.LLOAD, 2);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, initName, deserializationConstructorDescriptor, false);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(4, 4);
        methodVisitor.visitEnd();
    }

    private static void genClone(ClassWriter cw, String wrapper) {
        String cloneMethodName = "avm_clone";
        String cloneMethodDesc = "()Lorg/aion/avm/internal/IObject;";
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PUBLIC, cloneMethodName, cloneMethodDesc, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitTypeInsn(NEW, wrapper);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, wrapper, "underlying", "[Ljava/lang/Object;");
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, wrapper, "underlying", "[Ljava/lang/Object;");
        methodVisitor.visitInsn(ARRAYLENGTH);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "copyOf", "([Ljava/lang/Object;I)[Ljava/lang/Object;", false);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, wrapper, "<init>", "([Ljava/lang/Object;)V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(4, 1);
        methodVisitor.visitEnd();
    }

    static java.lang.String updateMethodDesc(java.lang.String desc) {
        return mapDescriptor(desc);
    }

    // Return the wrapper descriptor of an array
    public static java.lang.String getClassWrapper(java.lang.String desc){
        if (desc.endsWith(";")){
            desc = desc.substring(0, desc.length() - 1);
        }

        java.lang.String ret;
        if (desc.charAt(0) != '['){
            ret = desc;
        }else if (CLASS_WRAPPER_MAP.containsKey(desc)){
            ret = CLASS_WRAPPER_MAP.get(desc);
        }else{
            CLASS_WRAPPER_MAP.put(desc, newClassWrapper(desc));
            ret = CLASS_WRAPPER_MAP.get(desc);
        }
        return ret;
    }

    private static java.lang.String getObjectArrayWrapper(java.lang.String type, int dim){
        return getClassWrapper(buildArrayDescriptor(dim, 'L' + type + ';'));
    }

    private static java.lang.String getByteArrayWrapper(int dim){
        return getClassWrapper(buildArrayDescriptor(dim, "B"));
    }

    private static java.lang.String getCharArrayWrapper(int dim){
        return getClassWrapper(buildArrayDescriptor(dim, "C"));
    }

    private static java.lang.String getIntArrayWrapper(int dim){
        return getClassWrapper(buildArrayDescriptor(dim, "I"));
    }

    private static java.lang.String getDoubleArrayWrapper(int dim){
        return getClassWrapper(buildArrayDescriptor(dim, "D"));
    }

    private static java.lang.String getFloatArrayWrapper(int dim){
        return getClassWrapper(buildArrayDescriptor(dim, "F"));
    }

    private static java.lang.String getLongArrayWrapper(int dim){
        return getClassWrapper(buildArrayDescriptor(dim, "J"));
    }

    private static java.lang.String getShortArrayWrapper(int dim){
        return getClassWrapper(buildArrayDescriptor(dim, "S"));
    }

    // Return the wrapper descriptor of an array
    static java.lang.String getInterfaceWrapper(java.lang.String desc){
        if (desc.endsWith(";")){
            desc = desc.substring(0, desc.length() - 1);
        }

        java.lang.String ret;
        if (desc.charAt(0) != '['){
            ret = desc;
        }else if (INTERFACE_WRAPPER_MAP.containsKey(desc)){
            ret = INTERFACE_WRAPPER_MAP.get(desc);
        }else{
            INTERFACE_WRAPPER_MAP.put(desc, newInterfaceWrapper(desc));
            ret = INTERFACE_WRAPPER_MAP.get(desc);
        }

        return ret;
    }

    // Return the wrapper descriptor of an array
    static java.lang.String getFactoryDescriptor(java.lang.String wrapper, int d){
        String facDesc = buildFullString(d, 'I');
        facDesc = "(" + facDesc + ")L" + wrapper + ";";
        return facDesc;
    }

    private static java.lang.String newClassWrapper(java.lang.String desc){
        //System.out.println(desc);
        StringBuilder sb = new StringBuilder();
        sb.append(PackageConstants.kArrayWrapperSlashPrefix);

        //Check if the desc is a ref array
        if((desc.charAt(1) == 'L') || (desc.charAt(1) == '[')){
            sb.append(desc.replace('[', '$'));
        }else{
            throw RuntimeAssertionError.unreachable("newClassWrapper: " + desc);
        }

        return sb.toString();
    }

    private static java.lang.String newInterfaceWrapper(java.lang.String desc){
        //System.out.println(desc);
        StringBuilder sb = new StringBuilder();
        sb.append(PackageConstants.kArrayWrapperSlashPrefix + "interface/");

        //Check if the desc is a ref array
        if((desc.charAt(1) == 'L') || (desc.charAt(1) == '[')){
            sb.append(desc.replace('[', '_'));
        }else{
            throw RuntimeAssertionError.unreachable("newInterfaceWrapper :" + desc);
        }

        return sb.toString();
    }

    public static int getDimension(java.lang.String desc){
        // In this case, we are using the '[' as a prefix.
        return getPrefixSize(desc, '[');
    }

    public static int getPrefixSize(String input, char prefixChar) {
        int d = 0;
        while (input.charAt(d) == prefixChar) {
            d++;
        }
        return d;
    }


    // Return the element type of an array
    // 1D Primitive array will not be called with this method since there will be no aaload
    static java.lang.String getElementType(java.lang.String desc){

        RuntimeAssertionError.assertTrue(desc.startsWith("["));
        String ret = desc.substring(1);

        if (ret.startsWith("L")){
            ret = ret.substring(1, ret.length() - 1);
        }

        return ret;
    }

    private static String mapDescriptor(String descriptor) {
        StringBuilder builder = DescriptorParser.parse(descriptor, new DescriptorParser.Callbacks<>() {
            @Override
            public StringBuilder readObject(int arrayDimensions, String type, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                }
                userData.append(getObjectArrayWrapper(type, arrayDimensions));
                userData.append(DescriptorParser.OBJECT_END);
                return userData;
            }

            @Override
            public StringBuilder readBoolean(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getByteArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else {
                    userData.append(DescriptorParser.BOOLEAN);
                }
                return userData;
            }

            @Override
            public StringBuilder readShort(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getShortArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else{
                    userData.append(DescriptorParser.SHORT);
                }
                return userData;
            }

            @Override
            public StringBuilder readLong(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getLongArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else{
                    userData.append(DescriptorParser.LONG);
                }
                return userData;
            }

            @Override
            public StringBuilder readInteger(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getIntArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else{
                    userData.append(DescriptorParser.INTEGER);
                }
                return userData;
            }

            @Override
            public StringBuilder readFloat(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getFloatArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else{
                    userData.append(DescriptorParser.FLOAT);
                }

                return userData;
            }

            @Override
            public StringBuilder readDouble(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getDoubleArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else{
                    userData.append(DescriptorParser.DOUBLE);
                }
                return userData;
            }

            @Override
            public StringBuilder readChar(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getCharArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else{
                    userData.append(DescriptorParser.CHAR);
                }
                return userData;
            }

            @Override
            public StringBuilder readByte(int arrayDimensions, StringBuilder userData) {
                if (arrayDimensions > 0) {
                    userData.append(DescriptorParser.OBJECT_START);
                    userData.append(getByteArrayWrapper(arrayDimensions));
                    userData.append(DescriptorParser.OBJECT_END);
                }else{
                    userData.append(DescriptorParser.BYTE);
                }
                return userData;
            }

            @Override
            public StringBuilder argumentStart(StringBuilder userData) {
                userData.append(DescriptorParser.ARGS_START);
                return userData;
            }
            @Override
            public StringBuilder argumentEnd(StringBuilder userData) {
                userData.append(DescriptorParser.ARGS_END);
                return userData;
            }
            @Override
            public StringBuilder readVoid(StringBuilder userData) {
                userData.append(DescriptorParser.VOID);
                return userData;
            }

        }, new StringBuilder());

        return builder.toString();
    }


    private static String buildArrayDescriptor(int length, String elementDescriptor) {
        return buildFullString(length, '[') + elementDescriptor;
    }

    private static String buildFullString(int length, char element) {
        return new String(new char[length]).replace('\0', element);
    }

    private static String typeDescriptorForClass(Class<?> clazz) {
        return 'L' + clazz.getName() + ';';
    }
}
