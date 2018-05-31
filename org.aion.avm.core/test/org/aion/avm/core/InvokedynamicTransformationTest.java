package org.aion.avm.core;

import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Katerinenko
 */
public class InvokedynamicTransformationTest {
    private static final String HELPER_CLASS = "org/aion/avm/internal/Helper";

    private final AvmSharedClassLoader sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());

    @Test
    public void given_MultiLineLambda_then_itsTransformedAsUsualCode() {
        final var className = LongLambda.class.getName();
        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        final byte[] transformedBytecode = transform(origBytecode, className);
        Assert.assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        assertCanLoad(transformedBytecode, className);
    }

    private void assertCanLoad(byte[] bytecode, String className) {
        final var loader = new AvmSharedClassLoader(Map.of(className, bytecode));
        try {
            loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            Assert.fail(e.getMessage());
        }
    }

    private byte[] transform(byte[] origBytecode, String className) {
        final Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", origBytecode)
                .asMutableForest();
        final AvmImpl avm = new AvmImpl(sharedClassLoader);
        final var runtimeObjectSizes = new HashMap<>(AvmImpl.computeRuntimeObjectSizes());
        final var anyCost = 10;
        runtimeObjectSizes.put("java/lang/IllegalStateException", anyCost);
        final Map<String, Integer> allObjectSizes = avm.computeObjectSizes(new Forest<>(), runtimeObjectSizes);
        return transformClasses(origBytecode, classHierarchy, allObjectSizes);
    }

    private byte[] transformClasses(byte[] origBytecode, Forest<String, byte[]> classHierarchy, Map<String, Integer> objectSizes) {
        HierarchyTreeBuilder dynamicHierarchyBuilder = new HierarchyTreeBuilder();
        ExceptionWrapping.GeneratedClassConsumer generatedClassesSink = (superClassSlashName, classSlashName, bytecode) -> {
        };
        byte[] newBytecode = new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ClassMetering(HELPER_CLASS, objectSizes))
                .addNextVisitor(new StackWatcherClassAdapter())
                .addNextVisitor(new ClassShadowing(HELPER_CLASS))
                .addNextVisitor(new ExceptionWrapping(HELPER_CLASS, classHierarchy, generatedClassesSink))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, sharedClassLoader, classHierarchy, dynamicHierarchyBuilder))
                .build()
                .runAndGetBytecode();
        newBytecode = new ClassToolchain.Builder(newBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new ArrayWrappingClassAdapterRef())
                .addNextVisitor(new ArrayWrappingClassAdapter())
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, sharedClassLoader, classHierarchy, dynamicHierarchyBuilder))
                .build()
                .runAndGetBytecode();
        return newBytecode;
    }
}