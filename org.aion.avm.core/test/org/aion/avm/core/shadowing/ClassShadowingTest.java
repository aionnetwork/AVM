package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class ClassShadowingTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    @Test
    public void testReplaceJavaLang() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = "org.aion.avm.core.shadowing.TestResource";
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new ClassShadowing(Testing.CLASS_NAME, ClassWhiteList.buildForEmptyContract()))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, transformer.apply(raw));
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, new SimpleRuntime(new byte[0], new byte[0], 0));
        Class<?> clazz = loader.loadClass(className);
        Object obj = clazz.getConstructor().newInstance();

        Method method = clazz.getMethod("multi", int.class, int.class);
        Object ret = method.invoke(obj, 1, 2);
        Assert.assertEquals(0, ret);

        // Verify that we haven't created any wrapped instances, yet.
        Assert.assertEquals(0, Testing.countWrappedClasses);
        Assert.assertEquals(0, Testing.countWrappedClasses);

        // We can rely on our test-facing toString methods to look into what we got back.
        Object wrappedClass = clazz.getMethod("returnClass").invoke(obj);
        Assert.assertEquals("class org.aion.avm.java.lang.String", wrappedClass.toString());
        Object wrappedString = clazz.getMethod("returnString").invoke(obj);
        Assert.assertEquals("hello", wrappedString.toString());

        // Verify that we see wrapped instances.
        Assert.assertEquals(1, Testing.countWrappedClasses);
        Assert.assertEquals(1, Testing.countWrappedClasses);

        Helper.clearTestingState();
    }

    @Test
    public void testField() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = "org.aion.avm.core.shadowing.TestResource2";
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new ClassShadowing(Testing.CLASS_NAME, ClassWhiteList.buildForEmptyContract()))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, transformer.apply(raw));


        Set<String> loadedClasses = new HashSet<>();
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes) {
            @Override
            public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                loadedClasses.add(name);
                return super.loadClass(name, resolve);
            }
        };

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, new SimpleRuntime(new byte[0], new byte[0], 0));

        Class<?> clazz = loader.loadClass(className);
        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("getStatic");
        Object ret = method.invoke(obj);

        Assert.assertTrue(loadedClasses.contains("org.aion.avm.java.lang.Byte"));

        Helper.clearTestingState();
    }

    public static class Testing {
        public static String CLASS_NAME = Testing.class.getName().replaceAll("\\.", "/");
        public static int countWrappedClasses;
        public static int countWrappedStrings;

        public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
            countWrappedClasses += 1;
            return Helper.wrapAsClass(input);
        }
        public static org.aion.avm.java.lang.String wrapAsString(String input) {
            countWrappedStrings += 1;
            return Helper.wrapAsString(input);
        }
    }
}