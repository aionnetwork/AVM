package org.aion.avm.core.classgeneration;

import java.lang.reflect.Constructor;

import org.aion.avm.core.dappreading.ClassLoadingResult;
import org.aion.avm.core.dappreading.DAppClassLoader;
import org.aion.avm.core.dappreading.DAppLoader;
import org.junit.Assert;
import org.junit.Test;


public class StubGeneratorTest {
    @Test
    public void testBasics() throws Exception {
        String slashName = "my/test/ClassName";
        String dotName = slashName.replaceAll("/", ".");
        String superName = TestClass.class.getCanonicalName().replaceAll("\\.", "/");
        byte[] bytecode = StubGenerator.generateClass(slashName, superName);
        Class<?> clazz = Loader.loadClassAlone(dotName, bytecode);
        Constructor<?> con = clazz.getConstructor(Object.class);
        String contents = "one";
        Object foo = con.newInstance(contents);
        Assert.assertEquals(dotName, foo.getClass().getName());
        TestClass bar = (TestClass)foo;
        Assert.assertEquals(contents, bar.getContents());
    }

    /**
     * Tests that we can inject a stubbed class into the DApp loader.
     */
    @Test
    public void testWithDapp() throws Exception {
        final String dAppRuntimePath = "../examples/lib";
        final String dAppModulesPath = "../examples/build";
        final String startModuleName = "com.example.twoclasses";
        final String mainClassName = "com.example.twoclasses.JavaAccessor";
        
        final var avm = new DAppLoader(dAppRuntimePath, dAppModulesPath);
        ClassLoadingResult result = avm.loadDAppIntoNewLayer(startModuleName, mainClassName);
        Assert.assertTrue(result.isLoaded());
        Class<?> mainLoadedClass = result.getLoadedClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader() instanceof DAppClassLoader);
        
