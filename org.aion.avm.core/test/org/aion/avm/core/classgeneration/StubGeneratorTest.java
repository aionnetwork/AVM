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
