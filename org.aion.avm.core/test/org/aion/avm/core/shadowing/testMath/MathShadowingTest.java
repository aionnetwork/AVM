package org.aion.avm.core.shadowing.testMath;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MathShadowingTest {
    private SimpleAvm avm;
    private Class<?> clazz;

    @Before
    public void setup() throws ClassNotFoundException {
        this.avm = new SimpleAvm(10000L, TestResource.class);
        this.clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName());
    }

    @After
    public void teardown() {
        this.avm.shutdown();
    }

    @Test
    public void testMaxMin() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMaxMin"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    /**
     * Creates a basic MathContext, just to prove that it is correctly formed.
     */
    @Test
    public void createSimpleContext() throws Exception {
        Object result = clazz.getMethod(NamespaceMapper.mapMethodName("testMathContext")).invoke(null);
        Assert.assertNotNull(result);
    }
}
