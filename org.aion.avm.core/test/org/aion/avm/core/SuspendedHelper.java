package org.aion.avm.core;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Manages the IHelper interaction with the thread local in tests.
 * A common pattern is to need to suspend an IHelper instance installed by a common component in order to run a more specific test/call.
 * This contains the state of that IHelper so the tests aren't directly accessing the thread local.
 */
public class SuspendedHelper {
    private final IHelper suspendedHelper;
    // Note that we also hold on to the IInstrumentation (as we are transitioning to this).
    private final IInstrumentation suspendedInstrumentation;

    public SuspendedHelper() {
        this.suspendedHelper = IHelper.currentContractHelper.get();
        IHelper.currentContractHelper.remove();
        this.suspendedInstrumentation = IInstrumentation.attachedThreadInstrumentation.get();
        IInstrumentation.attachedThreadInstrumentation.remove();
    }
    public void resume() {
        RuntimeAssertionError.assertTrue(null == IInstrumentation.attachedThreadInstrumentation.get());
        IInstrumentation.attachedThreadInstrumentation.set(this.suspendedInstrumentation);
        RuntimeAssertionError.assertTrue(null == IHelper.currentContractHelper.get());
        IHelper.currentContractHelper.set(this.suspendedHelper);
    }
}
