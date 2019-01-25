package org.aion.avm.core.invokedynamic;

import org.aion.avm.core.*;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.testindy.java.lang.Double;
import org.aion.avm.core.testindy.java.lang.invoke.LambdaMetafactory;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.aion.avm.core.types.GeneratedClassConsumer;
import org.aion.avm.core.util.DebugNameResolver;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IRuntimeSetup;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.PackageConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;
import static org.junit.Assert.*;


public class InvokedynamicTransformationTest {
    private IInstrumentation instrumentation;
    // Note that not all tests use this.
    private IRuntimeSetup runtimeSetup;
    private boolean debugMode = false;

    @Before
    public void setup() {
        // Make sure that we bootstrap the NodeEnvironment before we install the instrumentation and attach the thread.
        Assert.assertNotNull(NodeEnvironment.singleton);
        this.instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(this.instrumentation);
    }

    @After
    public void teardown() {
        if (null != this.runtimeSetup) {
            InstrumentationHelpers.popExistingStackFrame(this.runtimeSetup);
        }
        InstrumentationHelpers.detachThread(this.instrumentation);
    }


    @Test
    public void given_parametrizedLambda_then_allUserAccessableObjectsShouldBeShadowed() throws Exception {
        final var className = ParametrizedLambda.class.getName();
        final byte[] origBytecode = loadRequiredResourceAsBytes(InvokedynamicUtils.getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForParametrizedLambdaTest(origBytecode, className);
        assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        final Double actual =
                (Double) callInstanceTestMethod(transformedBytecode, className, NamespaceMapper.mapMethodName("test"));
        assertEquals(30, actual.avm_doubleValue(), 0);
        assertTrue(actual.avm_valueOfWasCalled);
    }

    private byte[] transformForParametrizedLambdaTest(byte[] origBytecode, String className) {
        final Forest<String, ClassInfo> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", false, origBytecode)
                .asMutableForest();
        final var shadowPackage = "org/aion/avm/core/testindy/";
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new UserClassMappingVisitor(new NamespaceMapper(InvokedynamicUtils.buildSingletonAccessRules(classHierarchy, className), shadowPackage), debugMode))
                .addNextVisitor(new ConstantVisitor())
                .addNextVisitor(new ClassShadowing(shadowPackage))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        new ParentPointers(Collections.singleton(className), classHierarchy, debugMode),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    @Test
    public void given_MethodReference_then_itsTransformedAsUsual() throws Exception {
        final var testClassDotName = MethodReferenceTestResource.class.getName();
        final var originalBytecode = loadRequiredResourceAsBytes(InvokedynamicUtils.getSlashClassNameFrom(testClassDotName));
        final var transformedBytecode = transformForMethodReference(originalBytecode, testClassDotName);
        Assert.assertFalse(Arrays.equals(originalBytecode, transformedBytecode));
        final org.aion.avm.core.testindy.java.lang.Integer actual =
                (org.aion.avm.core.testindy.java.lang.Integer) callStaticTestMethod(transformedBytecode, testClassDotName, NamespaceMapper.mapMethodName("function"));
        assertEquals(MethodReferenceTestResource.VALUE.intValue(), actual.avm_intValue());
    }

    private byte[] transformForMethodReference(byte[] originalBytecode, String classDotName) {
        final Forest<String, ClassInfo> classHierarchy = new HierarchyTreeBuilder()
                .addClass(classDotName, "java.lang.Object", false, originalBytecode)
                .asMutableForest();
        final var shadowPackage = "org/aion/avm/core/testindy/";
        return new ClassToolchain.Builder(originalBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new UserClassMappingVisitor(new NamespaceMapper(InvokedynamicUtils.buildSingletonAccessRules(classHierarchy, classDotName), shadowPackage), debugMode))
                .addNextVisitor(new ConstantVisitor())
                .addNextVisitor(new ClassShadowing(shadowPackage))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        new ParentPointers(Collections.singleton(classDotName), classHierarchy, debugMode),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    @Test
    public void given_MultiLineLambda_then_itsTransformedAsUsualCode() throws Exception {
        final var className = LongLambda.class.getName();
        final byte[] origBytecode = Helpers.loadRequiredResourceAsBytes(InvokedynamicUtils.getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForMultiLineLambda(origBytecode, className);
        Assert.assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        final org.aion.avm.shadow.java.lang.Double actual =
                (org.aion.avm.shadow.java.lang.Double) callInstanceTestMethod(transformedBytecode, className, NamespaceMapper.mapMethodName("test"));
        assertEquals(100., actual.avm_doubleValue(), 0);
    }

    private byte[] transformForMultiLineLambda(byte[] origBytecode, String className) {
        final Forest<String, ClassInfo> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", false, origBytecode)
                .asMutableForest();
        final var shadowPackage = PackageConstants.kShadowSlashPrefix;
        final Map<String, byte[]> processedClasses = new HashMap<>();
        // WARNING:  This dynamicHierarchyBuilder is both mutable and shared by TypeAwareClassWriter instances.
        final HierarchyTreeBuilder dynamicHierarchyBuilder = new HierarchyTreeBuilder();
        final GeneratedClassConsumer generatedClassConsumer = (superClassSlashName, classSlashName, bytecode) -> {
            // Note that the processed classes are expected to use .-style names.
            String classDotName = Helpers.internalNameToFulllyQualifiedName(classSlashName);
            processedClasses.put(classDotName, bytecode);
            dynamicHierarchyBuilder.addClass(classSlashName, superClassSlashName, false, bytecode);
        };
        ParentPointers parentPointers = new ParentPointers(Collections.singleton(className), classHierarchy, debugMode);
        PreRenameClassAccessRules singletonRules = InvokedynamicUtils.buildSingletonAccessRules(classHierarchy, className);
        NamespaceMapper mapper = new NamespaceMapper(singletonRules);
        byte[] bytecode = new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new RejectionClassVisitor(singletonRules, mapper, debugMode))
                .addNextVisitor(new UserClassMappingVisitor(mapper, debugMode))
                .addNextVisitor(new ConstantVisitor())
                .addNextVisitor(new ClassMetering(DAppCreator.computeAllPostRenameObjectSizes(classHierarchy, debugMode)))
                .addNextVisitor(new ClassShadowing(shadowPackage))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addNextVisitor(new StackWatcherClassAdapter())
                .addNextVisitor(new ExceptionWrapping(parentPointers, generatedClassConsumer, debugMode))
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

    private Object callInstanceTestMethod(byte[] bytecode, String className, String methodName) throws Exception {
        String mappedClassName = DebugNameResolver.getUserPackageDotPrefix(className, debugMode);//PackageConstants.kUserDotPrefix + className;
        final Class<?> klass = loadClassInAvmLoader(bytecode, mappedClassName);

        final Constructor<?> constructor = klass.getDeclaredConstructor();
        final Object instance = constructor.newInstance();
        final Method method = klass.getDeclaredMethod(methodName);
        LambdaMetafactory.avm_metafactoryWasCalled = false;
        return method.invoke(instance);
    }

    private Object callStaticTestMethod(byte[] bytecode, String className, String methodName) throws Exception {
        String mappedClassName = DebugNameResolver.getUserPackageDotPrefix(className, debugMode);
        final Class<?> klass = loadClassInAvmLoader(bytecode, mappedClassName);
        
        final Method method = klass.getMethod(methodName, IObject.class);
        LambdaMetafactory.avm_metafactoryWasCalled = false;
        return method.invoke(null, new org.aion.avm.shadow.java.lang.Object());
    }


    private Class<?> loadClassInAvmLoader(byte[] bytecode, String mappedClassName) throws Exception {
        Map<String, byte[]> classAndHelper = Helpers.mapIncludingHelperBytecode(Map.of(mappedClassName, bytecode), Helpers.loadDefaultHelperBytecode());
        AvmClassLoader dappLoader = NodeEnvironment.singleton.createInvocationClassLoader(classAndHelper);
        // Here, we will construct the runtime setup and push the new stack frame (we check if this is set to pop the frame, later - kind of hackish but
        // avoids a lot of plumbing for a couple unit tests).
        Assert.assertNull(this.runtimeSetup);
        this.runtimeSetup = Helpers.getSetupForLoader(dappLoader);
        InstrumentationHelpers.pushNewStackFrame(this.runtimeSetup, dappLoader, 1_000_000L, 1);
        return dappLoader.loadClass(mappedClassName);
    }
}
