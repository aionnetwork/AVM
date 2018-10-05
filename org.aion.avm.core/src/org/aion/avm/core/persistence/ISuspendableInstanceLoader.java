package org.aion.avm.core.persistence;


/**
 * The interface an instance loading abstraction must implement in order to be notified of when it becomes "active"/"inactive" across
 * reentrant invocations.
 * Specifically, there is only ever one "active" instance loader:  the one on top of the reentrant stack (ie:  the one actively running).
 * Note that implementors are expected to start in an "active" state and they are not told that they become "inactive" when discarded.
 */
public interface ISuspendableInstanceLoader {
    /**
     * Tells the receiver it has become "active" as the instance loader on top of the stack.
     */
    void loaderDidBecomeActive();

    /**
     * Tells the receiver it has become "inactive" as a new instance loader has been pushed on top of it, on the stack.
     */
    void loaderDidBecomeInactive();
}
