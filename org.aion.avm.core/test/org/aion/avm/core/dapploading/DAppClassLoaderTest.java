package org.aion.avm.core.dapploading;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author Roman Katerinenko
 */
public class DAppClassLoaderTest {
    private static final String startModuleName = "org.aion.avm.examples";
    private static final String contractModulesPath = "../build/main";

    @Test
    public void checkAvmClassLoaderIsUsedForAllContractClasses() throws Exception {
        final var avm = new DAppLoader();
        final var mainClassName = "org.aion.avm.testcontracts.C1";
        ClassLoadingResult result = avm.loadDAppIntoNewLayer(contractModulesPath, startModuleName, mainClassName);
        Assert.assertTrue(result.isLoaded());
        Class<?> mainLoadedClass = avm.getDAppMainClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader() instanceof DAppClassLoader);
        //
        Object c1Instance = mainLoadedClass.getDeclaredConstructor().newInstance();
        Method method = mainLoadedClass.getDeclaredMethod("getC2");
        Object c2Instance = method.invoke(c1Instance);
        Assert.assertTrue(c2Instance.getClass().getClassLoader() instanceof DAppClassLoader);
    }

    @Test
    public void checkContractHasAccessToJavaClasses() {
        final var avm = new DAppLoader();
        final var mainClassName = "org.aion.avm.testcontracts.JavaAccessor";
        ClassLoadingResult result = avm.loadDAppIntoNewLayer(contractModulesPath, startModuleName, mainClassName);
        Assert.assertTrue(result.isLoaded());
        Class mainLoadedClass = avm.getDAppMainClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader() instanceof DAppClassLoader);
    }
}