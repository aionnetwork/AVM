package org.aion.avm.core.shadowing.testPrimitive;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PrimitiveShadowingTest {
    private SimpleAvm avm;
    private Class<?> clazz;

    @Before
    public void setup() throws ClassNotFoundException {
        avm = new SimpleAvm(1000000000000000000L, TestResource.class,
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
        clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName());
    }

    @After
    public void teardown() {
        avm.shutdown();
    }

    @Test
    public void testBoolean() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testBoolean"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testByte() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testByte"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testDouble() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testDouble"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testFloat() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testFloat"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testInteger() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testInteger"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testLong() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testLong"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testShort() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testShort"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testAutoboxing() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testAutoboxing"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}
