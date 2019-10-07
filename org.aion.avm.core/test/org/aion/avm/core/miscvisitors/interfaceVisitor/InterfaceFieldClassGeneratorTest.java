package org.aion.avm.core.miscvisitors.interfaceVisitor;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.InterfaceFieldClassGeneratorVisitor;
import org.aion.avm.core.miscvisitors.InterfaceFieldNameMappingVisitor;
import org.aion.avm.core.types.GeneratedClassConsumer;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

public class InterfaceFieldClassGeneratorTest {

    @Test
    public void testInterfaceContainingFieldsInnerClass() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String[] classNames = new String[]{getClassName(ClassWithFields.class),
                getClassName(ClassWithFields.InnerInterface.class),
                getClassName(ClassWithFields.InnerInterface.FIELDS.class),
                getClassName(OuterInterfaceWithFieldsClass.class),
                getClassName(OuterInterfaceWithFieldsClass.FIELDS.class)
        };

        Map<String, byte[]> classes = new HashMap<>();

        GeneratedClassConsumer consumer = (superClassName, className, bytecode) -> {
            classes.put(Helpers.internalNameToFulllyQualifiedName(className), bytecode);
        };
        Map<String, String> interfaceFieldClassNames = new HashMap<>();
        String javaLangObjectSlashName = "java/lang/Object";

