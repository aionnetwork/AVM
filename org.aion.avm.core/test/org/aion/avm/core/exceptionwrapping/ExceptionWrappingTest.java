package org.aion.avm.core.exceptionwrapping;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.Forest;
import org.aion.avm.core.HierarchyTreeBuilder;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.ParentPointers;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.SuspendedHelper;
import org.aion.avm.core.TypeAwareClassWriter;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.PackageConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ExceptionWrappingTest {
    private AvmClassLoader loader;
    private Class<?> testClass;

    @Before
    public void setup() throws Exception {
        TestHelpers.didUnwrap = false;
        TestHelpers.didWrap = false;
        
        // We know that we have an exception, in this test, but the forest normally needs to be populated from a jar so manually assemble it.
        String exceptionClassDotName = TestExceptionResource.UserDefinedException.class.getName();
        String runtimeExceptionClassDotName = TestExceptionResource.UserDefinedRuntimeException.class.getName();
        String classDotName = TestExceptionResource.class.getName();
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(exceptionClassDotName, "java.lang.Throwable", null)
                .addClass(runtimeExceptionClassDotName, "java.lang.RuntimeException", null)
                .addClass(classDotName, "java.lang.Object", null)
                .asMutableForest();
        LazyWrappingTransformer transformer = new LazyWrappingTransformer(classHierarchy);
        
        String classSlashName = Helpers.fulllyQualifiedNameToInternalName(classDotName);
        byte[] raw = Helpers.loadRequiredResourceAsBytes(classSlashName + ".class");
        transformer.transformClass(classDotName, raw);
        
        byte[] exceptionBytes = Helpers.loadRequiredResourceAsBytes(exceptionClassDotName.replaceAll("\\.", "/") + ".class");
        transformer.transformClass(exceptionClassDotName, exceptionBytes);
        
        String runtimeExceptionClassSlashName = Helpers.fulllyQualifiedNameToInternalName(runtimeExceptionClassDotName);
        byte[] runtimeExceptionBytes = Helpers.loadRequiredResourceAsBytes(runtimeExceptionClassSlashName + ".class");
        transformer.transformClass(runtimeExceptionClassDotName, runtimeExceptionBytes);
        
        Map<String, byte[]> classes = new HashMap<>();
        classes.putAll(transformer.getLateGeneratedClasses());
        
        this.loader = NodeEnvironment.singleton.createInvocationClassLoader(classes);
        // Load the test class and the exceptions.
        this.testClass = this.loader.loadUserClassByOriginalName(classDotName);
        this.loader.loadUserClassByOriginalName(exceptionClassDotName);
        this.loader.loadUserClassByOriginalName(runtimeExceptionClassDotName);
        
        // We don't really need the runtime but we do need the intern map initialized.
        new Helper(this.loader, 1_000_000L, 1);
    }

    @After
    public void teardown() throws Exception {
        Helper.clearTestingState();
    }


    /**
     * Tests that a multi-catch, using only java/lang/* exception types, works correctly.
     */
    @Test
    public void testSimpleTryMultiCatchFinally() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method tryMultiCatchFinally = this.testClass.getMethod(UserClassMappingVisitor.mapMethodName("tryMultiCatchFinally"));
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didUnwrap);
        int result = (Integer) tryMultiCatchFinally.invoke(null);
        Assert.assertTrue(TestHelpers.didUnwrap);
        Assert.assertEquals(3, result);
    }

    /**
     * Tests that a manually creating and throwing a java/lang/* exception type works correctly.
     */
    @Test
    public void testmSimpleManuallyThrowNull() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method manuallyThrowNull = this.testClass.getMethod(UserClassMappingVisitor.mapMethodName("manuallyThrowNull"));
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didWrap);
        boolean didCatch = false;
        try {
            manuallyThrowNull.invoke(null);
        } catch (InvocationTargetException e) {
            // Make sure that this is the wrapper type that we normally expect to see.
            Class<?> compare = this.loader.loadClass(PackageConstants.kExceptionWrapperDotPrefix + "java.lang.NullPointerException");
            didCatch = e.getCause().getClass() == compare;
        }
        Assert.assertTrue(TestHelpers.didWrap);
        Assert.assertTrue(didCatch);
    }

    /**
     * Tests that we can correctly interact with exceptions from the java/lang/* hierarchy from within the catch block.
     */
    @Test
    public void testSimpleTryMultiCatchInteraction() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method tryMultiCatchFinally = this.testClass.getMethod(UserClassMappingVisitor.mapMethodName("tryMultiCatch"));
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didUnwrap);
        int result = (Integer) tryMultiCatchFinally.invoke(null);
        Assert.assertTrue(TestHelpers.didUnwrap);
        Assert.assertEquals(2, result);
    }

    /**
     * Tests that we can re-throw VM-generated exceptions and re-catch them.
     */
    @Test
    public void testRecatchCoreException() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method outerCatch = this.testClass.getMethod(UserClassMappingVisitor.mapMethodName("outerCatch"));
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didUnwrap);
        int result = (Integer) outerCatch.invoke(null);
        Assert.assertTrue(TestHelpers.didUnwrap);
        // 3 here will imply that the exception table wasn't re-written (since it only caught at the top-level Throwable).
        Assert.assertEquals(2, result);
    }

    /**
     * Tests that the user-defined exception is correctly parsed when loaded through the AvmImpl pipeline (since this unit
     * test stubs out some important callbacks).
     */
    @Test
    public void testExceptionTransformOnAvmImplPipeline() throws Exception {
        // This test doesn't use the common IHelper from setup() so uninstall it.
        SuspendedHelper suspended = new SuspendedHelper();
        
        SimpleAvm avm = new SimpleAvm(10000,
                TestExceptionResource.class,
                TestExceptionResource.UserDefinedRuntimeException.class,
                TestExceptionResource.UserDefinedException.class);
        Set<String> transformedClassNames = avm.getTransformedClassNames();
        
        // We expect this to have 2 exceptions in it:  the transformed user-defined exception and the generated wrapper.
        String exceptionClassDotName = TestExceptionResource.UserDefinedException.class.getName();
        Assert.assertEquals(3 + 2, transformedClassNames.size());
        Assert.assertTrue(transformedClassNames.contains(PackageConstants.kUserDotPrefix + exceptionClassDotName));
        Assert.assertTrue(transformedClassNames.contains(PackageConstants.kExceptionWrapperDotPrefix + exceptionClassDotName));
        avm.shutdown();
        suspended.resume();
    }

    /**
     * Tests User-defined exceptions can be thrown and caught
     */
    @Test
    public void testUserDefinedThrowCatch() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method userDefinedCatch = this.testClass.getMethod(UserClassMappingVisitor.mapMethodName("userDefinedCatch"));
        
        Object result = userDefinedCatch.invoke(null);
        // We should see the "two" string if this was thrown and caught.
        Assert.assertEquals("two", result.toString());
    }

    /**
     * Make sure that we are handling all these cases in the common pipeline, not just the unit test.
     */
    @Test
    public void testUserDefinedThrowCatch_commonPipeline() throws Exception {
        // This test doesn't use the common IHelper from setup() so uninstall it.
        SuspendedHelper suspended = new SuspendedHelper();
        
        SimpleAvm avm = new SimpleAvm(1000L
                , TestExceptionResource.class
                , TestExceptionResource.UserDefinedException.class
                , TestExceptionResource.UserDefinedRuntimeException.class
        );
        
        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestExceptionResource.class.getName());
        
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method userDefinedCatch = clazz.getMethod(UserClassMappingVisitor.mapMethodName("userDefinedCatch"));
        
        Object result = userDefinedCatch.invoke(null);
        // We should see the "two" string if this was thrown and caught.
        Assert.assertEquals("two", result.toString());
        avm.shutdown();
        suspended.resume();
    }

    /**
     * issue-141:  The case where we see the original exception, since nobody tries to catch it.
     */
    @Test
    public void testOriginalNull_commonPipeline() throws Exception {
        // This test doesn't use the common IHelper from setup() so uninstall it.
        SuspendedHelper suspended = new SuspendedHelper();
        
        SimpleAvm avm = new SimpleAvm(1000_000L
                , TestExceptionResource.class
                , TestExceptionResource.UserDefinedException.class
                , TestExceptionResource.UserDefinedRuntimeException.class
        );
        
        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestExceptionResource.class.getName());
        
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method originalNull = clazz.getMethod(UserClassMappingVisitor.mapMethodName("originalNull"));
        
        try {
            originalNull.invoke(null);
        } catch (InvocationTargetException e) {
            Assert.assertTrue(NullPointerException.class.getName().equals(e.getCause().getClass().getName()));
        }
        avm.shutdown();
        suspended.resume();
    }

    /**
     * issue-141:  The case where we see the remapped exception, since the user caught and re-threw it.
     */
    @Test
    public void testInnerCatch_commonPipeline() throws Exception {
        // This test doesn't use the common IHelper from setup() so uninstall it.
        SuspendedHelper suspended = new SuspendedHelper();
        
        SimpleAvm avm = new SimpleAvm(1000_000L
                , TestExceptionResource.class
                , TestExceptionResource.UserDefinedException.class
                , TestExceptionResource.UserDefinedRuntimeException.class
        );
        
        Class<?> clazz = avm.getClassLoader().loadUserClassByOriginalName(TestExceptionResource.class.getName());
        
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method originalNull = clazz.getMethod(UserClassMappingVisitor.mapMethodName("innerCatch"));
        
        try {
            originalNull.invoke(null);
        } catch (InvocationTargetException e) {
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + NullPointerException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        avm.shutdown();
        suspended.resume();
    }


    // Note that we will delegate to the common Helper class to ensure that we maintain overall correctness.
    public static class TestHelpers {
        public static final String CLASS_NAME = TestHelpers.class.getName().replaceAll("\\.", "/");
        public static int countWrappedClasses;
        public static int countWrappedStrings;
        public static boolean didUnwrap = false;
        public static boolean didWrap = false;
        
        public static <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
            countWrappedClasses += 1;
            return Helper.wrapAsClass(input);
        }
        public static org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
            countWrappedStrings += 1;
            return Helper.wrapAsString(input);
        }
        public static org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t) {
            didUnwrap = true;
            return Helper.unwrapThrowable(t);
        }
        public static Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg) {
            didWrap = true;
            return Helper.wrapAsThrowable(arg);
        }
    }


    /**
     * Allows us to build an incremental class loader, which is able to dynamically generate and load its own classes,
     * if it wishes to, such that we can request the final map of classes once everything has been loaded/generated.
     */
    private static class LazyWrappingTransformer {
        private final Forest<String, byte[]> classHierarchy;
        private final Map<String, byte[]> transformedClasses;
        
        public LazyWrappingTransformer(Forest<String, byte[]> classHierarchy) {
            this.classHierarchy = classHierarchy;
            this.transformedClasses = new HashMap<>();
        }
        
        public void transformClass(String name, byte[] inputBytes) {
            HierarchyTreeBuilder dynamicHierarchyBuilder = new HierarchyTreeBuilder();
            ExceptionWrapping.GeneratedClassConsumer generatedClassesSink = (slashSuperName, slashName, bytes) -> {
                String superDotName = Helpers.internalNameToFulllyQualifiedName(slashSuperName);
                String dotName = Helpers.internalNameToFulllyQualifiedName(slashName);
                LazyWrappingTransformer.this.transformedClasses.put(dotName, bytes);
                dynamicHierarchyBuilder.addClass(dotName, superDotName, bytes);
            };
            ParentPointers parentPointers = new ParentPointers(ClassWhiteList.extractDeclaredClasses(this.classHierarchy), this.classHierarchy);
            final ClassToolchain toolchain = new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                    .addNextVisitor(new UserClassMappingVisitor(ClassWhiteList.extractDeclaredClasses(this.classHierarchy)))
                    .addNextVisitor(new ConstantVisitor(TestHelpers.CLASS_NAME))
                    .addNextVisitor(new ExceptionWrapping(TestHelpers.CLASS_NAME, parentPointers, generatedClassesSink))
                    .addNextVisitor(new ClassShadowing(TestHelpers.CLASS_NAME))
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, parentPointers, dynamicHierarchyBuilder))
                    .build();
            this.transformedClasses.put(PackageConstants.kUserDotPrefix + name, toolchain.runAndGetBytecode());
        }
        
        public Map<String, byte[]> getLateGeneratedClasses() {
            return Collections.unmodifiableMap(this.transformedClasses);
        }
    }
}
