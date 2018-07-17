package org.aion.avm.core.shadowing.testMath;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.internal.Helper;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class MathShadowingTest {
    private Class<?> clazz;

    @After
    public void clearTestingState() {
        Helper.clearTestingState();
    }

    @Before
    public void testReplaceJavaLang() throws ClassNotFoundException {
        SimpleAvm avm = new SimpleAvm(10000L, TestResource.class);
        AvmClassLoader loader = avm.getClassLoader();

        this.clazz = loader.loadUserClassByOriginalName(TestResource.class.getName());
    }

    @Test
    public void testMaxMin() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testMaxMin"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    /**
     * Creates a basic MathContext, just to prove that it is correctly formed.
     */
    @Test
    public void createSimpleContext() throws Exception {
        Object result = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testMathContext")).invoke(null);
        Assert.assertNotNull(result);
    }
}
