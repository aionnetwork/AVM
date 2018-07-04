package org.aion.avm.core.shadowing.testPrimitive;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class PrimitiveShadowingTest {

    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() throws ClassNotFoundException {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        testReplaceJavaLang();
    }

    static private Class<?> clazz;

    @After
    public void clearTestingState() {
        Helper.clearTestingState();
    }

    public static void testReplaceJavaLang() throws ClassNotFoundException {
        SimpleAvm avm = new SimpleAvm(1000000000000000000L, TestResource.class,
                TestResource.BooleanTest.class,
                TestResource.BooleanTest.Factory.class,
                TestResource.BooleanTest.ParseBoolean.class,
                TestResource.ByteTest.class,
                TestResource.ByteTest.Decode.class,
                TestResource.DoubleTest.class,
                TestResource.DoubleTest.Constants.class,
                TestResource.DoubleTest.Extrema.class,
                TestResource.DoubleTest.NaNInfinityParsing.class,
                TestResource.DoubleTest.ToString.class,
                TestResource.FloatTest.class,
                TestResource.FloatTest.Constants.class,
                TestResource.FloatTest.Extrema.class,
                TestResource.FloatTest.NaNInfinityParsing.class,
                TestResource.IntegerTest.class,
                TestResource.IntegerTest.ToString.class,
                TestResource.IntegerTest.Decode.class,
                TestResource.IntegerTest.ParsingTest.class,
                TestResource.LongTest.class,
                TestResource.LongTest.Decode.class,
                TestResource.LongTest.ToString.class,
                TestResource.LongTest.ParsingTest.class,
                TestResource.ShortTest.class,
                TestResource.ShortTest.Decode.class,
                TestResource.ShortTest.ByteSwap.class
        );
        AvmClassLoader loader = avm.getClassLoader();

        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, loader);
        loader.addHandler(wrapperGenerator);

        Helpers.writeBytesToFile(loader.getUserClassBytecodeByOriginalName(TestResource.class.getName()), "/tmp/Prim.class");
        Helpers.writeBytesToFile(loader.getUserClassBytecodeByOriginalName(TestResource.ShortTest.Decode.class.getName()), "/tmp/Short.class");

        clazz = loader.loadUserClassByOriginalName(TestResource.class.getName());
    }

    @Test
    public void testBoolean() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testBoolean"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testByte() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testByte"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testDouble() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testDouble"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testFloat() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testFloat"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testInteger() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testInteger"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testLong() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testLong"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testShort() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testShort"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testAutoboxing() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testAutoboxing"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}
