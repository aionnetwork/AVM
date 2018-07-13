package org.aion.avm.core.shadowing.testEnum;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.internal.Helper;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumShadowingTest {
    private Class<?> clazz;

    @After
    public void clearTestingState() {
        Helper.clearTestingState();
    }

    @Before
    public void testReplaceJavaLang() throws ClassNotFoundException {
        SimpleAvm avm = new SimpleAvm(1000000L, TestResource.class, TestEnum.class);
        AvmClassLoader loader = avm.getClassLoader();

        this.clazz = loader.loadUserClassByOriginalName(TestResource.class.getName());

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