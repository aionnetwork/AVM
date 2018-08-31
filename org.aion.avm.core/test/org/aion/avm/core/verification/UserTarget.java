package org.aion.avm.core.verification;


/**
 * Intermediate class in the VerifierTest.
 * This class is loaded within the VerifierClassLoader we are creating, but is not accessed directly (it is the superclass of that class).
 */
public class UserTarget extends CommonTarget {
    static {
        CommonTarget.didUserLoad = true;
    }
}
