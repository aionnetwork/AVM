package org.aion.avm.core;

import org.aion.avm.core.impl.AvmImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Katerinenko
 */
public class AvmClassLoaderTest {
    @Test
    public void checkAvmClassLoaderIsUsedForAllContractClasses() {
        /* final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.dummy";
        final var contractModulesPath = "../out/production";
        final var mainClassName = C1.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        final var mainClass = avm.getMainContractClass();
        Assert.assertSame(mainClass.getName(), mainClassName);
        Assert.assertTrue(mainClass.getClassLoader().getParent() instanceof AvmClassLoader); */
    }

    @Test
    public void checkContractHasAccessToJavaClasses() {
        /* final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.dummy";
        final var contractModulesPath = "../out/production";
        final var mainClassName = JavaAccessor.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        final var mainClass = avm.getMainContractClass();
        Assert.assertSame(mainClass.getName(), mainClassName);
        Assert.assertTrue(mainClass.getClassLoader().getParent() instanceof AvmClassLoader); */
    }
}