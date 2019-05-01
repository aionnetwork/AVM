package org.aion.avm.core.persistence;

import org.aion.avm.core.miscvisitors.SingleLoader;
import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Test;


/**
 * A demonstration of some of our assumptions of how <clinit> is invoked.
 * This is in this package since it is largely related to the assumptions made by LoadedDApp.
 */
public class ClinitAssumptionsTest {
    // The counter which will be touched by loaded classes (must be public since they are from different loaders).
    public static int COUNTER = 0;

    @Test
    public void verifyForcedLoad() throws Exception {
        // Find the class we want to verify, and its associated bytecode.
        String targetTestName = EmptyAccessClass.class.getName();
        byte[] targetTestBytes = Helpers.loadRequiredResourceAsBytes(targetTestName.replaceAll("\\.", "/") + ".class");
        
        // Verify we haven't touched the counter yet.
        Assert.assertEquals(0, COUNTER);
        SingleLoader loader = new SingleLoader(targetTestName, targetTestBytes);
        Assert.assertEquals(0, COUNTER);
        
        // Verify that this direct load call with "resolve" does NOT initialize the class.
        loader.loadClass(targetTestName, true);
        Assert.assertEquals(0, COUNTER);
        
        // Verify that this explicit load call with "initialize" DOES initialize the class.
        Class.forName(targetTestName, true, loader);
        Assert.assertEquals(1, COUNTER);
    }


    private static final class EmptyAccessClass {
        static {
            ClinitAssumptionsTest.COUNTER += 1;
        }
    }
}
