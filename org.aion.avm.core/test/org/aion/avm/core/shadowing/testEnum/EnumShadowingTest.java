package org.aion.avm.core.shadowing.testEnum;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.internal.Helper;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumShadowingTest {
    private SimpleAvm avm;
    private Class<?> clazz;

    @Before
    public void setup() throws ClassNotFoundException {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        this.avm = new SimpleAvm(1000000L, TestResource.class, TestEnum.class);
        this.clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testEnumAccess() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testEnumAccess"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testEnumValues() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testEnumValues"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testShadowJDKEnum() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testShadowJDKEnum"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}