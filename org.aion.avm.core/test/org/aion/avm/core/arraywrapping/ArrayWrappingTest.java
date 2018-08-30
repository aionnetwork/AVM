package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.internal.IHelper;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ArrayWrappingTest {

    private SimpleAvm avm;
    private Class<?> clazz;
    private IHelper helper;

    @Before
    public void setup() throws ClassNotFoundException {
        avm = new SimpleAvm(1000000000000000000L,
                TestResource.class,
                TestResource.A.class,
                TestResource.B.class,
                TestResource.C.class,
                TestResource.X.class,
                TestResource.Y.class,
                TestResource.Z.class
        );
        helper = avm.getHelper();
        clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName());

        helper.externalSetEnergy(1000000000000L);
    }

    @After
    public void teardown() {
        avm.shutdown();
    }

    @Test
    public void testBooleanArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testBooleanArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testByteArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testByteArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testCharArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testCharArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testDoubleArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testDoubleArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testFloatArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testFloatArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testIntArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testIntArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testLongArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testLongArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testShortArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testShortArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testObjectArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testObjectArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testStringArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testStringArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testSignature() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testSignature"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testVarargs() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testVarargs"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testTypeChecking() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testTypeChecking"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testClassField() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testClassField"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiInt() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiInt"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiByte() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiByte"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiChar() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiChar"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiFloat() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiFloat"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiLong() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiLong"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiDouble() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiDouble"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testMultiRef() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiRef"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }


    @Test
    public void testHierarachy() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testHierarachy"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testIncompleteArrayIni() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testIncompleteArrayIni"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

//    @Test
//    public void testArrayEnergy() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//
//        Object obj = clazz.getConstructor().newInstance();
//        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testArrayEnergy"));
//
//        helper.externalSetEnergy(10000000);
//        try{
//            method.invoke(obj);
//        }catch(InvocationTargetException e){
//            Assert.assertFalse(e.getCause() instanceof OutOfEnergyException);
//        }
//
//        helper.externalSetEnergy(1000);
//        try{
//            method.invoke(obj);
//        }catch(InvocationTargetException e){
//            Assert.assertTrue(e.getCause() instanceof OutOfEnergyException);
//        }
//        helper.externalSetEnergy(10000000000L);
//    }

    @Test
    public void testInterfaceArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testInterfaceArray"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testArrayClone() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testArrayClone"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

}
