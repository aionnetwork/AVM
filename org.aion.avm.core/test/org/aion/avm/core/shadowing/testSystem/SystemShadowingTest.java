package org.aion.avm.core.shadowing.testSystem;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class SystemShadowingTest {

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
        SimpleAvm avm = new SimpleAvm(1000000000000000000L, TestResource.class);
        AvmClassLoader loader = avm.getClassLoader();

        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, true, loader);
        loader.addHandler(wrapperGenerator);

        Helpers.writeBytesToFile(loader.getUserClassBytecodeByOriginalName(TestResource.class.getName()), "/tmp/System.class");

        clazz = loader.loadUserClassByOriginalName(TestResource.class.getName());
    }

    @Test
    public void testArrayCopy() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testArrayCopy"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}
