package org.aion.avm.core.invokedynamic;

import org.aion.avm.core.*;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IRuntimeSetup;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.PackageConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.aion.avm.core.invokedynamic.InvokedynamicUtils.buildSingletonAccessRules;
import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;
import static org.junit.Assert.assertFalse;


public class StringConcatenationTest {
    private IInstrumentation instrumentation;
    // Note that not all tests use this.
    private IRuntimeSetup runtimeSetup;
    private static boolean debugMode;

    @Before
    public void setup() {
        // Make sure that we bootstrap the NodeEnvironment before we install the instrumentation and attach the thread.
        Assert.assertNotNull(NodeEnvironment.singleton);
        this.instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(this.instrumentation);
        debugMode = false;
    }

    @After
    public void teardown() {
        if (null != this.runtimeSetup) {
            InstrumentationHelpers.popExistingStackFrame(this.runtimeSetup);
        }
        InstrumentationHelpers.detachThread(this.instrumentation);
    }

    @Test
    public void given_null_then_NullShouldBeConcatenated() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_concatWithDynamicArgs", null, null, null);
        Assert.assertEquals("nullnullnull", actual);
    }

    @Test
    public void given_nullReference_then_NullShouldBeConcatenated() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_nullReferenceConcat");
        Assert.assertEquals("null", actual);
    }

    @Test
    public void given_twoEmptyStrings_then_EmptyStringShouldBeReturned() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_emptyStringsConcat");
        Assert.assertEquals("", actual);
    }

    @Test
    public void given_stringConcatLambda_then_bootstrapMethodShouldBeShadowed() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_concatWithDynamicArgs",
                new org.aion.avm.shadow.java.lang.String("a"),
                new org.aion.avm.shadow.java.lang.String("b"),
                new org.aion.avm.shadow.java.lang.String("c"));
        Assert.assertEquals("abc", actual);
    }

    @Test
    public void given_stringConcatLambda_then_bootstrapMethodShouldBeShadowed2() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_concatWithCharacters",
                new org.aion.avm.shadow.java.lang.String("a"),
                new org.aion.avm.shadow.java.lang.String("b"),
                new org.aion.avm.shadow.java.lang.String("c"));
        Assert.assertEquals("yabcx12.8", actual);
    }

    @Test
    public void given_allPrimitives_then_shouldBeConcatenated() throws Exception {
        final var clazz = transformClass(IndyConcatenationTestResource.class);
        final var actual = callMethod(clazz, "avm_concatWithPrimtives");
        Assert.assertEquals("0121.10.0atrue3", actual);
    }

    private static String callMethod(Class<?> clazz,
                                     String methodName, org.aion.avm.shadow.java.lang.String... stringArgs) throws Exception {
        final var instance = clazz.getDeclaredConstructor().newInstance();
        final var method = clazz.getDeclaredMethod(methodName, getTypesFrom(stringArgs));
        final var actual = (org.aion.avm.shadow.java.lang.String) method.invoke(instance, (Object[])stringArgs);
        return actual.getUnderlying();
    }

    private static Class<?>[] getTypesFrom(Object[] objects) {
        return Stream.of(objects)
                .map((obj) -> (obj == null) ? org.aion.avm.shadow.java.lang.String.class : obj.getClass())
                .toArray(Class[]::new);
    }

    private Class<?> transformClass(Class<?> clazz) throws Exception {
        final var className = clazz.getName();
        final byte[] origBytecode = loadRequiredResourceAsBytes(InvokedynamicUtils.getSlashClassNameFrom(className));
        final byte[] transformedBytecode = transformForStringConcatTest(origBytecode, className);
        assertFalse(Arrays.equals(origBytecode, transformedBytecode));
        java.lang.String mappedClassName = PackageConstants.kUserDotPrefix + className;
        Map<String, byte[]> classAndHelper = Helpers.mapIncludingHelperBytecode(Map.of(mappedClassName, transformedBytecode), Helpers.loadDefaultHelperBytecode());
        AvmClassLoader dappLoader = NodeEnvironment.singleton.createInvocationClassLoader(classAndHelper);
        
        this.runtimeSetup = Helpers.getSetupForLoader(dappLoader);
        InstrumentationHelpers.pushNewStackFrame(this.runtimeSetup, dappLoader, 1_000_000L, 1);
        return dappLoader.loadClass(mappedClassName);
    }

    private static byte[] transformForStringConcatTest(byte[] origBytecode, String className) {
        final Forest<String, ClassInfo> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", false, origBytecode)
                .asMutableForest();
        final var shadowPackage = PackageConstants.kShadowSlashPrefix;
        return new ClassToolchain.Builder(origBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new UserClassMappingVisitor(new NamespaceMapper(buildSingletonAccessRules(classHierarchy, className)), debugMode))
                .addNextVisitor(new ConstantVisitor())
                .addNextVisitor(new ClassShadowing(shadowPackage))
                .addNextVisitor(new InvokedynamicShadower(shadowPackage))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        new ParentPointers(Collections.singleton(className), classHierarchy, debugMode),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }
}