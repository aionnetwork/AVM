package org.aion.avm.core.classgeneration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.dappreading.ClassLoadingResult;
import org.aion.avm.core.dappreading.DAppClassLoader;
import org.aion.avm.core.dappreading.DAppLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.rt.Address;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class StubGeneratorTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() throws Exception {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    @Before
    public void setup()throws ClassNotFoundException{
        Map<String, byte[]> classes = Helpers.mapIncludingHelperBytecode(Collections.emptyMap());
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        Helpers.instantiateHelper(loader, new SimpleRuntime(new byte[Address.LENGTH], new byte[Address.LENGTH], 0));
    }

    @Test
    public void testBasics() throws Exception {
        String slashName = "my/test/ClassName";
        String dotName = slashName.replaceAll("/", ".");
        String superName = TestClass.class.getName().replaceAll("\\.", "/");
        byte[] bytecode = StubGenerator.generateWrapperClass(slashName, superName);
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
        String superName = TestClass.class.getName().replaceAll("\\.", "/");
        byte[] bytecode = StubGenerator.generateWrapperClass(slashName, superName);
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
        String superName = TestClass.class.getName().replaceAll("\\.", "/");
        byte[] bytecode = StubGenerator.generateWrapperClass(slashName, superName);
        Class<?> superclass = avm.injectAndLoadClass(dotName, bytecode);
        Assert.assertTrue(superclass.getClassLoader() instanceof DAppClassLoader);
        
        // Create the subclass.
        String subSlashName = "my/test/sub/SubClass";
        String subDotName = subSlashName.replaceAll("/", ".");
        byte[] subBytecode = StubGenerator.generateWrapperClass(subSlashName, slashName);
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
     */
    @Test
    public void testGenerateExceptionShadows() throws Exception {
        ClassLoader parent = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and a good test.
        AvmClassLoader loader = generateExceptionShadowsAndWrappers(parent);
        Class<?> aioobe = loader.loadClass(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.IndexOutOfBoundsException", aioobe.getSuperclass().getName());
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.RuntimeException", aioobe.getSuperclass().getSuperclass().getName());
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.Exception", aioobe.getSuperclass().getSuperclass().getSuperclass().getName());
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getName());
        
        // Create an instance and prove that we can interact with it.
        Constructor<?> con = aioobe.getConstructor(org.aion.avm.java.lang.String.class);
        org.aion.avm.java.lang.String contents = new org.aion.avm.java.lang.String("one");
        Object instance = con.newInstance(contents);
        org.aion.avm.java.lang.Throwable shadow = (org.aion.avm.java.lang.Throwable)instance;
        // Ask for the toString (our internal version) since we know what that should look like.
        Assert.assertEquals("org.aion.avm.java.lang.ArrayIndexOutOfBoundsException: one", shadow.toString());
    }

    /**
     * This is must like above except we are interested in the generated wrappers _of_ the generated shadows for the built-in types
     */
    @Test
    public void testGenerateExceptionWrappers() throws Exception {
        ClassLoader parent = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and a good test.
        AvmClassLoader loader = generateExceptionShadowsAndWrappers(parent);
        Class<?> aioobe = loader.loadClass(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        // The interesting thing about the wrappers is that they are actually real Throwables.
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.IndexOutOfBoundsException", aioobe.getSuperclass().getName());
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.RuntimeException", aioobe.getSuperclass().getSuperclass().getName());
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.Exception", aioobe.getSuperclass().getSuperclass().getSuperclass().getName());
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getName());
        Assert.assertEquals("java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getName());
        
        // Create an instance and prove that we can interact with it.
        Constructor<?> con = aioobe.getConstructor(Object.class);
        String contents = "one";
        Object instance = con.newInstance(contents);
        org.aion.avm.exceptionwrapper.java.lang.Throwable wrapper = (org.aion.avm.exceptionwrapper.java.lang.Throwable)instance;
        // We can just unwrap this one.
        Assert.assertEquals(wrapper.unwrap(), contents);
        // Also, make sure that it is safe to cast this to the actual Throwable.
        Throwable top = (Throwable)wrapper;
        Assert.assertNotNull(top.toString());
    }

    /**
     * We want to verify that a generated shadow with a hand-written super-class has the correct classloaders through its hierarchy.
     */
    @Test
    public void getGeneratedShadowWithHandWrittenSuper() throws Exception {
        ClassLoader handWritten = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and is partially hand-written.
        AvmClassLoader generated = generateExceptionShadowsAndWrappers(handWritten);
        Class<?> aioobe = generated.loadClass(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        // We want to make sure that each class loader is what we expect.
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(sharedClassLoader, aioobe.getClassLoader()); // java.lang.ArrayIndexOutOfBoundsException
        Assert.assertEquals(sharedClassLoader, aioobe.getSuperclass().getClassLoader()); // java.lang.IndexOutOfBoundsException
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getClassLoader()); // java.lang.RuntimeException
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getSuperclass().getClassLoader()); // java.lang.Exception
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getClassLoader()); // java.lang.Throwable
        Assert.assertEquals(handWritten, aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getClassLoader()); // java.lang.Object
    }

    /**
     * Make sure that the "legacy" exceptions work properly (generated, but generated differently than the other cases).
     */
    @Test
    public void testGenerateLegacyExceptionShadows() throws Exception {
        ClassLoader handWritten = StubGeneratorTest.class.getClassLoader();
        // We specifically want to look at the hierarchy of java.lang.ClassNotFoundException, since it is deep and the legacy style.
        AvmClassLoader generated = generateExceptionShadowsAndWrappers(handWritten);
        Class<?> notFound = generated.loadClass(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.ClassNotFoundException");
        
        Assert.assertNotNull(notFound);
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.ClassNotFoundException", notFound.getName());
        Assert.assertEquals(sharedClassLoader, notFound.getClassLoader());
        
        Class<?> reflectiveOperationException = notFound.getSuperclass();
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.ReflectiveOperationException", reflectiveOperationException.getName());
        Assert.assertEquals(sharedClassLoader, reflectiveOperationException.getClassLoader());
        
        Class<?> exception = reflectiveOperationException.getSuperclass();
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.Exception", exception.getName());
        Assert.assertEquals(handWritten, exception.getClassLoader());
        
        Class<?> throwable = exception.getSuperclass();
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.Throwable", throwable.getName());
        Assert.assertEquals(handWritten, throwable.getClassLoader());
        
        Class<?> object = throwable.getSuperclass();
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.Object", object.getName());
        Assert.assertEquals(handWritten, object.getClassLoader());
        
        
        // Create an instance and prove that we can interact with it.
        Constructor<?> con = notFound.getConstructor(org.aion.avm.java.lang.String.class, org.aion.avm.java.lang.Throwable.class);
        org.aion.avm.java.lang.String contents = new org.aion.avm.java.lang.String("one");
        org.aion.avm.java.lang.Throwable cause = new org.aion.avm.java.lang.Throwable();
        
        Object instance = con.newInstance(contents, cause);
        org.aion.avm.java.lang.Throwable shadow = (org.aion.avm.java.lang.Throwable)instance;
        
        // Call our getException and make sure it is the cause.
        Method getException = notFound.getMethod("avm_getException");
        Object result = getException.invoke(shadow);
        Assert.assertTrue(result == cause);
    }


    private static AvmClassLoader generateExceptionShadowsAndWrappers(ClassLoader parent) throws Exception {
        // Get the generated classes.
        Map<String, byte[]> allGenerated = CommonGenerators.generateExceptionShadowsAndWrappers();
        // This test now falls back to the sharedClassLoader, which makes it kind of redundant, but it at least proves it is working as expected.
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, Collections.emptyMap());
        
        // NOTE:  Given that we can only inject individual classes, we need to add them in the right order.
        // See ExceptionWrappingTest for an example of how this can be fully loaded into the classloader in such a way that the
        // class relationships can be lazily constructed.
        // For now, for this test case, we will just load them in a hard-coded topological order (since we will probably use a
        // different loader, eventually).
        // (note that the CommonGenerators constant happens to be in a safe order so we will use that).
        for (String name : CommonGenerators.kExceptionClassNames) {
            String shadowName = CommonGenerators.kShadowClassLibraryPrefix + name;
            byte[] shadowBytes = allGenerated.get(shadowName);
            // Note that not all shadow exceptions are generated.
            Class<?> shadowClass = null;
            if (null != shadowBytes) {
                // Verify that these are being served by the shared loader (since these are the statically generated - shared loader).
                Assert.assertTrue(!CommonGenerators.kHandWrittenExceptionClassNames.contains(name));
                shadowClass = loader.loadClass(shadowName);
                Assert.assertEquals(sharedClassLoader, shadowClass.getClassLoader());
            } else {
                // This must be hand-written.
                Assert.assertTrue(CommonGenerators.kHandWrittenExceptionClassNames.contains(name));
                shadowClass = Class.forName(shadowName);
                Assert.assertEquals(parent, shadowClass.getClassLoader());
            }
            
            String wrapperName = CommonGenerators.kWrapperClassLibraryPrefix + name;
            byte[] wrapperBytes = allGenerated.get(wrapperName);
            Assert.assertNotNull(wrapperBytes);
            Class<?> wrapperClass = loader.loadClass(wrapperName);
            Assert.assertEquals(sharedClassLoader, wrapperClass.getClassLoader());
        }
        return loader;
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
