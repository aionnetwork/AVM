package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.internal.Helper;
import org.aion.avm.rt.Address;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;
import static org.junit.Assert.*;

/**
 * @author Roman Katerinenko
 */
public class InvokedynamicTransformationTest {
    private static String CLASS_NAME = InvokedynamicTransformationTest.TestingHelper.class
            .getName().replaceAll("\\.", "/");
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void init() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        final var avmClassLoader = new AvmClassLoader(sharedClassLoader, new HashMap<>());
        new Helper(avmClassLoader, new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 0));
    }
//    @Test
//    public void tryOriginalLambda() throws Exception {
//        final var className = ConstantStringsConcatIndy.class.getName();
//        final byte[] origBytecode = loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
//        NeverCommitUtils.storeAndJavap(origBytecode, "original.txt");
//        final var result = (java.lang.String) callTestMethod(origBytecode, className, "test");
//    }

    @Test
    public void given_stringConcatLambda_then_bootstrapMethodShouldNeShadowed() throws Exception {
        final var className = ConstantStringsConcatIndy.class.getName();
        final byte[] origBytecode = loadRequiredResourceAsBytes(getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForStringConcatTest(origBytecode, className);
        assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        final var actual = (org.aion.avm.java.lang.String) callTestMethod(transformedBytecode, className, "avm_test");
        Assert.assertEquals("abc", actual.toString());
    }

    private byte[] transformForStringConcatTest(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final var shadowPackage = "org/aion/avm/java/lang";
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ClassShadowing(CLASS_NAME, ClassWhiteList.build(className)))
                .addNextVisitor(new InvokedynamicShadower(CLASS_NAME, shadowPackage, ClassWhiteList.build(className)))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        sharedClassLoader,
                        classHierarchy,
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    @Test
    public void given_parametrizedLambda_then_allUserAccessableObjectsShouldBeShadowed() {
        final var className = ParametrizedLambda.class.getName();
        final byte[] origBytecode = loadRequiredResourceAsBytes(getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForParametrizedLambdaTest(origBytecode, className);
        assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        try {
            final org.aion.avm.core.testdoubles.indy.Double actual =
                    (org.aion.avm.core.testdoubles.indy.Double) callTestMethod(transformedBytecode, className, "avm_test");
            assertEquals(30, actual.avm_doubleValue(), 0);
            assertTrue(actual.avm_valueOfWasCalled);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private byte[] transformForParametrizedLambdaTest(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final var shadowPackage = "org/aion/avm/core/testdoubles/indy";
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ClassShadowing(CLASS_NAME, shadowPackage, ClassWhiteList.build(className)))
                .addNextVisitor(new InvokedynamicShadower(CLASS_NAME, shadowPackage, ClassWhiteList.build(className)))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        sharedClassLoader,
                        classHierarchy,
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

//    @Test
//    public void given_MultiLineLambda_then_itsTransformedAsUsualCode() {
//        final var className = LongLambda.class.getName();
//        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(getSlashClassNameFrom(className));
//        final byte[] transformedBytecode = transformForMultiLineLambda(origBytecode, className);
//        NeverCommitUtils.storeAndJavap(transformedBytecode, "transformed.txt");
//        Assert.assertFalse(Arrays.equals(origBytecode, transformedBytecode));
//        try {
//            final org.aion.avm.core.testdoubles.indy.Double actual =
//                    (org.aion.avm.core.testdoubles.indy.Double) callTestMethod(transformedBytecode, className, "avm_test");
//            assertEquals(100, actual.avm_doubleValue(), 0);
//            assertTrue(actual.avm_valueOfWasCalled);
//        } catch (Exception e) {
//            fail(e.getMessage());
//        }
//    }

    private byte[] transformForMultiLineLambda(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final var shadowPackage = "org/aion/avm/java/lang";
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ClassShadowing(CLASS_NAME, shadowPackage, ClassWhiteList.build(className)))
                .addNextVisitor(new InvokedynamicShadower(CLASS_NAME, shadowPackage, ClassWhiteList.build(className)))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        sharedClassLoader,
                        classHierarchy,
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    private static String getSlashClassNameFrom(String dotName) {
        return dotName.replaceAll("\\.", "/") + ".class";
    }

    private Object callTestMethod(byte[] bytecode, String className, String methodName) throws Exception {
        final var loader = new AvmSharedClassLoader(Map.of(className, bytecode));
        final Class<?> klass = loader.loadClass(className);
        final Constructor<?> constructor = klass.getDeclaredConstructor();
        final Object instance = constructor.newInstance();
        final Method method = klass.getDeclaredMethod(methodName);
        org.aion.avm.core.testdoubles.indy.invoke.LambdaMetafactory.avm_metafactoryWasCalled = false;
        return method.invoke(instance);
    }

    public static class TestingHelper {
        public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
            return Helper.wrapAsClass(input);
        }

        public static org.aion.avm.java.lang.String wrapAsString(String input) {
            return new org.aion.avm.java.lang.String(input);
        }
    }
}