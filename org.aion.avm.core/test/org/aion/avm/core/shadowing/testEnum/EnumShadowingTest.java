package org.aion.avm.core.shadowing.testEnum;

import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.api.Address;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class EnumShadowingTest {
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
        SimpleRuntime externalRuntime = new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 10000);
        SimpleAvm avm = new SimpleAvm(externalRuntime, TestResource.class, TestEnum.class);
        AvmClassLoader loader = avm.getClassLoader();

        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, true, loader);
        loader.addHandler(wrapperGenerator);

        //Helpers.writeBytesToFile(loader.getUserClassBytecodeByOriginalName(TestResource.class.getName()), "/tmp/testR.class");
        //Helpers.writeBytesToFile(loader.getUserClassBytecodeByOriginalName(TestEnum.class.getName()), "/tmp/enum.class");

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
}