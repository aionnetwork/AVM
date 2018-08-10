package org.aion.avm.core;

import org.aion.avm.internal.IHelper;


/**
 * Manages the IHelper interaction with the thread local in tests.
 * A common pattern is to need to suspend an IHelper instance installed by a common component in order to run a more specific test/call.
 * This contains the state of that IHelper so the tests aren't directly accessing the thread local.
 */
public class SuspendedHelper {
    private final IHelper suspended;
    public SuspendedHelper() {
        this.suspended = IHelper.currentContractHelper.get();
        IHelper.currentContractHelper.remove();
    }
    public void resume() {
        IHelper.currentContractHelper.set(this.suspended);
    }
}
