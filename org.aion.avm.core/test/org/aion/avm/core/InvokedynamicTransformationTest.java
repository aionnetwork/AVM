package org.aion.avm.core;

import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.testindy.java.lang.Double;
import org.aion.avm.core.testindy.java.lang.invoke.LambdaMetafactory;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
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

    @BeforeClass
    public static void init() {
        final var avmClassLoader = NodeEnvironment.singleton.createInvocationClassLoader(Collections.emptyMap());
        new Helper(avmClassLoader, 1_000_000L, 1);
    }

    @Test
    public void given_stringConcatLambda_then_bootstrapMethodShouldNeShadowed() throws Exception {
        final var className = ConstantStringsConcatIndy.class.getName();
        final byte[] origBytecode = loadRequiredResourceAsBytes(getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForStringConcatTest(origBytecode, className);
        assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        final var actual = (org.aion.avm.shadow.java.lang.String) callTestMethod(transformedBytecode, className, UserClassMappingVisitor.mapMethodName("test"));
        Assert.assertEquals("abc", actual.toString());
    }

    private byte[] transformForStringConcatTest(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final var shadowPackage = PackageConstants.kShadowSlashPrefix;
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new UserClassMappingVisitor(ClassWhiteList.extractDeclaredClasses(classHierarchy)))
                .addNextVisitor(new ConstantVisitor(HELPER_CLASS_NAME))
                .addNextVisitor(new ClassShadowing(HELPER_CLASS_NAME))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        new ParentPointers(Collections.singleton(className), classHierarchy),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    @Test
    public void given_parametrizedLambda_then_allUserAccessableObjectsShouldBeShadowed() throws Exception {
        final var className = ParametrizedLambda.class.getName();
        final byte[] origBytecode = loadRequiredResourceAsBytes(getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForParametrizedLambdaTest(origBytecode, className);
        assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        final Double actual =
                (Double) callTestMethod(transformedBytecode, className, UserClassMappingVisitor.mapMethodName("test"));
        assertEquals(30, actual.avm_doubleValue(), 0);
        assertTrue(actual.avm_valueOfWasCalled);
    }

    private byte[] transformForParametrizedLambdaTest(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final var shadowPackage = "org/aion/avm/core/testindy/";
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new UserClassMappingVisitor(ClassWhiteList.extractDeclaredClasses(classHierarchy), shadowPackage))
                .addNextVisitor(new ConstantVisitor(HELPER_CLASS_NAME))
                .addNextVisitor(new ClassShadowing(HELPER_CLASS_NAME, shadowPackage))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        new ParentPointers(Collections.singleton(className), classHierarchy),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    @Test
    public void given_MultiLineLambda_then_itsTransformedAsUsualCode() throws Exception {
        final var className = LongLambda.class.getName();
        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForMultiLineLambda(origBytecode, className);
        Assert.assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        final org.aion.avm.shadow.java.lang.Double actual =
                (org.aion.avm.shadow.java.lang.Double) callTestMethod(transformedBytecode, className, UserClassMappingVisitor.mapMethodName("test"));
        assertEquals(100., actual.avm_doubleValue(), 0);
    }

    private byte[] transformForMultiLineLambda(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final var shadowPackage = PackageConstants.kShadowSlashPrefix;
        Helper.setEnergy(1_000_000L);
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
                .addNextVisitor(new RejectionClassVisitor())
                .addNextVisitor(new UserClassMappingVisitor(ClassWhiteList.extractDeclaredClasses(classHierarchy)))
                .addNextVisitor(new ConstantVisitor(HELPER_CLASS_NAME))
                .addNextVisitor(new ClassMetering(HELPER_CLASS_NAME, DAppCreator.computeAllPostRenameObjectSizes(classHierarchy)))
                .addNextVisitor(new ClassShadowing(HELPER_CLASS_NAME, shadowPackage))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addNextVisitor(new StackWatcherClassAdapter())
                .addNextVisitor(new ExceptionWrapping(HELPER_CLASS_NAME, parentPointers, generatedClassConsumer))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, parentPointers, dynamicHierarchyBuilder))
                .build()
                .runAndGetBytecode();
        bytecode = new ClassToolchain.Builder(bytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ArrayWrappingClassAdapterRef())
                .addNextVisitor(new ArrayWrappingClassAdapter())
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, parentPointers, dynamicHierarchyBuilder))
                .build()
                .runAndGetBytecode();
        return bytecode;
    }

    private static String getSlashClassNameFrom(String dotName) {
        return dotName.replaceAll("\\.", "/") + ".class";
    }

    private Object callTestMethod(byte[] bytecode, String className, String methodName) throws Exception {
        String mappedClassName = PackageConstants.kUserDotPrefix + className;
        final AvmSharedClassLoader loader = new AvmSharedClassLoader(Map.of(mappedClassName, bytecode));
        final Class<?> klass = loader.loadClass(mappedClassName);
        final Constructor<?> constructor = klass.getDeclaredConstructor();
        final Object instance = constructor.newInstance();
        final Method method = klass.getDeclaredMethod(methodName);
        LambdaMetafactory.avm_metafactoryWasCalled = false;
        return method.invoke(instance);
    }

    public static class TestingHelper {
        public static <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
            return Helper.wrapAsClass(input);
        }

        public static org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
            return new org.aion.avm.shadow.java.lang.String(input);
        }
    }
}