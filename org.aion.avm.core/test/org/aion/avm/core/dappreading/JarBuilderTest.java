package org.aion.avm.core.dappreading;

import org.junit.Assert;
import org.junit.Test;


public class JarBuilderTest {
    /**
     * Verifies that we can store a simple class, reload it, and identify the main class.
     */
    @Test
    public void testPreserveMainClass() throws Exception {
        byte[] bytes = UserlibJarBuilder.buildJarForMainAndClasses(JarBuilderTest.class);
        LoadedJar jar = LoadedJar.fromBytes(bytes);
        Assert.assertEquals(JarBuilderTest.class.getName(), jar.mainClassName);
    }

    /**
     * Proves that we will create the same JAR file even if our local time changes by a few seconds.
     */
    @Test
    public void testConsistencyOnCall() throws Exception {
        byte[] bytes = UserlibJarBuilder.buildJarForMainAndClasses(JarBuilderTest.class);
        Thread.sleep(2000);
        byte[] bytes2 = UserlibJarBuilder.buildJarForMainAndClasses(JarBuilderTest.class);
        Assert.assertArrayEquals(bytes, bytes2);
    }
}
