package org.aion.avm.internal;


/**
 * This is a specific sub-tree of the {#link InternalError} hierarchy, specifically designed to describe
 * a node-level failure.  That is, these cases are so severe that we are expecting not even fail the
 * contract code, but drop it and bring the system down.
 * These cases are so severe that calling "System.exit()" open wanting to instantiate one would be a
 * reasonable implementation.
 */
public abstract class FatalAvmError extends AvmException {
    private static final long serialVersionUID = 1L;

    protected FatalAvmError() {
        super();
    }

    protected FatalAvmError(String message) {
        super(message);
    }

    protected FatalAvmError(String message, Throwable cause) {
        super(message, cause);
    }

    protected FatalAvmError(Throwable cause) {
        super(cause);
    }
}
