package org.aion.avm.core.dappreading;

import org.junit.Assert;
import org.junit.Test;


public class JarBuilderTest {
    /**
     * Verifies that we can store a simple class, reload it, and identify the main class.
     */
    @Test
    public void testPreserveMainClass() throws Exception {
        byte[] bytes = JarBuilder.buildJarForMainAndClasses(JarBuilderTest.class);
        LoadedJar jar = LoadedJar.fromBytes(bytes);
        Assert.assertEquals(JarBuilderTest.class.getName(), jar.mainClassName);
    }
}