package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.PackageConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;


public class ClassShadowingTest {
    private static String runtimeClassName;

    @BeforeClass
    public static void setupClass() {
        runtimeClassName = Helpers.fulllyQualifiedNameToInternalName(Testing.class.getName());
    }

    @After
    public void clearTestingState() {
        Testing.countWrappedStrings = 0;
        Testing.countWrappedClasses = 0;
    }

    @Test
    public void testNewObject() throws Exception {
        SimpleAvm avm = new SimpleAvm(1_000_000L, TestObjectCreation.class);
        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestObjectCreation.class.getName());

        Object ret = clazz.getMethod(NamespaceMapper.mapMethodName("accessObject")).invoke(null);
        Assert.assertEquals(Integer.valueOf(1), ret);
        avm.shutdown();
    }

    @Test
    public void testReplaceJavaLang() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = TestResource.class.getName();
        byte[] bytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new UserClassMappingVisitor(createTestingAccessRules(className)))
                        .addNextVisitor(new ConstantVisitor(runtimeClassName))
                        .addNextVisitor(new ClassShadowing(runtimeClassName))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(PackageConstants.kUserDotPrefix + className, transformer.apply(bytecode));
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, 1_000_000L, 1);
        Class<?> clazz = loader.loadUserClassByOriginalName(className);
        Object obj = clazz.getConstructor().newInstance();

        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("abs"), int.class);
        Object ret = method.invoke(obj, -10);
        Assert.assertEquals(10, ret);

        // Note that all string constants are wrapped in <clinit>, so we should see that, but classes are lazily wrapped so that is still 0.
        Assert.assertEquals(1, Testing.countWrappedStrings);
        Assert.assertEquals(0, Testing.countWrappedClasses);

        // We can rely on our test-facing toString methods to look into what we got back.
        Object wrappedClass = clazz.getMethod(NamespaceMapper.mapMethodName("returnClass")).invoke(obj);
        Assert.assertEquals("class org.aion.avm.shadow.java.lang.String", wrappedClass.toString());
        Object wrappedString = clazz.getMethod(NamespaceMapper.mapMethodName("returnString")).invoke(obj);
        Assert.assertEquals("hello", wrappedString.toString());

        // Verify that we see wrapped instances.
        Assert.assertEquals(1, Testing.countWrappedStrings);
        Assert.assertEquals(1, Testing.countWrappedClasses);

        Helper.clearTestingState();
    }

    @Test
    public void testField() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = TestResource2.class.getName();
        String mappedClassName = PackageConstants.kUserDotPrefix + className;
        byte[] bytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, 0) /* DO NOT SKIP ANYTHING */
                        .addNextVisitor(new UserClassMappingVisitor(createTestingAccessRules(className)))
                        .addNextVisitor(new ConstantVisitor(runtimeClassName))
                        .addNextVisitor(new ClassShadowing(runtimeClassName))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();

        Map<String, byte[]> classes = new HashMap<>();
        classes.put(mappedClassName, transformer.apply(bytecode));

        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, 1_000_000L, 1);

        Class<?> clazz = loader.loadClass(mappedClassName);
        Object obj = clazz.getConstructor().newInstance();

        //Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("getStatic");
        //Object ret = method.invoke(obj);
        //Assert.assertTrue(loadedClasses.contains(PackageConstants.kShadowJavaLangDotPrefix + "Byte"));

        Method method2 = clazz.getMethod(NamespaceMapper.mapMethodName("localVariable"));
        Object ret2 = method2.invoke(obj);
        Assert.assertEquals(Integer.valueOf(3), ret2);

        Helper.clearTestingState();
    }

    @Test
    public void testInterfaceHandling() throws Exception {
        String className = TestResourceInterface.class.getName();
        byte[] bytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");

        String innerClassName = className + "$1";
        byte[] innerBytecode = Helpers.loadRequiredResourceAsBytes(innerClassName.replaceAll("\\.", "/") + ".class");

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new UserClassMappingVisitor(createTestingAccessRules(className, innerClassName)))
                        .addNextVisitor(new ConstantVisitor(runtimeClassName))
                        .addNextVisitor(new ClassShadowing(runtimeClassName))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        byte[] transformed = transformer.apply(bytecode);
        classes.put(PackageConstants.kUserDotPrefix + className, transformed);
        classes.put(PackageConstants.kUserDotPrefix + innerClassName, transformer.apply(innerBytecode));

        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, 1_000_000L, 1);
        Class<?> clazz = loader.loadUserClassByOriginalName(className);

        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("getStringForNull"));
        Object ret = method.invoke(null);
        // Note that we can't yet override methods in our contracts so the toString returns false, from Object.
        Assert.assertEquals(null, ret);

        Helper.clearTestingState();
    }

    @Test
    public void testEnumHandling() throws Exception {
        SimpleAvm avm = new SimpleAvm(1000000L, TestResourceEnum.class);
        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(TestResourceEnum.class.getName());
        
        // Try the normal constructor (private, so set accessible).
        Constructor<?> one = clazz.getDeclaredConstructor(org.aion.avm.shadow.java.lang.String.class, int.class, org.aion.avm.shadow.java.lang.String.class);
        one.setAccessible(true);
        Object instance = one.newInstance(new org.aion.avm.shadow.java.lang.String("TEST"), 99, new org.aion.avm.shadow.java.lang.String("TEST"));
        Assert.assertNotNull(instance);
        
        // Try the deserialization constructor.
        Object stub = clazz.getConstructor(IDeserializer.class, long.class).newInstance(null, 6l);
        Assert.assertNotNull(stub);
        avm.shutdown();
    }

    @Test
    public void testEnumHandling_internal() throws Exception {
        SimpleAvm avm = new SimpleAvm(1000000L, TestContainer.class, TestContainer.InternalEnum.class);
        AvmClassLoader loader = avm.getClassLoader();
        Class<?> clazz = loader.loadUserClassByOriginalName(TestContainer.InternalEnum.class.getName());
        
        // Try the normal constructor (private, so set accessible).
        Constructor<?> one = clazz.getDeclaredConstructor(org.aion.avm.shadow.java.lang.String.class, int.class, org.aion.avm.shadow.java.lang.String.class);
        one.setAccessible(true);
        Object instance = one.newInstance(new org.aion.avm.shadow.java.lang.String("TEST"), 99, new org.aion.avm.shadow.java.lang.String("TEST"));
        Assert.assertNotNull(instance);
        
        // Try the deserialization constructor.
        Object stub = clazz.getConstructor(IDeserializer.class, long.class).newInstance(null, 6l);
        Assert.assertNotNull(stub);
        avm.shutdown();
    }

    /**
     * Shadow Object is only instance-equal, no other kind of equality is defined.
     */
    @Test
    public void testShadowObjectEquality() throws Exception {
        SimpleAvm avm = new SimpleAvm(1_000_000L, TestObjectCreation.class);
        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestObjectCreation.class.getName());
        Method createInstance = clazz.getMethod(NamespaceMapper.mapMethodName("createInstance"));
        Method isEqual = clazz.getMethod(NamespaceMapper.mapMethodName("isEqual"), org.aion.avm.internal.IObject.class, org.aion.avm.internal.IObject.class);

        Object one = createInstance.invoke(null);
        Object two = createInstance.invoke(null);
        Assert.assertEquals(Boolean.TRUE, isEqual.invoke(null, one, one));
        Assert.assertEquals(Boolean.FALSE, isEqual.invoke(null, one, two));
        avm.shutdown();
    }


    public static class Testing {
        public static int countWrappedClasses;
        public static int countWrappedStrings;

        public static <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
            countWrappedClasses += 1;
            return Helper.wrapAsClass(input);
        }

        public static org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
            countWrappedStrings += 1;
            return Helper.wrapAsString(input);
        }
    }

    private PreRenameClassAccessRules createTestingAccessRules(String... userDotNameClasses) {
        // WARNING:  We are providing the class set as both the "classes only" and "classes plus interfaces" sets.
        // This works for this test but, in general, is not correct.
        Set<String> userClassDotNameSet = Set.of(userDotNameClasses);
        return new PreRenameClassAccessRules(userClassDotNameSet, userClassDotNameSet);
    }
}