        byte[][] bytecode = new byte[classNames.length][];
        for (int i = 0; i < classNames.length; ++i) {
            bytecode[i] = Helpers.loadRequiredResourceAsBytes(classNames[i] + ".class");
        }

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0)
                        .addNextVisitor(new InterfaceFieldClassGeneratorVisitor(consumer, interfaceFieldClassNames, javaLangObjectSlashName))
                        .addNextVisitor(new InterfaceFieldNameMappingVisitor(interfaceFieldClassNames))
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();

        for (int i = 0; i < classNames.length; ++i) {
            classes.put(Helpers.internalNameToFulllyQualifiedName(classNames[i]), transformer.apply(bytecode[i]));
        }

        //ensure FIELDS class is generated for all the interface
        Assert.assertEquals(7, classes.size());

        // ensure fields are removed from the class
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        Class<?> clazz = loader.loadClass(OuterInterfaceWithFieldsClass.class.getName());
        Assert.assertEquals(0, clazz.getDeclaredFields().length);

        // validate field values
        clazz = loader.loadClass(ClassWithFields.InnerInterface.class.getName() + "$FIELDS");
        Assert.assertNotNull(clazz.getDeclaredField("d"));
        Assert.assertNotNull(clazz.getDeclaredField("e"));

        clazz = loader.loadClass(ClassWithFields.InnerInterface.class.getName() + "$FIELDS0");
        Field a = clazz.getField("a");
        a.setAccessible(true);
        Assert.assertEquals(1, a.get(null));

        Field b = clazz.getField("b");
        b.setAccessible(true);
        Assert.assertEquals("abc", b.get(null));

        Field c = clazz.getField("c");
        c.setAccessible(true);
        Assert.assertEquals(Object.class, c.get(null).getClass());

        clazz = loader.loadClass(OuterInterfaceWithFieldsClass.class.getName() + "$FIELDS0");
        Field outerA = clazz.getField("a");
        outerA.setAccessible(true);
        Assert.assertEquals(1, outerA.get(null));

        clazz = loader.loadClass(OuterInterfaceWithFieldsClass.class.getName() + "$FIELDS");
        Assert.assertNotNull(clazz.getDeclaredField("f"));

        clazz = loader.loadClass(ClassWithFields.class.getName());
        Object ret = clazz.getMethod("f").invoke(null);
        Assert.assertEquals(4, ret);
    }

    @Test
    public void testNestedInterface() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String[] classNames = new String[]{getClassName(NestedInterfaces.class),
                getClassName(NestedInterfaces.InnerInterface.class),
                getClassName(NestedInterfaces.InnerInterface.InnerInterfaceLevel2.class),
                getClassName(NestedInterfaces.InnerInterface.InnerInterfaceLevel2.InnerInterfaceLevel3.class)
        };

        Map<String, byte[]> classes = new HashMap<>();

        GeneratedClassConsumer consumer = (superClassName, className, bytecode) -> {
            classes.put(Helpers.internalNameToFulllyQualifiedName(className), bytecode);
        };
        Map<String, String> interfaceFieldClassNames = new HashMap<>();
        String javaLangObjectSlashName = "java/lang/Object";

        byte[][] bytecode = new byte[classNames.length][];
        for (int i = 0; i < classNames.length; ++i) {
            bytecode[i] = Helpers.loadRequiredResourceAsBytes(classNames[i] + ".class");
        }

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0)
                        .addNextVisitor(new InterfaceFieldClassGeneratorVisitor(consumer, interfaceFieldClassNames, javaLangObjectSlashName))
                        .addNextVisitor(new InterfaceFieldNameMappingVisitor(interfaceFieldClassNames))
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();

        for (int i = 0; i < classNames.length; ++i) {
            classes.put(Helpers.internalNameToFulllyQualifiedName(classNames[i]), transformer.apply(bytecode[i]));
        }

        //ensure FIELDS class is generated for all the interface
        Assert.assertEquals(7, classes.size());

        // ensure fields are removed from the class
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        Class<?> clazz = loader.loadClass(NestedInterfaces.InnerInterface.class.getName());
        Assert.assertEquals(0, clazz.getFields().length);

        clazz = loader.loadClass(NestedInterfaces.InnerInterface.InnerInterfaceLevel2.class.getName());
        Assert.assertEquals(0, clazz.getFields().length);

        clazz = loader.loadClass(NestedInterfaces.InnerInterface.InnerInterfaceLevel2.InnerInterfaceLevel3.class.getName());
        Assert.assertEquals(0, clazz.getFields().length);

        // validate field values
        clazz = loader.loadClass(NestedInterfaces.InnerInterface.class.getName() + "$FIELDS");
        Assert.assertNotNull(clazz.getDeclaredField("a"));
        Assert.assertNotNull(clazz.getDeclaredField("b"));

        clazz = loader.loadClass(NestedInterfaces.InnerInterface.InnerInterfaceLevel2.class.getName() + "$FIELDS");
        Assert.assertNotNull(clazz.getDeclaredField("a"));
        Assert.assertNotNull(clazz.getDeclaredField("c"));

        clazz = loader.loadClass(NestedInterfaces.InnerInterface.InnerInterfaceLevel2.InnerInterfaceLevel3.class.getName() + "$FIELDS");
        Assert.assertNotNull(clazz.getDeclaredField("a"));
        Assert.assertNotNull(clazz.getDeclaredField("d"));

        clazz = loader.loadClass(NestedInterfaces.class.getName());
        Object ret = clazz.getMethod("f").invoke(null);
        Assert.assertEquals(18, ret);
    }

    @Test
    public void testLowerLevelFieldsInInterface() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        String[] classNames = new String[]{getClassName(ClassWithLowerLevelOfField.class),
                getClassName(ClassWithLowerLevelOfField.InnerInterface.class),
                getClassName(ClassWithLowerLevelOfField.InnerInterface.innerClass.class),
                getClassName(ClassWithLowerLevelOfField.InnerInterface.innerClass.FIELDS.class),
                getClassName(OuterInterfaceFields.class),
                getClassName(OuterInterfaceFields.innerClass.class),
                getClassName(OuterInterfaceFields.innerClass.FIELDS.class)
        };

        Map<String, byte[]> classes = new HashMap<>();

        GeneratedClassConsumer consumer = (superClassName, className, bytecode) -> {
            classes.put(Helpers.internalNameToFulllyQualifiedName(className), bytecode);
        };
        Map<String, String> interfaceFieldClassNames = new HashMap<>();
        String javaLangObjectSlashName = "java/lang/Object";

        byte[][] bytecode = new byte[classNames.length][];
        for (int i = 0; i < classNames.length; ++i) {
            bytecode[i] = Helpers.loadRequiredResourceAsBytes(classNames[i] + ".class");
        }

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0)
                        .addNextVisitor(new InterfaceFieldClassGeneratorVisitor(consumer, interfaceFieldClassNames, javaLangObjectSlashName))
                        .addNextVisitor(new InterfaceFieldNameMappingVisitor(interfaceFieldClassNames))
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();

        for (int i = 0; i < classNames.length; ++i) {
            classes.put(Helpers.internalNameToFulllyQualifiedName(classNames[i]), transformer.apply(bytecode[i]));
        }

        //ensure FIELDS class is generated for all the interface
        Assert.assertEquals(9, classes.size());

        // ensure fields are removed from the class
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        Class<?> clazz = loader.loadClass(ClassWithLowerLevelOfField.InnerInterface.class.getName());
        Assert.assertEquals(0, clazz.getFields().length);

        clazz = loader.loadClass(OuterInterfaceFields.class.getName());
        Assert.assertEquals(0, clazz.getFields().length);

        // validate field values
        clazz = loader.loadClass(ClassWithLowerLevelOfField.InnerInterface.class.getName() + "$FIELDS");
        Field a = clazz.getField("a");
        a.setAccessible(true);
        Assert.assertEquals(1, a.get(null));

        Field b = clazz.getField("b");
        b.setAccessible(true);
        Assert.assertEquals("abc", b.get(null));

        clazz = loader.loadClass(OuterInterfaceFields.class.getName() + "$FIELDS");
        a = clazz.getField("a");
        a.setAccessible(true);
        Assert.assertEquals(2, a.get(null));

        b = clazz.getField("b");
        b.setAccessible(true);
        Assert.assertEquals("def", b.get(null));
    }

    @Test
    public void testMultipleFieldsClassNames() throws ClassNotFoundException, NoSuchFieldException {
        String[] classNames = new String[]{getClassName(ClassWithMultipleFieldSuffix.class),
                getClassName(ClassWithMultipleFieldSuffix.InnerInterface.class),
        };

        Map<String, byte[]> classes = new HashMap<>();

        GeneratedClassConsumer consumer = (superClassName, className, bytecode) -> {
            classes.put(Helpers.internalNameToFulllyQualifiedName(className), bytecode);
        };
        Map<String, String> interfaceFieldClassNames = new HashMap<>();
        String javaLangObjectSlashName = "java/lang/Object";

        byte[][] bytecode = new byte[classNames.length][];
        for (int i = 0; i < classNames.length; ++i) {
            bytecode[i] = Helpers.loadRequiredResourceAsBytes(classNames[i] + ".class");
        }

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0)
                        .addNextVisitor(new InterfaceFieldClassGeneratorVisitor(consumer, interfaceFieldClassNames, javaLangObjectSlashName))
                        .addNextVisitor(new InterfaceFieldNameMappingVisitor(interfaceFieldClassNames))
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();

        for (int i = 0; i < classNames.length; ++i) {
            classes.put(Helpers.internalNameToFulllyQualifiedName(classNames[i]), transformer.apply(bytecode[i]));
        }

        //ensure FIELDS class is generated for all the interface
        Assert.assertEquals(3, classes.size());

        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        Class<?> clazz = loader.loadClass(ClassWithMultipleFieldSuffix.InnerInterface.class.getName());
        Assert.assertEquals(0, clazz.getFields().length);

        // missing class name
        clazz = loader.loadClass(ClassWithMultipleFieldSuffix.InnerInterface.class.getName() + "$FIELDS16");
        Assert.assertNotNull(clazz.getDeclaredField("a"));
        Assert.assertNotNull(clazz.getDeclaredField("b"));
        Assert.assertNotNull(clazz.getDeclaredField("c"));
    }

    @Test
    public void testNoFields() {
        String[] classNames = new String[]{getClassName(ClassWithNoInterfaceFields.class),
                getClassName(ClassWithNoInterfaceFields.InnerInterface.class),
                getClassName(outerInterfaceNoFields.class),
        };

        Map<String, byte[]> classes = new HashMap<>();

        GeneratedClassConsumer consumer = (superClassName, className, bytecode) -> {
            classes.put(Helpers.internalNameToFulllyQualifiedName(className), bytecode);
        };
        Map<String, String> interfaceFieldClassNames = new HashMap<>();
        String javaLangObjectSlashName = "java/lang/Object";

        byte[][] bytecode = new byte[classNames.length][];
        for (int i = 0; i < classNames.length; ++i) {
            bytecode[i] = Helpers.loadRequiredResourceAsBytes(classNames[i] + ".class");
        }

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0)
                        .addNextVisitor(new InterfaceFieldClassGeneratorVisitor(consumer, interfaceFieldClassNames, javaLangObjectSlashName))
                        .addNextVisitor(new InterfaceFieldNameMappingVisitor(interfaceFieldClassNames))
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();

        for (int i = 0; i < classNames.length; ++i) {
            classes.put(Helpers.internalNameToFulllyQualifiedName(classNames[i]), transformer.apply(bytecode[i]));
        }

        Assert.assertEquals(3, classes.size());
    }

    @Test
    public void testInnerInterfaceAllFIELDS() throws ClassNotFoundException, NoSuchFieldException {
        Map<String, byte[]> classes = new HashMap<>();

        GeneratedClassConsumer consumer = (superClassName, className, bytecode) -> {
            classes.put(Helpers.internalNameToFulllyQualifiedName(className), bytecode);
        };
        Map<String, String> interfaceFieldClassNames = new HashMap<>();
        String javaLangObjectSlashName = "java/lang/Object";


        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0)
                        .addNextVisitor(new InterfaceFieldClassGeneratorVisitor(consumer, interfaceFieldClassNames, javaLangObjectSlashName))
                        .addNextVisitor(new InterfaceFieldNameMappingVisitor(interfaceFieldClassNames))
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();

        classes.put("NestedMain", transformer.apply(getFIELDMainClassBytes()));
        classes.put("NestedInterfaces", transformer.apply(getInnerFILEDSInterfaceBytes()));
        classes.put("NestedInterfaces$FIELDS", transformer.apply(getNestedInterfaceCalledFIELDSLevelOne()));
        classes.put("NestedInterfaces$FIELDS$FIELDS", transformer.apply(getNestedInterfaceCalledFIELDSLevelTwo()));

        Assert.assertEquals(6, classes.size());

        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        //FIELDS does exits as an inner class
        Class<?> clazz = loader.loadClass("NestedInterfaces$FIELDS" + "$FIELDS0");
        Assert.assertNotNull(clazz.getDeclaredField("a"));
        //FIELDS does not exit as an inner class
        clazz = loader.loadClass("NestedInterfaces$FIELDS$FIELDS" + "$FIELDS");
        Assert.assertNotNull(clazz.getDeclaredField("b"));

    }

    private String getClassName(Class<?> clazz) {
        return clazz.getName().replaceAll("\\.", "/");
    }

    public static byte[] getInnerFILEDSInterfaceBytes() {
        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "NestedInterfaces", null, "java/lang/Object", null);

        classWriter.visitSource("NestedInterfaces.java", null);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS", "NestedInterfaces", "FIELDS", ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS$FIELDS", "NestedInterfaces$FIELDS", "FIELDS", ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    public static byte[] getNestedInterfaceCalledFIELDSLevelOne() {
        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;

        classWriter.visit(V10, ACC_ABSTRACT | ACC_INTERFACE, "NestedInterfaces$FIELDS", null, "java/lang/Object", null);

        classWriter.visitSource("NestedInterfaces.java", null);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS", "NestedInterfaces", "FIELDS", ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS$FIELDS", "NestedInterfaces$FIELDS", "FIELDS", ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        {
            fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "a", "I", null, new Integer(101));
            fieldVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    public static byte[] getNestedInterfaceCalledFIELDSLevelTwo() {

        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE, "NestedInterfaces$FIELDS$FIELDS", null, "java/lang/Object", null);

        classWriter.visitSource("NestedInterfaces.java", null);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS", "NestedInterfaces", "FIELDS", ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS$FIELDS", "NestedInterfaces$FIELDS", "FIELDS", ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        {
            fieldVisitor = classWriter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "b", "I", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(7, label0);
            methodVisitor.visitTypeInsn(NEW, "java/lang/Object");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
            methodVisitor.visitFieldInsn(PUTSTATIC, "NestedInterfaces$FIELDS$FIELDS", "b", "I");
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 0);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    public static byte[] getFIELDMainClassBytes() {

        ClassWriter classWriter = new ClassWriter(0);
        MethodVisitor methodVisitor;

        classWriter.visit(V10, ACC_PUBLIC | ACC_SUPER, "NestedMain", null, "java/lang/Object", null);

        classWriter.visitSource("NestedMain.java", null);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS", "NestedInterfaces", "FIELDS", ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        classWriter.visitInnerClass("NestedInterfaces$FIELDS$FIELDS", "NestedInterfaces$FIELDS", "FIELDS", ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);

        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "()[B", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(5, label0);
            methodVisitor.visitIntInsn(BIPUSH, 101);
            methodVisitor.visitFieldInsn(GETSTATIC, "NestedInterfaces$FIELDS$FIELDS", "b", "I");
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitIntInsn(SIPUSH, 303);
            methodVisitor.visitInsn(IADD);
            methodVisitor.visitVarInsn(ISTORE, 0);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(6, label1);
            methodVisitor.visitVarInsn(ILOAD, 0);
            methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(2, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
