package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class ArrayWrappingTest {
    private Class<?> clazz;

    @Before
    // We only need to load the instrumented class once.
    public void getInstructmentedClass()throws IOException, ClassNotFoundException{
        String className = "org.aion.avm.core.arraywrapping.TestResource";
        Map<String, byte[]> classes = new HashMap<>(CommonGenerators.generateExceptionShadowsAndWrappers());
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            ClassReader in = new ClassReader(inputBytes);
            ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            ArrayWrappingClassAdapter swc = new ArrayWrappingClassAdapter(out);
            ArrayWrappingClassAdapterRef swcRef = new ArrayWrappingClassAdapterRef(swc);

            in.accept(swcRef, ClassReader.EXPAND_FRAMES);

            byte[] transformed = out.toByteArray();

            Helpers.writeBytesToFile(transformed,"/tmp/wrapped.class");

            return transformed;
        };
        byte[] raw = TestClassLoader.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        classes.put(className, transformer.apply(raw));
        AvmClassLoader loader = new AvmClassLoader(classes);

        clazz = loader.loadClass(className);
    }

    @Test
    public void testBooleanArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testBooleanArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testByteArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testByteArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testCharArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testCharArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testDoubleArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testDoubleArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testFloatArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testFloatArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testIntArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testIntArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testLongArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testLongArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testShortArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testShortArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testObjectArray() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testObjectArray");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }


    @Test
    public void testSignature() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testSignature");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}
