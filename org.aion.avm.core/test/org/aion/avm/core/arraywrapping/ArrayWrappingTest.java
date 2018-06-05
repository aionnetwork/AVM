package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class ArrayWrappingTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    private Class<?> clazz;

    @Before
    // We only need to load the instrumented class once.
    public void getInstrumentedClass() throws ClassNotFoundException {
        String className = "org.aion.avm.core.arraywrapping.TestResource";
        Map<String, byte[]> classes = new HashMap<>();
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            final ClassToolchain toolchain = new ClassToolchain.Builder(inputBytes, ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new ArrayWrappingClassAdapterRef())
                    .addNextVisitor(new ArrayWrappingClassAdapter())
                    .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES))
                    .build();
            byte[] transformed = toolchain.runAndGetBytecode();

            Helpers.writeBytesToFile(transformed,"/tmp/wrapped.class");

            return transformed;
        };
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        classes.put(className, transformer.apply(raw));
        Map<String, byte[]> finalClasses = Helpers.mapIncludingHelperBytecode(classes);

        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, finalClasses);

        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.genWrapperClass(cName);
        loader.addHandler(wrapperGenerator);

        // We don't really need the runtime but we do need to initialize the Helper.
        Helpers.instantiateHelper(loader, new SimpleRuntime(new byte[0], new byte[0], 1000000));

        clazz = loader.loadClass(className);
    }

    @Test
    public void testBooleanArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testBooleanArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testByteArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testByteArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testCharArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testCharArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testDoubleArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testDoubleArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testFloatArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testFloatArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testIntArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testIntArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testLongArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testLongArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testShortArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testShortArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testObjectArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testObjectArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testStringArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testStringArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testSignature() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testSignature");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testVarargs() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testVarargs");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testTypeChecking() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testTypeChecking");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testClassField() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testClassField");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiInt() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testMultiInt");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiByte() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testMultiByte");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiChar() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testMultiChar");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiFloat() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testMultiFloat");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiLong() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testMultiLong");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiDouble() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testMultiDouble");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiRef() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testMultiRef");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }


    @Test
    public void testHierarachy() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testHierarachy");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

}
