package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Roman Katerinenko
 */
public class InvokedynamicTransformationTest {
    private static final String HELPER_CLASS = "org/aion/avm/internal/Helper";
    private static final String SHADOW_PACKAGE = "org/aion/avm/core/testdoubles/indy";

//    @Test
//    public void tryOriginalLambda() throws IOException, InterruptedException {
//        final var className = ParametrizedLambda.class.getName();
//        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
//        storeAndJavap(origBytecode, "original.txt");
//        assertCanInstantiate(origBytecode, className);
//    }

    @Test
    public void given_parametrizedLambda_then_allUserAccessableObjectsShouldBeShadowed() {
        final var className = ParametrizedLambda.class.getName();
        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        final byte[] transformedBytecode = transform(origBytecode, className);
        // todo remove
        NeverCommitUtils.storeAndJavap(transformedBytecode, "transformed.txt");
        Assert.assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        assertCanInstantiate(transformedBytecode, className);
    }

//    @Test
//    public void given_MultiLineLambda_then_itsTransformedAsUsualCode() {
//        final var className = LongLambda.class.getName();
//        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
//        final byte[] transformedBytecode = transform(origBytecode, className);
//        Assert.assertFalse(Arrays.equals(origBytecode, transformedBytecode));
//        assertCanLoad(transformedBytecode, className);
//    }

//    private void assertCanLoad(byte[] bytecode, String className) {
//        final var loader = new AvmSharedClassLoader(Map.of(className, bytecode));
//        try {
//            loader.loadClass(className);
//        } catch (ClassNotFoundException e) {
//            fail(e.getMessage());
//        }
//    }

    private void assertCanInstantiate(byte[] bytecode, String className) {
        final var loader = new AvmSharedClassLoader(Map.of(className, bytecode));
        try {
            final Class<?> klass = loader.loadClass(className);
            final Constructor<?> constructor = klass.getDeclaredConstructor();
            final Object instance = constructor.newInstance();
            final Method method = klass.getDeclaredMethod("test");
            org.aion.avm.core.testdoubles.indy.invoke.LambdaMetafactory.avm_metafactoryWasCalled = false;
            final org.aion.avm.core.testdoubles.indy.Double actual = (org.aion.avm.core.testdoubles.indy.Double) method.invoke(instance);
            assertEquals(30, actual.avm_doubleValue(), 0);
            assertTrue(actual.avm_valueOfWasCalled);
            assertTrue(org.aion.avm.core.testdoubles.indy.invoke.LambdaMetafactory.avm_metafactoryWasCalled);
        } catch (Exception e) {
            e.printStackTrace(); // todo remove
            fail(e.getMessage());
        }
    }

    private byte[] transform(byte[] origBytecode, String className) {
        final AvmSharedClassLoader sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        final var avmClassLoader = new AvmClassLoader(sharedClassLoader, new HashMap<>());
        new Helper(avmClassLoader, new SimpleRuntime(new byte[0], new byte[0], 0));
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ClassShadowing(HELPER_CLASS, SHADOW_PACKAGE, ClassWhiteList.buildForEmptyContract()))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, sharedClassLoader, classHierarchy, new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }
}