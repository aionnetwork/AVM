package org.aion.avm.core.miscvisitors;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Method;


/**
 * This tests the ClinitStrippingVisitor, alone, not depending on anything else in our infrastructure.
 * This means that most names are literally preserved, for example.
 */
public class ClinitStrippingVisitorTest {
    private Class<?> originalClass;
    private Class<?> strippedClass;

    @Before
    public void setup() throws Exception {
        String targetTestName = ClinitStrippingVisitorTestTarget.class.getName();
        byte[] targetTestBytes = Helpers.loadRequiredResourceAsBytes(targetTestName.replaceAll("\\.", "/") + ".class");
        
        byte[] strippedBytes = new ClassToolchain.Builder(targetTestBytes, ClassReader.SKIP_DEBUG)
                        .addNextVisitor(new ClinitStrippingVisitor())
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();
        this.originalClass = SingleLoader.loadClass(targetTestName, targetTestBytes);
        this.strippedClass = SingleLoader.loadClass(targetTestName, strippedBytes);
    }

    /**
     * We should see the correct constant string.
     */
    @Test
    public void testNormalOperation() throws Exception {
        Method getConstant = this.originalClass.getMethod("getConstant");
        Object result = getConstant.invoke(null);
        Assert.assertEquals(ClinitStrippingVisitorTestTarget.ONE_CONSTANT, result);
    }

    /**
     * We should see a null..
     */
    @Test
    public void testStrippedOperation() throws Exception {
        Method getConstant = this.strippedClass.getMethod("getConstant");
        Object result = getConstant.invoke(null);
        Assert.assertNull(result);
    }

    public static class SingleLoader extends ClassLoader {
        public static Class<?> loadClass(String name, byte[] bytecode) throws ClassNotFoundException {
            SingleLoader loader = new SingleLoader(name, bytecode);
            Class<?> clazz = Class.forName(name, true, loader);
            Assert.assertNotNull(clazz);
            Assert.assertEquals(loader, clazz.getClassLoader());
            return clazz;
        }
        
        
        private final String name;
        private final byte[] bytecode;
        
        public SingleLoader(String name, byte[] bytecode) {
            this.name = name;
            this.bytecode = bytecode;
        }
        
        // NOTE:  We override loadClass, instead of findClass, since we want to force this class loader to get the first crack at loading.
        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> clazz = null;
            if (this.name.equals(name)) {
                clazz = this.defineClass(this.name, this.bytecode, 0, this.bytecode.length);
                if (resolve) {
                    this.resolveClass(clazz);
                }
            } else {
                clazz = super.loadClass(name, resolve);
            }
            return clazz;
        }
    }
}
