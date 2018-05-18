package org.aion.avm.core.dappreading;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author Roman Katerinenko
 */
public class DAppClassLoaderTest {
    private static final String dAppRuntimePath = "../examples/lib";
    private static final String dAppModulesPath = "../examples/build";
    private static final String moduleName = "com.example.twoclasses";

    @Test
    public void checkAvmClassLoaderIsUsedForAllDAppClasses() throws Exception {
        final var avm = new DAppLoader(dAppRuntimePath, dAppModulesPath);
        final var mainClassName = "com.example.twoclasses.C1";
        ClassLoadingResult result = avm.loadDAppIntoNewLayer(moduleName, mainClassName);
        Assert.assertTrue(result.isLoaded());
        Class<?> mainLoadedClass = result.getLoadedClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader() instanceof DAppClassLoader);
        //
        Object c1Instance = mainLoadedClass.getDeclaredConstructor().newInstance();
        Method method = mainLoadedClass.getDeclaredMethod("getC2");
        Object c2Instance = method.invoke(c1Instance);
        Assert.assertTrue(c2Instance.getClass().getClassLoader() instanceof DAppClassLoader);
    }

    @Test
    public void checkDAppHasAccessToJavaClasses() {
        final var avm = new DAppLoader(dAppRuntimePath, dAppModulesPath);
        final var mainClassName = "com.example.twoclasses.JavaAccessor";
        ClassLoadingResult result = avm.loadDAppIntoNewLayer(moduleName, mainClassName);
        Assert.assertTrue(result.isLoaded());
        Class<?> mainLoadedClass = result.getLoadedClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader() instanceof DAppClassLoader);
    }
}
