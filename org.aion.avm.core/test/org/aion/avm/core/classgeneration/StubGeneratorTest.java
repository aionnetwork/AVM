package org.aion.avm.core.classgeneration;

import java.lang.reflect.Constructor;
import java.util.Map;

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
        String superName = TestClass.class.getCanonicalName().replaceAll("\\.", "/");
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
        String superName = TestClass.class.getCanonicalName().replaceAll("\\.", "/");
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
     * TODO:  We probably want to use a slightly different StubGenerator routine for this since these shadows technically have 4 constructors.
     * TODO:  We need to add handling for the hand-coded classes which had their own method support (and potentially their superclasses).
     */
    @Test
    public void testGenerateExceptionShadows() throws Exception {
        final String dAppRuntimePath = "/dev/null";
        final String dAppModulesPath = "/dev/null";
        final DAppLoader avm = new DAppLoader(dAppRuntimePath, dAppModulesPath);
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and a good test.
        Class<?> aioobe = generateExceptionShadowsAndWrappers(avm, CommonGenerators.kShadowClassLibraryPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.IndexOutOfBoundsException", aioobe.getSuperclass().getCanonicalName());
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.RuntimeException", aioobe.getSuperclass().getSuperclass().getCanonicalName());
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.Exception", aioobe.getSuperclass().getSuperclass().getSuperclass().getCanonicalName());
        Assert.assertEquals(CommonGenerators.kShadowClassLibraryPrefix + "java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getCanonicalName());
        
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
        final String dAppRuntimePath = "/dev/null";
        final String dAppModulesPath = "/dev/null";
        final DAppLoader avm = new DAppLoader(dAppRuntimePath, dAppModulesPath);
        // We specifically want to look at the hierarchy of java.lang.ArrayIndexOutOfBoundsException, since it is deep and a good test.
        Class<?> aioobe = generateExceptionShadowsAndWrappers(avm, CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.ArrayIndexOutOfBoundsException");
        
        // The interesting thing about the wrappers is that they are actually real Throwables.
        Assert.assertNotNull(aioobe);
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.IndexOutOfBoundsException", aioobe.getSuperclass().getCanonicalName());
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.RuntimeException", aioobe.getSuperclass().getSuperclass().getCanonicalName());
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.Exception", aioobe.getSuperclass().getSuperclass().getSuperclass().getCanonicalName());
        Assert.assertEquals(CommonGenerators.kWrapperClassLibraryPrefix + "java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getCanonicalName());
        Assert.assertEquals("java.lang.Throwable", aioobe.getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getCanonicalName());
        
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

    private static Class<?> generateExceptionShadowsAndWrappers(DAppLoader avm, String testClassName) throws Exception {
        // Get the generated classes.
        Map<String, byte[]> allGenerated = CommonGenerators.generateExceptionShadowsAndWrappers();
        
        // NOTE:  Given that we can only inject individual classes, we need to add them in the right order.
        // See ExceptionWrappingTest for an example of how this can be fully loaded into the classloader in such a way that the
        // class relationships can be lazily constructed.
        // For now, for this test case, we will just load them in a hard-coded topological order (since we will probably use a
        // different loader, eventually).
        // (note that the CommonGenerators constant happens to be in a safe order so we will use that).
        Class<?> found = null;
        for (String name : CommonGenerators.kExceptionClassNames) {
            String shadowName = CommonGenerators.kShadowClassLibraryPrefix + name;
            byte[] shadowBytes = allGenerated.get(shadowName);
            Class<?> shadowClass = avm.injectAndLoadClass(shadowName, shadowBytes);
            Assert.assertTrue(shadowClass.getClassLoader() instanceof DAppClassLoader);
            if (testClassName.equals(shadowName)) {
                found = shadowClass;
            }
            
            String wrapperName = CommonGenerators.kWrapperClassLibraryPrefix + name;
            byte[] wrapperBytes = allGenerated.get(wrapperName);
            Class<?> wrapperClass = avm.injectAndLoadClass(wrapperName, wrapperBytes);
            Assert.assertTrue(wrapperClass.getClassLoader() instanceof DAppClassLoader);
            if (testClassName.equals(wrapperName)) {
                found = wrapperClass;
            }
        }
        return found;
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
