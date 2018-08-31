package org.aion.avm.core.verification;


/**
 * Key class in the VerifierTest.
 * This class is loaded within the VerifierClassLoader we are creating, directly, and the test interacts with it.
 */
public class UserSubTarget extends UserTarget {
    static {
        CommonTarget.didUserSubLoad = true;
    }
}
