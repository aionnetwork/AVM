package org.aion.avm.testcore;

import org.aion.avm.core.AvmClassLoader;
import org.aion.avm.core.impl.AvmImpl;
import org.aion.avm.fakecontract.C1;
import org.aion.avm.fakecontract.JavaAccessor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Katerinenko
 */
public class AvmClassLoaderTest {
    @Test
    public void checkAvmClassLoaderIsUsedForAllContractClasses() {
        final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.fakecontract";
        final var contractModulesPath = "../out/production";
        final var mainClassName = C1.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        final var mainClass = avm.getMainContractClass();
        Assert.assertSame(mainClass.getName(), mainClassName);
        Assert.assertTrue(mainClass.getClassLoader().getParent() instanceof AvmClassLoader);
    }

    @Test
    public void checkContractHasAccessToJavaClasses() {
        final var avm = new AvmImpl();
        final var startModuleName = "org.aion.avm.fakecontract";
        final var contractModulesPath = "../out/production";
        final var mainClassName = JavaAccessor.class.getName();
        avm.computeContract(contractModulesPath, startModuleName, mainClassName);
        final var mainClass = avm.getMainContractClass();
        Assert.assertSame(mainClass.getName(), mainClassName);
        Assert.assertTrue(mainClass.getClassLoader().getParent() instanceof AvmClassLoader);
    }
}