package org.aion.avm.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.aion.avm.shadowapi.avm.InternalRunnable;


public final class RunnableFactory {
    private final MethodHandles.Lookup lookup;
    private final MethodHandle target;

    public RunnableFactory(MethodHandles.Lookup lookup, MethodHandle target) {
        this.lookup = lookup;
        this.target = target;
    }

    public org.aion.avm.shadow.java.lang.Runnable instantiate() {
        return InternalRunnable.createRunnable(this.lookup, this.target);
    }
}
