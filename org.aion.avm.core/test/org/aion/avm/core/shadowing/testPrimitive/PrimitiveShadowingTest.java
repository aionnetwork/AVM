package org.aion.avm.core.shadowing.testPrimitive;

import org.aion.avm.api.Address;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.SimpleRuntime;
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
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    private Class<?> clazz;

    @After
    public void clearTestingState() {
        Helper.clearTestingState();
    }

    @Before
    public void testReplaceJavaLang() throws ClassNotFoundException {
        SimpleAvm avm = new SimpleAvm(1000000L, TestResource.class);
        AvmClassLoader loader = avm.getClassLoader();

        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, true, loader);
        loader.addHandler(wrapperGenerator);

        Helpers.writeBytesToFile(loader.getUserClassBytecodeByOriginalName(TestResource.class.getName()), "/tmp/Prim.class");

        this.clazz = loader.loadUserClassByOriginalName(TestResource.class.getName());
    }

    @Test
    public void testAutoboxing() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testAutoboxing"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}
