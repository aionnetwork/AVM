package org.aion.avm.core.shadowing.testEnum;


import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.api.Address;
import org.junit.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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
        String enumClassName = TestEnum.class.getName();
        byte[] enumBytecode = Helpers.loadRequiredResourceAsBytes(enumClassName.replaceAll("\\.", "/") + ".class");
        String enumMappedClassName = PackageConstants.kUserDotPrefix + enumClassName;

        String testClassName = TestResource.class.getName();
        byte[] testBytecode = Helpers.loadRequiredResourceAsBytes(testClassName.replaceAll("\\.", "/") + ".class");
        String testMappedClassName = PackageConstants.kUserDotPrefix + testClassName;

        Set<String> classNamesPreMap = new HashSet<>(Arrays.asList(enumClassName, testClassName));

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new UserClassMappingVisitor(classNamesPreMap))
                        .addNextVisitor(new ClassShadowing(PackageConstants.kInternalSlashPrefix + "Helper", new ClassWhiteList()))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();

        Map<String, byte[]> classes = new HashMap<>();
        classes.put(testMappedClassName, transformer.apply(testBytecode));
        classes.put(enumMappedClassName, transformer.apply(enumBytecode));

        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        new Helper(loader, new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 0));
        clazz = loader.loadClass(testMappedClassName);
    }

    @Ignore // TODO: fix by junhan
    @Test
    public void testEnumAccess() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testEnumAccess"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}