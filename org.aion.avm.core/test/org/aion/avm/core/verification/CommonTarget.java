package org.aion.avm.core.verification;


/**
 * Top-level class accessed by the VerifierTest.
 * This is the class loaded by the test class loader, checked to see if any subclasses are loaded (since they set these flags).
 */
public class CommonTarget {
    public static boolean didILoad;
    public static boolean didUserLoad;
    public static boolean didUserSubLoad;

    static {
        didILoad = true;
    }
}
