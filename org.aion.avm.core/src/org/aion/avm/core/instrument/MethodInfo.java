package org.aion.avm.core.instrument;

public final class MethodInfo {
    public final int access;
    public final String prefixStrippedName;
    public final String descriptor;
    public final String signature;
    public final String[] exceptions;

    public MethodInfo(int access, String prefixStrippedName, String descriptor, String signature, String[] exceptions) {
        this.access = access;
        this.prefixStrippedName = prefixStrippedName;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    @Override
    public String toString() {
        return "MethodInfo { access = " + this.access
            + ", method name = " + this.prefixStrippedName
            + ", descriptor = " + this.descriptor
            + ", signature = " + this.signature
            + ", exceptions = " + exceptionsAsString() + " }";
    }

    private String exceptionsAsString() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (String exception : this.exceptions) {
            builder.append(exception);
            if (index < this.exceptions.length - 1) {
                builder.append(", ");
            }
            index++;
        }
        return builder.toString();
    }
}
