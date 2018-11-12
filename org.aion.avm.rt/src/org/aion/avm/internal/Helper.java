package org.aion.avm.internal;

import org.aion.avm.api.BlockchainRuntime;


public class Helper implements IHelper {
    public static final String RUNTIME_HELPER_NAME = "H";

    // TODO:  Rationalize the use of IInstrumentation and IHelper (they currently have overlapping responsibilities).
    private static IInstrumentation target;
    private IInstrumentation snapshot;


    // We also describe non-static variants of a few pieces of data for reentrance purposes (we can use this to snapshot/restore).
    // This is relevant since the Helper statics are called by the code but limits, etc, may be changed during reentrant invocation.
    private IBlockchainRuntime snapshot_blockchainRuntime;


    public static void clearTestingState() {
        // Currently intended only for use in testing since we expect the real deployment to load this in the DApp class
        // loader and discard after the call completes.
        target = null;
        
        // We also want to clear the thread local pointer.
        IHelper.currentContractHelper.remove();
    }

    public static <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
        return target.wrapAsClass(input);
    }

    /**
     * Note:  This is called by instrumented <clinit> methods to intern String constants defined in the contract code.
     * It should not be called anywhere else.
     * 
     * @param input The original String constant.
     * @return The interned shadow String wrapper.
     */
    public static org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
        return target.wrapAsString(input);
    }

    public static org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t) {
        return target.unwrapThrowable(t);
    }

    public static Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg) {
        return target.wrapAsThrowable(arg);
    }

    public static void chargeEnergy(long cost) throws OutOfEnergyException {
        target.chargeEnergy(cost);
    }

    public static long energyLeft() {
        return target.energyLeft();
    }

    public static int getNextHashCode() {
        return target.getNextHashCode();
    }

    public void externalSetAbortState() {
        target.setAbortState();
    }

    public static int getCurStackSize(){
        return target.getCurStackSize();
    }

    public static int getCurStackDepth(){
        return target.getCurStackDepth();
    }

    public static void enterMethod(int frameSize) {
        target.enterMethod(frameSize);
    }

    public static void exitMethod(int frameSize) {
        target.exitMethod(frameSize);
    }

    public static void enterCatchBlock(int depth, int size) {
        target.enterCatchBlock(depth, size);
    }

    /**
     * Creates the Helper instance which is used as a trampoline to make calls into the Helper statics inside the DApp classloader space.
     * Without this, direct access to Helper, from the core runtime, would access the wrong class (since there is one per DApp) or reflection
     * would need to be used to access this specific one.
     * By creating this instance which implements a common interface, the core runtime can access any Helper statics via the corresponding
     * methods in the IHelper interface.
     * Note that one of these needs to be created for every call into the DApp, not just one for the DApp, itself.  This is because the
     * energyLeft is a per-call concept.
     * NOTE:  This has the side-effect of setting IHelper.currentContractHelper for the current thread.  The caller is responsible for clearing
     * this when the call is done.
     * 
     * @param contractLoader The class loader for the DApp.
     * @param energyLeft The energy limit for this call.
     * @param nextHashCode The default hashCode to use for the next allocated object.
     */
    public Helper(ClassLoader contractLoader, long energyLeft, int nextHashCode) {
        target = new CommonInstrumentation(contractLoader, energyLeft, nextHashCode);
        
        // We want to install ourself as the contract helper for this thread so it can call into the Helper class, within the DApp, without
        // always needing to resort to reflection.
        // (we also want to prove that we aren't silently over-writing something)
        RuntimeAssertionError.assertTrue(null == IHelper.currentContractHelper.get());
        IHelper.currentContractHelper.set(this);
    }
    @Override
    public void externalChargeEnergy(long cost) {
        Helper.chargeEnergy(cost);
    }
    @Override
    public long externalGetEnergyRemaining() {
        return Helper.energyLeft();
    }
    @Override
    public org.aion.avm.shadow.java.lang.Class<?> externalWrapAsClass(Class<?> input) {
        return Helper.wrapAsClass(input);
    }
    @Override
    public int externalGetNextHashCode() {
        return Helper.getNextHashCode();
    }
    @Override
    public int captureSnapshotAndNextHashCode() {
        RuntimeAssertionError.assertTrue(null == this.snapshot);
        RuntimeAssertionError.assertTrue(null != target);
        this.snapshot_blockchainRuntime = BlockchainRuntime.blockchainRuntime;
        BlockchainRuntime.blockchainRuntime = null;
        this.snapshot = target;
        target = null;
        return this.snapshot.getNextHashCode();
    }
    @Override
    public void applySnapshotAndNextHashCode(int nextHashCode) {
        RuntimeAssertionError.assertTrue(null != this.snapshot);
        // (target should be null but we don't symmetrically clean it up in all cases, yet)
        target = this.snapshot;
        this.snapshot = null;
        BlockchainRuntime.blockchainRuntime = this.snapshot_blockchainRuntime;
        this.snapshot_blockchainRuntime = null;
        target.forceNextHashCode(nextHashCode);
    }
    @Override
    public void externalBootstrapOnly() {
        // This implementation is for per-contract invocation, meaning it is not acceptable for the bootstrap phase.
        RuntimeAssertionError.assertTrue(false);
    }
}
