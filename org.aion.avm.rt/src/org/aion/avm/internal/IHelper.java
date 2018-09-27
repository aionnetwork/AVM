package org.aion.avm.internal;


/**
 * The common interface implemented by the Helper class.
 * 
 * The need for this interface isn't obvious so here is a small explanation (see also issue-58):
 * -the Helper implementation is static, to be directly called from instrumentation
 * -an instance of the Helper class is loaded for each contract to avoid contaminating this state
 * -there is common code which needs access the Helper class of the contract calling it
 * -accessing the static interface is not an option since that would be a different class
 * -when a contract is loaded, its own Helper class is instantiated and that instance is saved here, in a ThreadLocal such that the common
 * code can call the Helper statics via instance shims which implement this interface
 * 
 * This means that this interface is shared by all the contracts, even though they all have their own copy of the Helper class.
 * We still want the Helper state to be static, since the callouts from the contract instrumentation is the critical path.
 */
public interface IHelper {
    // When a new Helper class is loaded and instantiated, it installs itself here for the calling thread.
    public static final ThreadLocal<IHelper> currentContractHelper = new ThreadLocal<>();

    public void externalChargeEnergy(long cost);

    public void externalSetEnergy(long energy);

    public long externalGetEnergyRemaining();

    public org.aion.avm.shadow.java.lang.Class<?> externalWrapAsClass(Class<?> input);

    public int externalGetNextHashCode();

    public void externalSetAbortState();

    /**
     * Instructs the receiver to capture a snapshot of the static Helper state it can access and store that in its own instance state for later
     * application.
     * This is done when we want to reuse the code (including the Helper class) within different calls (and each call has its own IHelper instance)
     * but we want to come back to this one, later on (this is used for reentrant cross-DApp call support).
     * 
     * @return The current "nextHashCode" value (since this is only part of state which actually might need to be persisted).
     */
    public int captureSnapshotAndNextHashCode();

    /**
     * Instructs the receiver to apply its previously-captured snapshot of the static Helper state back to the Helper.
     * This is done when a cross-DApp call returns from its target and wants to reenter the caller (meaning its static state must be restored).
     * 
     * @param nextHashCode The value to set as the "nextHashCode" (since this is related to the state of the DApp, not the call).
     */
    public void applySnapshotAndNextHashCode(int nextHashCode);

    /**
     * Optionally called by bootstrapping operations to validate that the IHelper instance they are using is appropriate for bootstrap operations.
     * Typically, an implementation which was for contract execution, only, would throw a runtime exception when this is called.
     */
    public void externalBootstrapOnly();
}
