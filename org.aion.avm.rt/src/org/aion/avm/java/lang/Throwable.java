package org.aion.avm.java.lang;


/**
 * Our shadow implementation of java.lang.Throwable.
 * TODO:  Determine how to handle the calls that don't make sense in this environment or depend on types we aren't including.
 * 
 * NOTE:  Instances of this class never actually touch the underlying VM-generated exception.
 * If we want to carry that information around, we will need a new constructor, an addition to the generated stubs, and a sense of how to use it.
 * Avoiding carrying those instances around means that this implementation becomes very safely defined.
 * It does, however, mean that we can't expose stack traces since those are part of the VM-generated exceptions.
 */
public class Throwable extends Object {
    private final String message;
    private Throwable cause;

    public Throwable() {
        this(null, null);
    }

    public Throwable(String message) {
        this(message, null);
    }

    public Throwable(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    public Throwable(Throwable cause) {
        this(null, cause);
    }

    public String avm_getMessage() {
        return this.message;
    }

    public String avm_getLocalizedMessage() {
        return this.message;
    }

    public Throwable avm_getCause() {
        return this.cause;
    }

    public Throwable avm_initCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public String avm_toString() {
        return this.message;
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
        // We don't expose stack traces.
        return this;
    }

    // TODO:  Can't implement until we wrap StackTraceElement.
//    public StackTraceElement[] avm_getStackTrace() {
//    }

//    public void avm_setStackTrace(StackTraceElement[] stackTrace) {
//    }

    public void avm_addSuppressed(Throwable exception) {
        // TODO:  Does suppression make sense for this?
    }

    public Throwable[] avm_getSuppressed() {
        // TODO:  Does suppression make sense for this?
        return null;
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        return getClass().getCanonicalName() + ": " + this.message;
    }
}
