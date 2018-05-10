package org.aion.avm.core.shadowing;

import org.aion.avm.core.TestClassLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClassShadowingTest {

    @Test
    public void testReplaceJavaLang() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<String, String> map = new HashMap<>();
        map.put("java/lang/Object", "org/aion/avm/java/lang/Object");
        map.put("java/lang/Math", "org/aion/avm/java/lang/Math");

        String name = "org.aion.avm.core.shadowing.TestResource";

        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), name, (inputBytes) -> ClassShadowing.replaceClassRef(inputBytes, map));
        Class<?> clazz = loader.loadClass(name);
        Object obj = clazz.getConstructor().newInstance();

        Method method = clazz.getMethod("multi", int.class, int.class);
        Object ret = method.invoke(obj, 1, 2);
        Assert.assertEquals(Integer.valueOf(0), ret);
    }
}
