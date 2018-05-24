package org.aion.avm.internal;


/**
 * Handles the cases of some kind of asynchronous JVM-originating exception which nobody should try to handle.
 */
public class JvmError extends FatalAvmError {
    private static final long serialVersionUID = 1L;

    public JvmError(VirtualMachineError error) {
        super(error);
    }
}
