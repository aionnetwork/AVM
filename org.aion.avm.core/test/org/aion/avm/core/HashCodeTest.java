package org.aion.avm.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.rt.Address;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests the hashCode behaviour of the contract code.  Includes a tests that our helpers/instrumentation don't invalidate Java assumptions.
 */
public class HashCodeTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() throws Exception {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    private Class<?> clazz;

    @Before
    public void setup() throws Exception {
        SimpleRuntime externalRuntime = new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 10000);
        SimpleAvm avm = new SimpleAvm(externalRuntime, HashCodeTestTarget.class);
        AvmClassLoader loader = avm.getClassLoader();
        
        this.clazz = loader.loadUserClassByOriginalName(HashCodeTestTarget.class.getName());
        Assert.assertEquals(loader, this.clazz.getClassLoader());
    }

    /**
     * Tests that we can invoke the entire transformation pipeline for a basic test.
     * This test just verifies that we can do the transformation, not that the transformed class is correct or can be run.
     * Arguably, this test belongs in AvmImplTest, but it acts on a common test target and environmental shape as the other tests here
     * so we will build it here in the hopes that it informs the same evolution of common testing infrastructure.
     */
    @Test
    public void testBasicTranslation() throws Exception {
        Assert.assertNotNull(clazz);
    }

    @Test
    public void testCommonHash() throws Exception {
        Assert.assertNotNull(clazz);
        Method getOneHashCode = clazz.getMethod("avm_getOneHashCode");
        
        Object result = getOneHashCode.invoke(null);
        Assert.assertEquals(1, ((Integer)result).intValue());
        result = getOneHashCode.invoke(null);
        Assert.assertEquals(2, ((Integer)result).intValue());
        clazz.getConstructor().newInstance();
        result = getOneHashCode.invoke(null);
        Assert.assertEquals(4, ((Integer)result).intValue());
    }

    /**
     * Tests that requesting the same string constant, more than once, returns the same instance.
     */
    @Test
    public void testStringConstant() throws Exception {
        Assert.assertNotNull(clazz);
        Method getStringConstant = clazz.getMethod("avm_getStringConstant");
        
        Object instance1 = getStringConstant.invoke(null);
        Object instance2 = getStringConstant.invoke(null);
        Assert.assertTrue(instance1 == instance2);
    }

    /**
     * Tests that the string hashcode of our wrapper gives us the actual string's hashcode.
     */
    @Test
    public void testStringHashCode() throws Exception {
        Assert.assertNotNull(clazz);
        Method getStringConstant = clazz.getMethod("avm_getStringConstant");
        Method getStringHash = clazz.getMethod("avm_getStringHash");
        
        Object instance1 = getStringConstant.invoke(null);
        Object hash = getStringHash.invoke(null);
        // Make sure that the hashcode, as seen within the contract is correct.
        Assert.assertTrue(instance1.toString().hashCode() == ((Integer)hash).intValue());
        // Make sure that the hashcode, as seen within the our runtime is correct.
        Assert.assertTrue(instance1.hashCode() == ((Integer)hash).intValue());
    }

    /**
     * Tests that requesting the same class constant, more than once, returns the same instance.
     */
    @Test
    public void testClassConstant() throws Exception {
        Assert.assertNotNull(clazz);
        Method getClassConstant = clazz.getMethod("avm_getClassConstant");
        
        Object instance1 = getClassConstant.invoke(null);
        Object instance2 = getClassConstant.invoke(null);
        Assert.assertTrue(instance1 == instance2);
    }

    /**
     * Tests that re-throwing a VM-generated exception results in the same contract-visible exception instance.
     */
    @Test
    public void testVmExceptionInstancePreserved() throws Exception {
        Assert.assertNotNull(clazz);
        Method matchRethrowVmException = clazz.getMethod("avm_matchRethrowVmException");
        
        Object instance1 = matchRethrowVmException.invoke(null);
        Assert.assertTrue(((Boolean)instance1).booleanValue());
    }

    /**
     * Tests that Class.getName() instance return behaviour is consistent between normal Java and our contract environment.
     */
    @Test
    public void testClassGetName() throws Exception {
        Assert.assertNotNull(clazz);
        Method compareClassName = clazz.getMethod("avm_compareClassName");
        
        Object instance1 = compareClassName.invoke(null);
        boolean didMatchInContract = ((Boolean)instance1).booleanValue();
        boolean didMatchInJava = HashCodeTestTarget.compareClassName();
        Assert.assertEquals(didMatchInJava, didMatchInContract);
    }

    /**
     * Tests that String.toString() instance return behaviour is consistent between normal Java and our contract environment.
     */
    @Test
    public void testStringToString() throws Exception {
        Assert.assertNotNull(clazz);
        Method compareStringString = clazz.getMethod("avm_compareStringString");
        
        Object instance1 = compareStringString.invoke(null);
        boolean didMatchInContract = ((Boolean)instance1).booleanValue();
        boolean didMatchInJava = HashCodeTestTarget.compareStringString();
        Assert.assertEquals(didMatchInJava, didMatchInContract);
    }

    /**
     * Tests that an override of hashCode() is called.
     */
    @Test
    public void testOverrideHashcode() throws Exception {
        Assert.assertNotNull(clazz);
        Method getOverrideHashCode = clazz.getMethod("avm_getOverrideHashCode", int.class);
        
        int override = 5;
        Object instance1 = getOverrideHashCode.invoke(null, Integer.valueOf(override));
        int result = ((Integer)instance1).intValue();
        Assert.assertEquals(override, result);
    }

    /**
     * Tests the difference in behaviour between the shared loader and the per-contract loader.
     */
    @Test
    public void testTwoLevelClassLoading() throws Exception {
        // Create the shared instance with the reusable classes.
        AvmSharedClassLoader sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        
        // Load the testing class.
        String className = HashCodeTestTarget.class.getName();
        byte[] transformed = getTransformedTestClass(className);
        Map<String, byte[]> classes = Collections.singletonMap(PackageConstants.kUserDotPrefix + className, transformed);
        
        // Create 2 instances of the contract-specific loaders, each with the same class, and prove that we get 2 instances.
        AvmClassLoader loader1 = new AvmClassLoader(sharedClassLoader, classes);
        Class<?> clazz1 = loader1.loadUserClassByOriginalName(className);
        AvmClassLoader loader2 = new AvmClassLoader(sharedClassLoader, classes);
        Class<?> clazz2 = loader2.loadUserClassByOriginalName(className);
        // -not the same instances.
        Assert.assertFalse(clazz1 == clazz2);
        // -but reloading one of them gives us the same one back.
        Assert.assertTrue(loader2.loadUserClassByOriginalName(className) == clazz2);
        
        // Load a shared class, via each contract-specific loader, and ensure that we get the same instance.
        String classToLoad = PackageConstants.kShadowJavaLangDotPrefix + "Error";
        Class<?> match1 = loader1.loadClass(classToLoad);
        Class<?> match2 = loader2.loadClass(classToLoad);
        Assert.assertTrue(match1 == match2);
    }

    /**
     * Tests that we can load 2 contracts, execute a bunch of code in them, and see their implications, independently.
     * To avoid needing to manage threads, we will test these, sequentially.
     */
    @Test
    public void testTwoIsolatedContracts() throws Exception {
        // Create the shared instance with the reusable classes.
        AvmSharedClassLoader sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        
        // Load the testing class and the Helper.
        String targetClassName = HashCodeTestTarget.class.getName();
        byte[] transformedTarget = getTransformedTestClass(targetClassName);
        Map<String, byte[]> classes = Helpers.mapIncludingHelperBytecode(Collections.singletonMap(PackageConstants.kUserDotPrefix + targetClassName, transformedTarget));
        
        // We need a common runtime.
        SimpleRuntime commonRuntime = new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 10000);
        
        // Now, we will create 2 class loaders with the same classes:  these will be contract-level loaders.
        AvmClassLoader loader1 = new AvmClassLoader(sharedClassLoader, classes);
        AvmClassLoader loader2 = new AvmClassLoader(sharedClassLoader, classes);
        
        // First, run some tests in helper1.
        IHelper helper1 = Helpers.instantiateHelper(loader1, commonRuntime);
        Class<?> clazz1 = loader1.loadUserClassByOriginalName(targetClassName);
        Method getOneHashCode1 = clazz1.getMethod("avm_getOneHashCode");
        Object result = getOneHashCode1.invoke(null);
        Assert.assertEquals(1, ((Integer)result).intValue());
        result = getOneHashCode1.invoke(null);
        Assert.assertEquals(2, ((Integer)result).intValue());
        Assert.assertEquals(3, helper1.externalGetNextHashCode());
        
        // Now, create the helper2, show that it is independent, and run a test in that.
        IHelper helper2 = Helpers.instantiateHelper(loader2, commonRuntime);
        Class<?> clazz2 = loader2.loadUserClassByOriginalName(targetClassName);
        Method getOneHashCode2 = clazz2.getMethod("avm_getOneHashCode");
        Assert.assertEquals(1, helper2.externalGetNextHashCode());
        result = getOneHashCode2.invoke(null);
        Assert.assertEquals(2, ((Integer)result).intValue());
    }

    /**
     * Tests that the user code can't catch the OutOfEnergyError and bury it.
     */
    @Test
    public void testExhaustion() throws Exception {
        Method runUntilExhausted = this.clazz.getMethod("avm_runUntilExhausted");
        
        boolean caught = false;
        try {
            runUntilExhausted.invoke(null);
        } catch (InvocationTargetException e) {
            // Expected.
            Throwable cause = e.getCause();
            Assert.assertEquals(OutOfEnergyError.class, cause.getClass());
            caught = true;
        }
        Assert.assertTrue(caught);
    }


    private byte[] getTransformedTestClass(String className) {
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", raw)
                .asMutableForest();
        Map<String, Integer> runtimeObjectSizes = AvmImpl.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = AvmImpl.computeObjectSizes(classHierarchy, runtimeObjectSizes);
        Map<String, byte[]> transformedClasses = avm.transformClasses(Collections.singletonMap(className, raw), classHierarchy, allObjectSizes);
        
        // Note that the class is renamed during this transformation.
        return transformedClasses.get(PackageConstants.kUserDotPrefix + className);
    }
}
