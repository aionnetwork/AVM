package org.aion.avm.core.rejection;


/**
 * Throw by RejectionVisitor when it detects a violation of one of its rules.
 * This is a RuntimeException since it is thrown from deep within the visitor machinery and we want to catch it at the top-level.
 */
public class RejectedClassException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public static void unsupportedClassVersion(int version) {
        throw new RejectedClassException("Unsupported class version: " + version);
    }

    public static void blacklistedOpcode(int opcode) {
        throw new RejectedClassException("Blacklisted Opcode detected: 0x" + Integer.toHexString(opcode));
    }

    public static void nonWhiteListedClass(String className) {
        throw new RejectedClassException("Class is not on white-list: " + className);
    }


    public RejectedClassException(String message) {
        super(message);
    }
}