        String slashName = "my/test/ClassName";
        String dotName = slashName.replaceAll("/", ".");
        String superName = TestClass.class.getCanonicalName().replaceAll("\\.", "/");
        byte[] bytecode = StubGenerator.generateClass(slashName, superName);
        Class<?> clazz = avm.injectAndLoadClass(dotName, bytecode);
        Assert.assertTrue(clazz.getClassLoader() instanceof DAppClassLoader);
        Constructor<?> con = clazz.getConstructor(Object.class);
        String contents = "one";
        Object foo = con.newInstance(contents);
        Assert.assertEquals(dotName, foo.getClass().getName());
        TestClass bar = (TestClass)foo;
        Assert.assertEquals(contents, bar.getContents());
    }

    /**
     * Tests that we can inject a stubbed class into the DApp loader and then subclass it within the same DApp.
     */
    @Test
    public void testSubclassInDapp() throws Exception {
        final String dAppRuntimePath = "/dev/null";
        final String dAppModulesPath = "/dev/null";
        final DAppLoader avm = new DAppLoader(dAppRuntimePath, dAppModulesPath);
        
        // Create the superclass.
        String slashName = "my/test/ClassName";
        String dotName = slashName.replaceAll("/", ".");
        String superName = TestClass.class.getCanonicalName().replaceAll("\\.", "/");
        byte[] bytecode = StubGenerator.generateClass(slashName, superName);
        Class<?> superclass = avm.injectAndLoadClass(dotName, bytecode);
        Assert.assertTrue(superclass.getClassLoader() instanceof DAppClassLoader);
        
        // Create the subclass.
        String subSlashName = "my/test/sub/SubClass";
        String subDotName = subSlashName.replaceAll("/", ".");
        byte[] subBytecode = StubGenerator.generateClass(subSlashName, slashName);
        Class<?> subclass = avm.injectAndLoadClass(subDotName, subBytecode);
        Assert.assertTrue(subclass.getClassLoader() instanceof DAppClassLoader);
        Constructor<?> con = subclass.getConstructor(Object.class);
        String contents = "one";
        Object foo = con.newInstance(contents);
        Assert.assertEquals(subDotName, foo.getClass().getName());
        Assert.assertEquals(dotName, foo.getClass().getSuperclass().getName());
        TestClass bar = (TestClass)foo;
        Assert.assertEquals(contents, bar.getContents());
    }

    /**
     * Tests that our approach for generating all the exception shadows is correct.
     * The approach here is to find the original and shadow "java.lang.Throwable", walk the original, building a parallel tree in the shadow.
     * NOTE:  This test is really just a temporary landing zone for some generation code until its real location is ready, in the core.
     * TODO:  We probably want to use a slightly different StubGenerator routine for this since these shadows technically have 4 constructors.
     * TODO:  We need to add handling for the hand-coded classes which had their own method support (and potentially their superclasses).
     */
    @Test
    public void testGenerateExceptionShadows() throws Exception {
        final String dAppRuntimePath = "/dev/null";
        final String dAppModulesPath = "/dev/null";
        final DAppLoader avm = new DAppLoader(dAppRuntimePath, dAppModulesPath);
        
        // The location where we are writing this will have access to the shadow java.lang and the real one.
        String shadowClassLibraryPrefix = "org.aion.avm.";
        
        // There doesn't appear to be any way to enumerate these classes in the existing class loader (even though they are part of java.lang)
        // so we will list the names of all the classes we need and assemble them that way.
        // We should at least be able to use the original Throwable's classloader to look up the subclasses (again, since they are in java.lang).
        String[] exceptionNames = new String[] {
                "java.lang.Error",
                "java.lang.AssertionError",
                "java.lang.LinkageError",
                "java.lang.BootstrapMethodError",
                "java.lang.ClassCircularityError",
                "java.lang.ClassFormatError",
                "java.lang.UnsupportedClassVersionError",
                "java.lang.ExceptionInInitializerError",
                "java.lang.IncompatibleClassChangeError",
                "java.lang.AbstractMethodError",
                "java.lang.IllegalAccessError",
                "java.lang.InstantiationError",
                "java.lang.NoSuchFieldError",
                "java.lang.NoSuchMethodError",
                "java.lang.NoClassDefFoundError",
                "java.lang.UnsatisfiedLinkError",
                "java.lang.VerifyError",
                "java.lang.ThreadDeath",
                "java.lang.VirtualMachineError",
                "java.lang.InternalError",
                "java.lang.OutOfMemoryError",
                "java.lang.StackOverflowError",
                "java.lang.UnknownError",
                "java.lang.Exception",
                "java.lang.CloneNotSupportedException",
                "java.lang.InterruptedException",
                "java.lang.ReflectiveOperationException",
                "java.lang.ClassNotFoundException",
                "java.lang.IllegalAccessException",
                "java.lang.InstantiationException",
                "java.lang.NoSuchFieldException",
                "java.lang.NoSuchMethodException",
                "java.lang.RuntimeException",
                "java.lang.ArithmeticException",
                "java.lang.ArrayStoreException",
                "java.lang.ClassCastException",
                "java.lang.EnumConstantNotPresentException",
                "java.lang.IllegalArgumentException",
                "java.lang.IllegalThreadStateException",
                "java.lang.NumberFormatException",
                "java.lang.IllegalCallerException",
                "java.lang.IllegalMonitorStateException",
                "java.lang.IllegalStateException",
                "java.lang.IndexOutOfBoundsException",
                "java.lang.ArrayIndexOutOfBoundsException",
                "java.lang.StringIndexOutOfBoundsException",
                "java.lang.LayerInstantiationException",
                "java.lang.NegativeArraySizeException",
                "java.lang.NullPointerException",
                "java.lang.SecurityException",
                "java.lang.TypeNotPresentException",
                "java.lang.UnsupportedOperationException",
        };
        
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and a good test.
        Class<?> aioobe = null;
        
        for (String className : exceptionNames) {
            // We need to look this up to find the superclass.
            String superclassName = Class.forName(className).getSuperclass().getCanonicalName();
            
            // Generate this.
            String mappedName = shadowClassLibraryPrefix + className;
            String mappedSuperName = shadowClassLibraryPrefix + superclassName;
            
            String slashName = mappedName.replaceAll("\\.", "/");
            String superSlashName = mappedSuperName.replaceAll("\\.", "/");
            byte[] bytecode = StubGenerator.generateClass(slashName, superSlashName);
            Class<?> clazz = avm.injectAndLoadClass(mappedName, bytecode);
            Assert.assertTrue(clazz.getClassLoader() instanceof DAppClassLoader);
            
            if ("java.lang.ArrayIndexOutOfBoundsException".equals(className)) {
                aioobe = clazz;
            }
        }
        
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(shadowClassLibraryPrefix + "java.lang.IndexOutOfBoundsException", aioobe.getSuperclass().getCanonicalName());
        Assert.assertEquals(shadowClassLibraryPrefix + "java.lang.RuntimeException", aioobe.getSuperclass().getSuperclass().getCanonicalName());
        Assert.assertEquals(shadowClassLibraryPrefix + "java.lang.Exception", aioobe.getSuperclass().getSuperclass().getSuperclass().getCanonicalName());
        Assert.assertEquals(shadowClassLibraryPrefix + "java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getCanonicalName());
        
        // Create an instance and prove that we can interact with it.
        Constructor<?> con = aioobe.getConstructor(Object.class);
        String contents = "one";
        Object instance = con.newInstance(contents);
        org.aion.avm.java.lang.Throwable shadow = (org.aion.avm.java.lang.Throwable)instance;
        // We know that this constructor will create a shadow with a null underlying instance, meaning it will fail on calls (just there for initial testing).
        NullPointerException expected = null;
        try {
            shadow.toString();
        } catch (NullPointerException e) {
            expected = e;
        }
        Assert.assertNotNull(expected);
    }

    //Note that class names here are always in the dot style:  "java.lang.Object"
    private static class Loader {
        public static Class<?> loadClassAlone(String topName, byte[] bytecode) throws ClassNotFoundException {
            ClassLoader loader = new ClassLoader() {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    Class<?> result = null;
                    if (name.equals(topName)) {
                        result = defineClass(name, bytecode, 0, bytecode.length);
                    } else {
                        result = super.loadClass(name);
                    }
                    return result;
                }
            };
            return loader.loadClass(topName);
        }
    }
}
