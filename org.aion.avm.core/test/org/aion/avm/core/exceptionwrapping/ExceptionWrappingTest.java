package org.aion.avm.core.exceptionwrapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.ClassHierarchyForest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;


public class ExceptionWrappingTest {
    private final Function<byte[], byte[]> commonCostBuilder = (inputBytes) -> {
        ClassReader in = new ClassReader(inputBytes);
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        ClassHierarchyForest classHierarchy = null;
        Map<String, byte[]> generatedClasses = null;
        ExceptionWrapping wrapping = new ExceptionWrapping(out, TestHelpers.CLASS_NAME, classHierarchy, generatedClasses);
        in.accept(wrapping, ClassReader.SKIP_DEBUG);
        
        byte[] transformed = out.toByteArray();
        return transformed;
    };

    @Before
    public void setup() {
        TestHelpers.didUnwrap = false;
        TestHelpers.didWrap = false;
    }


    /**
     * Tests that a multi-catch, using only java/lang/* exception types, works correctly.
     */
    @Test
    public void testSimpleTryMultiCatchFinally() throws Exception {
        String className = TestExceptionResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestExceptionResource.class.getClassLoader(), className, this.commonCostBuilder);
        Class<?> clazz = loader.loadClass(className);
        
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method tryMultiCatchFinally = clazz.getMethod("tryMultiCatchFinally");
        
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
        String className = TestExceptionResource.class.getCanonicalName();
        TestClassLoader loader = new TestClassLoader(TestExceptionResource.class.getClassLoader(), className, this.commonCostBuilder);
        Class<?> clazz = loader.loadClass(className);
        
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Method manuallyThrowNull = clazz.getMethod("manuallyThrowNull");
        
        // Create an array and make sure it is correct.
        Assert.assertFalse(TestHelpers.didWrap);
        boolean didCatch = false;
        try {
            manuallyThrowNull.invoke(null);
        } catch (InvocationTargetException e) {
            didCatch = e.getCause() instanceof NullPointerException;
        }
        Assert.assertTrue(TestHelpers.didWrap);
        Assert.assertTrue(didCatch);
    }


    public static class TestHelpers{
        public static final String CLASS_NAME = ExceptionWrappingTest.class.getCanonicalName().replaceAll("\\.", "/") + "$TestHelpers";
        public static boolean didUnwrap = false;
        public static boolean didWrap = false;
        
        public static Object unwrapThrowable(Throwable t) {
            didUnwrap = true;
            return t;
        }
        
        public static Throwable wrapAsThrowable(Object arg) {
            didWrap = true;
            // In our test, we are assume that this actually _is_ a throwable.
            return (Throwable)arg;
        }
    }
}
