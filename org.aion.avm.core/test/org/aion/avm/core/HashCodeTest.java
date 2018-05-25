package org.aion.avm.core;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.internal.Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the hashCode behaviour of the contract code.  Includes a tests that our helpers/instrumentation don't invalidate Java assumptions.
 */
public class HashCodeTest {
    @Before
    public void setup() throws Exception {
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
        Class<?> clazz = commonLoadTestClass();
        Assert.assertNotNull(clazz);
    }

    @Test
    public void testCommonHash() throws Exception {
        Class<?> clazz = commonLoadTestClass();
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
        Class<?> clazz = commonLoadTestClass();
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
        Class<?> clazz = commonLoadTestClass();
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


    private Class<?> commonLoadTestClass() throws ClassNotFoundException {
        ClassLoader parentLoader = HashCodeTest.class.getClassLoader();
        String className = HashCodeTestTarget.class.getName();
        byte[] raw = TestClassLoader.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", raw)
                .asMutableForest();
        
        AvmImpl avm = new AvmImpl();
        Map<String, Integer> runtimeObjectSizes = avm.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = avm.computeObjectSizes(classHierarchy, runtimeObjectSizes);
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            return avm.transformClasses(Collections.singletonMap(className, inputBytes), classHierarchy, allObjectSizes).get(className);
        };
        TestClassLoader loader = new TestClassLoader(parentLoader, transformer);
        loader.addClassForRewrite(className, raw);
        Class<?> clazz = loader.loadClass(className);
        Assert.assertEquals(loader, clazz.getClassLoader());
        return clazz;
    }
}
