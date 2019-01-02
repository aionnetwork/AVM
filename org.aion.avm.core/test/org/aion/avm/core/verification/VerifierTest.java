package org.aion.avm.core.verification;

import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class VerifierTest {
    @Before
    public void setup() {
        CommonTarget.didUserLoad = false;
        CommonTarget.didUserSubLoad = false;
    }

    @Test
    public void verifyClinitOnInstantiate() throws Exception {
        // Load the test bytecode.
        Map<String, byte[]> testCode = VerifierTest.loadClassBytes(UserTarget.class, UserSubTarget.class);
        VerifierClassLoader testLoader = new VerifierClassLoader(testCode);
        
        // Verify that the common base class did run, but nothing else.
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertFalse(CommonTarget.didUserLoad);
        Assert.assertFalse(CommonTarget.didUserSubLoad);
        
        // Now, verify that we still don't see the load if we load the sub, without init.
        Class<?> sub = Class.forName(UserSubTarget.class.getName(), false, testLoader);
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertFalse(CommonTarget.didUserLoad);
        Assert.assertFalse(CommonTarget.didUserSubLoad);
        
        // That load should have forced both the classes to load.
        Assert.assertEquals(0, testLoader.getNotYetLoadedCount());
        
        // Verify that reflecting on fields or methods doesn't cause the load.
        sub.getFields();
        sub.getMethods();
        sub.getConstructors();
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertFalse(CommonTarget.didUserLoad);
        Assert.assertFalse(CommonTarget.didUserSubLoad);
        
        // Show that creating a new instance definitely loads.
        sub.getConstructor().newInstance();
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertTrue(CommonTarget.didUserLoad);
        Assert.assertTrue(CommonTarget.didUserSubLoad);
    }

    @Test
    public void verifyClinitOnInitializeArg() throws Exception {
        // Load the test bytecode.
        Map<String, byte[]> testCode = VerifierTest.loadClassBytes(UserTarget.class, UserSubTarget.class);
        VerifierClassLoader testLoader = new VerifierClassLoader(testCode);
        
        // Verify that the common base class did run, but nothing else.
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertFalse(CommonTarget.didUserLoad);
        Assert.assertFalse(CommonTarget.didUserSubLoad);
        
        // Now, verify that we still don't see the load if we load the sub, without init.
        Class.forName(UserSubTarget.class.getName(), false, testLoader);
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertFalse(CommonTarget.didUserLoad);
        Assert.assertFalse(CommonTarget.didUserSubLoad);
        
        // That load should have forced both the classes to load.
        Assert.assertEquals(0, testLoader.getNotYetLoadedCount());
        
        // Finally, verify that we still DO see the load if we load the sub, WITH init.
        Class.forName(UserSubTarget.class.getName(), true, testLoader);
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertTrue(CommonTarget.didUserLoad);
        Assert.assertTrue(CommonTarget.didUserSubLoad);
    }

    @Test
    public void verifyClinitNotRunByVerifierHelper() throws Exception {
        // Load the test bytecode.
        Map<String, byte[]> testCode = VerifierTest.loadClassBytes(UserTarget.class, UserSubTarget.class);
        
        // Verify that the common base class did run, but nothing else.
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertFalse(CommonTarget.didUserLoad);
        Assert.assertFalse(CommonTarget.didUserSubLoad);
        
        // Now, verify the 2 user sub-classes and verify that this hasn't changed.
        Verifier.verifyUntrustedClasses(testCode);
        Assert.assertTrue(CommonTarget.didILoad);
        Assert.assertFalse(CommonTarget.didUserLoad);
        Assert.assertFalse(CommonTarget.didUserSubLoad);
    }


    private static Map<String, byte[]> loadClassBytes(Class<?> ...classes) {
        Map<String, byte[]> map = new HashMap<>();
        for (Class<?> clazz : classes) {
            String name = clazz.getName();
            byte[] bytes = Helpers.loadRequiredResourceAsBytes(Helpers.fulllyQualifiedNameToInternalName(name) + ".class");
            Assert.assertNotNull(bytes);
            map.put(name, bytes);
        }
        return map;
    }
}
