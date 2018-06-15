package org.aion.avm.core.shadowing.Enum;


import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.rt.Address;
import org.junit.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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
    public void testReplaceJavaLang() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String enumClassName = TestEnum.class.getName();
        byte[] raw1 = Helpers.loadRequiredResourceAsBytes(enumClassName.replaceAll("\\.", "/") + ".class");
        String testClassName = TestEnumResource.class.getName();
        byte[] raw2 = Helpers.loadRequiredResourceAsBytes(testClassName.replaceAll("\\.", "/") + ".class");
        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new ClassShadowing(PackageConstants.kInternalSlashPrefix + "Helper", ClassWhiteList.buildForEmptyContract()))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();
        Map<String, byte[]> classes = new HashMap<>();

        byte[] transformed;

        transformed = transformer.apply(raw2);
        classes.put(testClassName, transformed);

        transformed = transformer.apply(raw1);
        classes.put(enumClassName, transformed);

        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 0));
        clazz = loader.loadClass(testClassName);;
    }

    @Test
    public void testEnumAccess() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("avm_testEnumAccess");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

}