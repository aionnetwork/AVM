package org.aion.avm.core.classloading;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author Roman Katerinenko
 */
public class AvmClassLoaderTest {
    private static final String startModuleName = "org.aion.avm.examples";
    private static final String contractModulesPath = "../build/main";

    @Test
    public void checkAvmClassLoaderIsUsedForAllContractClasses() throws Exception {
        final var avm = new AvmImpl();
        final var mainClassName = "org.aion.avm.testcontracts.C1";
        ClassLoadingResult result = avm.loadContract(contractModulesPath, startModuleName, mainClassName);
        Assert.assertTrue(result.isLoaded());
        Class<?> mainLoadedClass = avm.getMainContractClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader() instanceof AvmClassLoader);
        //
        Object c1Instance = mainLoadedClass.getDeclaredConstructor().newInstance();
        Method method = mainLoadedClass.getDeclaredMethod("getC2");
        Object c2Instance = method.invoke(c1Instance);
        Assert.assertTrue(c2Instance.getClass().getClassLoader() instanceof AvmClassLoader);
    }

    @Test
    public void checkContractHasAccessToJavaClasses() {
        final var avm = new AvmImpl();
        final var mainClassName = "org.aion.avm.testcontracts.JavaAccessor";
        ClassLoadingResult result = avm.loadContract(contractModulesPath, startModuleName, mainClassName);
        Assert.assertTrue(result.isLoaded());
        Class mainLoadedClass = avm.getMainContractClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader() instanceof AvmClassLoader);
    }
}