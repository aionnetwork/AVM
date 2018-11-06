package org.aion.avm.core.rejection.errors;


/**
 * Proves that user code isn't allowed to subclass VirtualMachineError (or anything in that hierarchy).
 */
public class RejectSubclassError extends VirtualMachineError {
    private static final long serialVersionUID = 1L;

    public static byte[] main() {
        return new byte[] { 0x0 };
    }
}
