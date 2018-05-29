package org.aion.avm.core;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.After;
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
        String className = HashCodeTestTarget.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", raw)
                .asMutableForest();
        
        AvmImpl avm = new AvmImpl();
        Map<String, Integer> runtimeObjectSizes = avm.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = avm.computeObjectSizes(classHierarchy, runtimeObjectSizes);
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            return avm.transformClasses(Collections.singletonMap(className, inputBytes), classHierarchy, allObjectSizes).get(className);
        };
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, transformer.apply(raw));
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        this.clazz = loader.loadClass(className);
        Assert.assertEquals(loader, this.clazz.getClassLoader());
        Helper.setLateClassLoader(loader);
        SimpleRuntime rt = new SimpleRuntime(null, null, 10000);
        Helper.setBlockchainRuntime(rt);
    }

    @After
    public void teardown() throws Exception {
        Helper.clearTestingState();
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
        Method getOneHashCode = clazz.getMethod("getOneHashCode");
        
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
        Method getStringConstant = clazz.getMethod("getStringConstant");
        
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
        Method getStringConstant = clazz.getMethod("getStringConstant");
        Method getStringHash = clazz.getMethod("getStringHash");
        
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
        Method getClassConstant = clazz.getMethod("getClassConstant");
        
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
        Method matchRethrowVmException = clazz.getMethod("matchRethrowVmException");
        
        Object instance1 = matchRethrowVmException.invoke(null);
        Assert.assertTrue(((Boolean)instance1).booleanValue());
    }

    /**
     * Tests that Class.getName() instance return behaviour is consistent between normal Java and our contract environment.
     */
    @Test
    public void testClassGetName() throws Exception {
        Assert.assertNotNull(clazz);
        Method compareClassName = clazz.getMethod("compareClassName");
        
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
        Method compareStringString = clazz.getMethod("compareStringString");
        
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
        Method getOverrideHashCode = clazz.getMethod("getOverrideHashCode", int.class);
        
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
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        AvmImpl avm = new AvmImpl();
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", raw)
                .asMutableForest();
        Map<String, Integer> runtimeObjectSizes = avm.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = avm.computeObjectSizes(classHierarchy, runtimeObjectSizes);
        byte[] transformed = avm.transformClasses(Collections.singletonMap(className, raw), classHierarchy, allObjectSizes).get(className);
        Map<String, byte[]> classes = Collections.singletonMap(className, transformed);
        
        // Create 2 instances of the contract-specific loaders, each with the same class, and prove that we get 2 instances.
        AvmClassLoader loader1 = new AvmClassLoader(sharedClassLoader, classes);
        Class<?> clazz1 = loader1.loadClass(className);
        Assert.assertEquals(loader1, clazz1.getClassLoader());
        AvmClassLoader loader2 = new AvmClassLoader(sharedClassLoader, classes);
        Class<?> clazz2 = loader2.loadClass(className);
        Assert.assertEquals(loader2, clazz2.getClassLoader());
        // -not the same instances.
        Assert.assertFalse(clazz1 == clazz2);
        // -but reloading one of them gives us the same one back.
        Assert.assertTrue(loader2.loadClass(className) == clazz2);
        
        // Load a shared class, via each contract-specific loader, and ensure that we get the same instance.
        String classToLoad = "org.aion.avm.java.lang.Error";
        Class<?> match1 = loader1.loadClass(classToLoad);
        Class<?> match2 = loader2.loadClass(classToLoad);
        Assert.assertTrue(match1 == match2);
    }
}
