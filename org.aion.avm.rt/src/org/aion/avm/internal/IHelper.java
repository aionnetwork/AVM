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

    public org.aion.avm.shadow.java.lang.String externalWrapAsString(java.lang.String input);

    public int externalGetNextHashCode();
}
