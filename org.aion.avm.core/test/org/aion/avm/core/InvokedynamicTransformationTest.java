package org.aion.avm.core;

import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.miscvisitors.StringConstantVisitor;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.rt.Address;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;
import static org.junit.Assert.*;

/**
 * @author Roman Katerinenko
 */
public class InvokedynamicTransformationTest {
    private static String HELPER_CLASS_NAME = PackageConstants.kInternalSlashPrefix + "Helper";
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void init() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        final var avmClassLoader = new AvmClassLoader(sharedClassLoader, new HashMap<>());
        new Helper(avmClassLoader, new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 0));
    }

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
        final var shadowPackage = PackageConstants.kShadowJavaLangSlashPrefix;
        final var classWhiteList = ClassWhiteList.build(className);
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ClassShadowing(HELPER_CLASS_NAME, classWhiteList))
                .addNextVisitor(new InvokedynamicShadower(HELPER_CLASS_NAME, shadowPackage, classWhiteList))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        sharedClassLoader,
                        new ParentPointers(Collections.singleton(className), classHierarchy),
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
        final var shadowPackage = "org/aion/avm/core/testdoubles/indy/";
        final var classWhiteList = ClassWhiteList.build(className);
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ClassShadowing(HELPER_CLASS_NAME, shadowPackage, classWhiteList))
                .addNextVisitor(new InvokedynamicShadower(HELPER_CLASS_NAME, shadowPackage, classWhiteList))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        sharedClassLoader,
                        new ParentPointers(Collections.singleton(className), classHierarchy),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    @Test
    public void given_MultiLineLambda_then_itsTransformedAsUsualCode() {
        final var className = LongLambda.class.getName();
        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForMultiLineLambda(origBytecode, className);
        Assert.assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        try {
            final org.aion.avm.java.lang.Double actual =
                    (org.aion.avm.java.lang.Double) callTestMethod(transformedBytecode, className, "avm_test");
            assertEquals(100., actual.avm_doubleValue(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private byte[] transformForMultiLineLambda(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final var shadowPackage = PackageConstants.kShadowJavaLangSlashPrefix;
        Helper.setEnergy(1000);
        final var classWhiteList = ClassWhiteList.build(className);
        final Map<String, byte[]> processedClasses = new HashMap<>();
        // WARNING:  This dynamicHierarchyBuilder is both mutable and shared by TypeAwareClassWriter instances.
        final HierarchyTreeBuilder dynamicHierarchyBuilder = new HierarchyTreeBuilder();
        final ExceptionWrapping.GeneratedClassConsumer generatedClassConsumer = (superClassSlashName, classSlashName, bytecode) -> {
            // Note that the processed classes are expected to use .-style names.
            String classDotName = Helpers.internalNameToFulllyQualifiedName(classSlashName);
            processedClasses.put(classDotName, bytecode);
            dynamicHierarchyBuilder.addClass(classSlashName, superClassSlashName, bytecode);
        };
        ParentPointers parentPointers = new ParentPointers(Collections.singleton(className), classHierarchy);
        byte[] bytecode = new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new RejectionClassVisitor(classWhiteList))
                .addNextVisitor(new StringConstantVisitor())
                .addNextVisitor(new ClassMetering(HELPER_CLASS_NAME, AvmImpl.computeAllObjectsSizes(classHierarchy)))
                .addNextVisitor(new ClassShadowing(HELPER_CLASS_NAME, shadowPackage, classWhiteList))
                .addNextVisitor(new InvokedynamicShadower(HELPER_CLASS_NAME, shadowPackage, classWhiteList))
                .addNextVisitor(new StackWatcherClassAdapter())
                .addNextVisitor(new ExceptionWrapping(HELPER_CLASS_NAME, parentPointers, generatedClassConsumer))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, sharedClassLoader, parentPointers, dynamicHierarchyBuilder))
                .build()
                .runAndGetBytecode();
        bytecode = new ClassToolchain.Builder(bytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ArrayWrappingClassAdapterRef())
                .addNextVisitor(new ArrayWrappingClassAdapter())
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, sharedClassLoader, parentPointers, dynamicHierarchyBuilder))
                .build()
                .runAndGetBytecode();
        return bytecode;
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