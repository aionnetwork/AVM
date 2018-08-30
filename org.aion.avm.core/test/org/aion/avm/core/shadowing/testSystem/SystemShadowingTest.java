package org.aion.avm.core.shadowing.testSystem;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemShadowingTest {

    private SimpleAvm avm;
    private Class<?> clazz;

    @Before
    public void setup() throws ClassNotFoundException {
        avm = new SimpleAvm(1000000000000000000L, TestResource.class);
        clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName());
    }

    @After
    public void teardown() {
        avm.shutdown();
    }

    @Test
    public void testArrayCopy() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testArrayCopy"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}
