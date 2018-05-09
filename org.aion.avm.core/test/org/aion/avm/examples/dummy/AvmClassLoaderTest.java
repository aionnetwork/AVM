package org.aion.avm.examples.dummy;

import org.aion.avm.core.AvmClassLoader;
import org.aion.avm.core.impl.AvmImpl;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author Roman Katerinenko
 */
public class AvmClassLoaderTest {
    @Test
    public void checkAvmClassLoaderIsUsedForAllContractClasses() throws Exception {
        final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.examples";
        final var contractModulesPath = "../build/main";
        final String mainClassName = C1.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        Class<?> mainLoadedClass = avm.getMainContractClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader().getParent() instanceof AvmClassLoader);
        //
        Object c1Instance = mainLoadedClass.getDeclaredConstructor().newInstance();
        Method method = mainLoadedClass.getDeclaredMethod("getC2");
        Object c2Instance = method.invoke(c1Instance);
        Assert.assertTrue(c2Instance.getClass().getClassLoader().getParent() instanceof AvmClassLoader);
    }

    @Test
    public void checkContractHasAccessToJavaClasses() {
        final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.examples";
        final var contractModulesPath = "../build/main";
        final String mainClassName = JavaAccessor.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        Class mainLoadedClass = avm.getMainContractClass();
        Assert.assertSame(mainLoadedClass.getName(), mainClassName);
        Assert.assertTrue(mainLoadedClass.getClassLoader().getParent() instanceof AvmClassLoader);
    }
}