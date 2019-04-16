package org.aion.avm.core.types;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.dappreading.LoadedJar;
import org.junit.Assert;
import org.junit.Test;


public class RawDappModuleTest {
    @Test
    public void testDeployNoMain() throws Exception {
        // This is a valid jar.
        byte[] jarBytes = JarBuilder.buildJarForMainAndClasses(null, RawDappModuleTest.class);
        LoadedJar jar = LoadedJar.fromBytes(jarBytes);
        Assert.assertNotNull(jar);
        Assert.assertNull(jar.mainClassName);
        
        // But NOT a valid Dapp.
        RawDappModule dapp = RawDappModule.readFromJar(jarBytes, false);
        Assert.assertNull(dapp);
    }

    @Test
    public void testDeployNoClasses() throws Exception {
        // This is a valid jar.
        byte[] jarBytes = JarBuilder.buildJarForMainAndClasses(null);
        LoadedJar jar = LoadedJar.fromBytes(jarBytes);
        Assert.assertNotNull(jar);
        Assert.assertTrue(jar.classBytesByQualifiedNames.isEmpty());
        
        // But NOT a valid Dapp.
        RawDappModule dapp = RawDappModule.readFromJar(jarBytes, false);
        Assert.assertNull(dapp);
    }
}
