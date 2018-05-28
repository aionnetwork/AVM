package org.aion.avm.core.shadowing;

import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class ClassShadowingTest {
    @Test
    public void testReplaceJavaLang() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // We don't really need the runtime but we do need the intern map initialized.
        Helper.setBlockchainRuntime(new SimpleRuntime(null, null, 0));
        
        String className = "org.aion.avm.core.shadowing.TestResource";
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            ClassReader in = new ClassReader(inputBytes);
            ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            ClassShadowing cs = new ClassShadowing(out, Testing.CLASS_NAME);
            in.accept(cs, ClassReader.SKIP_DEBUG);

            byte[] transformed = out.toByteArray();
            return transformed;
        };
        Map<String, byte[]> classes = new HashMap<>(CommonGenerators.generateExceptionShadowsAndWrappers());
        classes.put(className, transformer.apply(raw));
        AvmClassLoader loader = new AvmClassLoader(classes);
        Class<?> clazz = loader.loadClass(className);
        Object obj = clazz.getConstructor().newInstance();

        Method method = clazz.getMethod("multi", int.class, int.class);
        Object ret = method.invoke(obj, 1, 2);
        Assert.assertEquals(0, ret);

        // Verify that we haven't created any wrapped instances, yet.
        Assert.assertEquals(0, Testing.countWrappedClasses);
        Assert.assertEquals(0, Testing.countWrappedClasses);
        
        // We can rely on our test-facing toString methods to look into what we got back.
        Object wrappedClass = clazz.getMethod("returnClass").invoke(obj);
        Assert.assertEquals("class org.aion.avm.java.lang.String", wrappedClass.toString());
        Object wrappedString = clazz.getMethod("returnString").invoke(obj);
        Assert.assertEquals("hello", wrappedString.toString());
        
        // Verify that we see wrapped instances.
        Assert.assertEquals(1, Testing.countWrappedClasses);
        Assert.assertEquals(1, Testing.countWrappedClasses);
        
        Helper.clearTestingState();
    }


    public static class Testing {
        public static String CLASS_NAME = Testing.class.getName().replaceAll("\\.", "/");
        public static int countWrappedClasses;
        public static int countWrappedStrings;
        
        public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
            countWrappedClasses += 1;
            return Helper.wrapAsClass(input);
        }
        public static org.aion.avm.java.lang.String wrapAsString(String input) {
            countWrappedStrings += 1;
            return Helper.wrapAsString(input);
        }
    }
}