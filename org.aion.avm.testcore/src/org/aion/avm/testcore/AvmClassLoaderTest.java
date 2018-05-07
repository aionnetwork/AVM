package org.aion.avm.testcore;

import org.aion.avm.core.AvmClassLoader;
import org.aion.avm.core.impl.AvmImpl;
import org.aion.avm.testclasses.C1;
import org.aion.avm.testclasses.JavaAccessor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Katerinenko
 */
public class AvmClassLoaderTest {
    @Test
    public void checkAvmClassLoaderIsUsedForAllContractClasses() throws Exception {
        final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.testclasses";
        final var contractModulesPath = "../out/production";
        final var mainClassName = C1.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        final var mainClass = avm.getMainContractClass();
        Assert.assertSame(mainClass.getName(), mainClassName);
        Assert.assertTrue(mainClass.getClassLoader().getParent() instanceof AvmClassLoader);
    }

    @Test
    public void checkContractHasAccessToJavaClasses() throws Exception {
        final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.testclasses";
        final var contractModulesPath = "../out/production";
        final var mainClassName = JavaAccessor.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        final var mainClass = avm.getMainContractClass();
        Assert.assertSame(mainClass.getName(), mainClassName);
        Assert.assertTrue(mainClass.getClassLoader().getParent() instanceof AvmClassLoader);
    }
}