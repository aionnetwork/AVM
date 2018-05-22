package org.aion.avm.java.lang;


/**
 * Our shadow implementation of java.lang.Throwable.
 * TODO:  Determine how to handle the calls that don't make sense in this environment or depend on types we aren't including.
 * See also the "WARNING", below, regarding re-wrapping underlying objects.
 */
public class Throwable extends Object {
    private final java.lang.Throwable underlying;

    protected Throwable(java.lang.Object temporary) {
        // This is just here as a temporary way to satisfy the creation of these exceptions until we update the StubGenerator to know about the real constructors.
        // This just allows for some easier testing.
        if (temporary instanceof java.lang.Throwable) {
            this.underlying = (java.lang.Throwable)temporary;
        } else {
            this.underlying = null;
        }
    }

    public Throwable(java.lang.Throwable underlying) {
        this.underlying = underlying;
    }

    public String avm_getMessage() {
        return new String(this.underlying.getMessage());
    }

    public String avm_getLocalizedMessage() {
        return new String(this.underlying.getLocalizedMessage());
    }

    public Throwable avm_getCause() {
        // WARNING:  We are going to recreate this on every call.  If the user code is expecting this to always be the same instance, we might have to
        // itern these, somewhere (as even caching it still means we might not know if someone else already created a wrapper).
        return wrapInner(this.underlying.getCause());
    }

    public Throwable avm_initCause(Throwable cause) {
        this.underlying.initCause(cause.underlying);
        return this;
    }

    public String avm_toString() {
        return new String(this.underlying.toString());
    }

    // TODO:  Determine if we should throw/fail when something calls a method which doesn't make sense in this environment.
    // Otherwise, these cases are commented-out since some of them would require types which can't be instantiated and this
    // will cause these to fail in a not silent way.
//    public void avm_printStackTrace() {
//    }

//    public void avm_printStackTrace(PrintStream s) {
//    }

//    private void avm_printStackTrace(PrintStreamOrWriter s) {
//    }

//    private void avm_printEnclosedStackTrace(PrintStreamOrWriter s,
//                                         StackTraceElement[] enclosingTrace,
//                                         String caption,
//                                         String prefix,
//                                         Set<Throwable> dejaVu) {
//    }

//    public void avm_printStackTrace(PrintWriter s) {
//    }

    public Throwable avm_fillInStackTrace() {
        this.underlying.fillInStackTrace();
        return this;
    }

    // TODO:  Can't implement until we wrap StackTraceElement.
//    public StackTraceElement[] avm_getStackTrace() {
//    }

//    public void avm_setStackTrace(StackTraceElement[] stackTrace) {
//    }

    public void avm_addSuppressed(Throwable exception) {
        this.underlying.addSuppressed(exception.underlying);
    }

    public Throwable[] avm_getSuppressed() {
        // WARNING:  We are going to recreate this on every call.  If the user code is expecting this to always be the same instance, we might have to
        // itern these, somewhere (as even caching it still means we might not know if someone else already created a wrapper).
        java.lang.Throwable[] real = this.underlying.getSuppressed();
        Throwable[] wrapped = new Throwable[real.length];
        for (int i = 0; i < real.length; ++i) {
            wrapped[i] = wrapInner(real[i]);
        }
        return wrapped;
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }

    // WARNING:  We are going to recreate this on every call.  If the user code is expecting this to always be the same instance, we might have to
    // itern these, somewhere (as even caching it still means we might not know if someone else already created a wrapper).
    private static Throwable wrapInner(java.lang.Throwable underlying) {
        return new Throwable(underlying);
    }
}
