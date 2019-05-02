package org.aion.avm.core;

import i.IInstrumentation;
import i.RuntimeAssertionError;


/**
 * Manages the IInstrumentation interaction with the thread local in tests.
 * A common pattern is to need to suspend an IInstrumentation instance installed by a common component in order to run a more specific test/call.
 * This contains the state of that IInstrumentation so the tests aren't directly accessing the thread local.
 */
public class SuspendedInstrumentation {
    private final IInstrumentation suspendedInstrumentation;
    public SuspendedInstrumentation() {
        this.suspendedInstrumentation = IInstrumentation.attachedThreadInstrumentation.get();
        IInstrumentation.attachedThreadInstrumentation.remove();
    }
    public void resume() {
        RuntimeAssertionError.assertTrue(null == IInstrumentation.attachedThreadInstrumentation.get());
        IInstrumentation.attachedThreadInstrumentation.set(this.suspendedInstrumentation);
    }
}
