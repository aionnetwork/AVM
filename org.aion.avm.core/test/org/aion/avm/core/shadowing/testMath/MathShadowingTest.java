package org.aion.avm.core.shadowing.testMath;

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
import org.aion.avm.rt.Address;
import org.junit.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MathShadowingTest {

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
        String testClassName = TestResource.class.getName();
        byte[] testBytecode = Helpers.loadRequiredResourceAsBytes(testClassName.replaceAll("\\.", "/") + ".class");
        String mappedClassName = PackageConstants.kUserDotPrefix + testClassName;

        Function<byte[], byte[]> transformer = (inputBytes) ->
                new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new UserClassMappingVisitor(Collections.singleton(testClassName)))
                        .addNextVisitor(new ClassShadowing("org/aion/avm/internal/Helper", new ClassWhiteList()))
                        .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                        .build()
                        .runAndGetBytecode();

        Map<String, byte[]> classes = new HashMap<>();
        byte[] transformed = transformer.apply(testBytecode);
        classes.put(mappedClassName, transformed);

        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);

        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(loader, new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 0));
        clazz = loader.loadClass(mappedClassName);
    }

    @Test
    public void testMaxMin() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod(UserClassMappingVisitor.mapMethodName("testMaxMin"));

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

}
