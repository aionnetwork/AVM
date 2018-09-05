package org.aion.avm.internal;

import java.util.IdentityHashMap;


public class Helper implements IHelper {

    public static IBlockchainRuntime blockchainRuntime;
    public static StackWatcher stackWatcher;

    private static long energyLeft;
    private static ClassLoader lateLoader;
    private static int nextHashCode;


    /**
     * Note that we need to consider instance equality for strings and classes:
     * -String instance quality isn't normally important but some cases, such as constant identifiers, are sometimes expected to be instance-equal.
     *  In our implementation, we are only going to preserve this for the <clinit> methods of the contract classes and, other than that, actively
     *  avoid any observable instance equality beyond instance preservation in the object graph (no relying on the same Class instance giving the
     *  same String instance back on successive calls, for example).
     * -Class instance equality is generally more important since classes don't otherwise have a clear definition of "equality"
     * Therefore, we will only create a map for interning strings if we suspect that this is the first call (a 1 nextHashCode - we may make this
     * explicit, in the future) but we will always create the map for interning classes.
     * The persistence layer also knows that classes are encoded differently so it will correctly resolve instance through this interning map.
     */
    private static IdentityHashMap<String, org.aion.avm.shadow.java.lang.String> internedStringWrappers;
    private static IdentityHashMap<Class<?>, org.aion.avm.shadow.java.lang.Class<?>> internedClassWrappers;

    // Set forceExitState to non-null to re-throw at the entry to every block (forces the contract to exit).
    private static AvmThrowable forceExitState;

    // We also describe non-static variants of a few pieces of data for reentrance purposes (we can use this to snapshot/restore).
    // This is relevant since the Helper statics are called by the code but limits, etc, may be changed during reentrant invocation.
    private IBlockchainRuntime snapshot_blockchainRuntime;
    private StackWatcher snapshot_stackWatcher;
    private long snapshot_energyLeft;
    private ClassLoader snapshot_lateLoader;
    private AvmThrowable snapshot_forceExitState;


    private static void initializeStaticState(ClassLoader loader, long nrgLeft, int nextHashCode) {
        // If we set the lateLoader twice, there is a serious problem in our configuration.
        RuntimeAssertionError.assertTrue(null == lateLoader);
        RuntimeAssertionError.assertTrue(null != loader);
        lateLoader = loader;

        energyLeft = nrgLeft;
        Helper.nextHashCode = nextHashCode;
        
        // Reset our interning state.
        // Note that we want to fail on any attempt to use the interned string map which isn't the initial call (since <clinit> needs it but any
        // other attempt to use it is an error).
        if (1 == nextHashCode) {
            internedStringWrappers = new IdentityHashMap<String, org.aion.avm.shadow.java.lang.String>();
        }
        internedClassWrappers = new IdentityHashMap<Class<?>, org.aion.avm.shadow.java.lang.Class<?>>();

        // setting up a default stack watcher.
        stackWatcher = new StackWatcher();
        stackWatcher.setPolicy(StackWatcher.POLICY_SIZE);
        stackWatcher.setMaxStackDepth(512);
        stackWatcher.setMaxStackSize(16 * 1024);
    }

    public static void clearTestingState() {
        // Currently intended only for use in testing since we expect the real deployment to load this in the DApp class
        // loader and discard after the call completes.
        lateLoader = null;
        forceExitState = null;
        
        // We also want to clear the thread local pointer.
        IHelper.currentContractHelper.remove();
    }

    @SuppressWarnings("unchecked")
    public static <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
        org.aion.avm.shadow.java.lang.Class<T> wrapper = null;
        if (null != input) {
            wrapper = (org.aion.avm.shadow.java.lang.Class<T>) internedClassWrappers.get(input);
            if (null == wrapper) {
                /**
                 * NOTE:  We need to treat Class objects as though they are allocated "outside" of the contract space.  We could use a special IHelper
                 * instance for that case but that seems a little heavy-weight for what is currently only observable as a change in the hashcode.
                 * In the future, we may want to formalize this using a mechanism like that (potentially even the bootstrap IHelper) but, to keep this
                 * simple, we will just swap out the "nextHashCode" and restore it, after allocation.  Similarly, to avoid the Class being observed
                 * as being in any specific allocation order, we will formally apply a hashcode based on its name as its "identity hash".
                 */
                int normalHashCode = nextHashCode;
                nextHashCode = input.getName().hashCode();
                wrapper = new org.aion.avm.shadow.java.lang.Class<T>(input);
                // Restore the normal hashcode counter.
                nextHashCode = normalHashCode;
                internedClassWrappers.put(input, wrapper);
            }
        }
        return wrapper;
    }

    /**
     * Note:  This is called by instrumented <clinit> methods to intern String constants defined in the contract code.
     * It should not be called anywhere else.
     * 
     * @param input The original String constant.
     * @return The interned shadow String wrapper.
     */
    public static org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
        org.aion.avm.shadow.java.lang.String wrapper = null;
        if (null != input) {
            wrapper = internedStringWrappers.get(input);
            if (null == wrapper) {
                wrapper = new org.aion.avm.shadow.java.lang.String(input);
                internedStringWrappers.put(input, wrapper);
            }
        }
        return wrapper;
    }

    public static org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t) {
        org.aion.avm.shadow.java.lang.Object shadow = null;
        AvmThrowable exceptionToRethrow = null;
        try {
            // NOTE:  This is called for both the cases where the throwable is a VM-generated "java.lang" exception or one of our wrappers.
            // We need to wrap the java.lang instance in a shadow and unwrap the other case to return the shadow.
            String throwableName = t.getClass().getName();
            if (throwableName.startsWith("java.lang.")) {
                // Note that there are 2 cases of VM-generated exceptions:  the kind we wrap for the user and the kind we interpret as a fatal node error.
                if (t instanceof VirtualMachineError) {
                    // This is a fatal node error:
                    // -create our fatal exception
                    JvmError error = new JvmError((VirtualMachineError)t);
                    // -store it in forceExitState
                    forceExitState = error;
                    // -throw it
                    throw error;
                }
                // This is VM-generated - we will have to instantiate a shadow, directly.
                shadow = convertVmGeneratedException(t);
            } else if (t instanceof AvmThrowable) {
                // There are cases where an AvmException might appear here during, for example, a finally clause.  We just want to re-throw it
                // since these aren't catchable within the user code.
                exceptionToRethrow = (AvmThrowable)t;
            } else {
                // This is one of our wrappers.
                org.aion.avm.exceptionwrapper.java.lang.Throwable wrapper = (org.aion.avm.exceptionwrapper.java.lang.Throwable)t;
                shadow = (org.aion.avm.shadow.java.lang.Object)wrapper.unwrap();
            }
        } catch (Throwable err) {
            // Unrecoverable internal error.
            throw RuntimeAssertionError.unexpected(err);
        }
        if (null != exceptionToRethrow) {
            throw exceptionToRethrow;
        }
        return shadow;
    }

    public static Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg) {
        Throwable result = null;
        try {
            // In this case, we just want to look up the appropriate wrapper (using reflection) and instantiate a wrapper for this.
            String objectClass = arg.getClass().getName();
            // Note that there are currently 2 cases related to the argument:
            // 1) This is an object from our "java/lang" shadows.
            // 2) This is an object defined by the user, and mapped into our "user" package.
            // Determine which case it is to strip off that prefix and apply the common wrapper prefix to look up the class.
            RuntimeAssertionError.assertTrue(objectClass.startsWith(PackageConstants.kShadowDotPrefix)
                    || objectClass.startsWith(PackageConstants.kUserDotPrefix));
            
            // Note that, since we currently declare the "java.lang." inside the constant for JDK shadows, we need to avoid curring that off.
            int lengthToCut = objectClass.startsWith(PackageConstants.kShadowDotPrefix)
                    ? PackageConstants.kShadowDotPrefix.length()
                    : PackageConstants.kUserDotPrefix.length();
            String wrapperClassName = PackageConstants.kExceptionWrapperDotPrefix + objectClass.substring(lengthToCut);
            Class<?> wrapperClass = lateLoader.loadClass(wrapperClassName);
            result = (Throwable)wrapperClass.getConstructor(Object.class).newInstance(arg);
        } catch (Throwable err) {
            // Unrecoverable internal error.
            throw RuntimeAssertionError.unexpected(err);
        } 
        return result;
    }

    public static void chargeEnergy(long cost) throws OutOfEnergyException {

        // This is called at the beginning of a block so see if we are being asked to exit.
        if (null != forceExitState) {
            throw forceExitState;
        }
        
        // Bill for the block.
        energyLeft -= cost;
        if (energyLeft < 0) {
            // Note that this is a reason to force the exit so set this.
            OutOfEnergyException error = new OutOfEnergyException();
            forceExitState = error;
            throw error;
        }
    }

    public static long energyLeft() {
        return energyLeft;
    }

    // Note that setEnergy is just for internal test purpose.
    public static void setEnergy(long e) {energyLeft = e;}

    public static int getNextHashCode() {
        // NOTE:  In the case of a Class object, this value is swapped out, temporarily.
        return nextHashCode++;
    }

    public static int getCurStackSize(){
        return stackWatcher.getCurStackSize();
    }

    public static int getCurStackDepth(){
        return stackWatcher.getCurStackDepth();
    }

    public static void enterMethod(int frameSize) {
        // may be redundant with class metering
        if (null != forceExitState) {
            throw forceExitState;
        }

        try {
            stackWatcher.enterMethod(frameSize);
        } catch (OutOfStackException ex) {
            forceExitState = ex;
        }
    }

    public static void exitMethod(int frameSize) {
        // may be redundant with class metering
        if (null != forceExitState) {
            throw forceExitState;
        }

        try {
            stackWatcher.exitMethod(frameSize);
        } catch (OutOfStackException ex) {
            forceExitState = ex;
        }
    }

    public static void enterCatchBlock(int depth, int size) {
        // may be redundant with class metering
        if (null != forceExitState) {
            throw forceExitState;
        }

        try {
            stackWatcher.enterCatchBlock(depth, size);
        } catch (OutOfStackException ex) {
            forceExitState = ex;
        }
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
        // We don't use these within the instance state but it is a convenient initialization point since we have direct visibility into the
        // Helper class which exists within the contract (outside, this isn't accessible).
        Helper.initializeStaticState(contractLoader, energyLeft, nextHashCode);
        
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
        this.snapshot_blockchainRuntime = Helper.blockchainRuntime;
        this.snapshot_stackWatcher = Helper.stackWatcher;
        Helper.blockchainRuntime = null;
        Helper.stackWatcher = null;
        this.snapshot_energyLeft = Helper.energyLeft;
        this.snapshot_lateLoader = Helper.lateLoader;
        Helper.lateLoader = null;
        this.snapshot_forceExitState = Helper.forceExitState;
        Helper.forceExitState = null;
        return Helper.nextHashCode;
    }
    @Override
    public void applySnapshotAndNextHashCode(int nextHashCode) {
        Helper.blockchainRuntime = this.snapshot_blockchainRuntime;
        Helper.stackWatcher = this.snapshot_stackWatcher;
        Helper.energyLeft = this.snapshot_energyLeft;
        Helper.lateLoader = this.snapshot_lateLoader;
        Helper.forceExitState = this.snapshot_forceExitState;
        Helper.nextHashCode = nextHashCode;
    }
    @Override
    public void externalSetEnergy(long energy){
        Helper.setEnergy(energy);
    }
    @Override
    public void externalBootstrapOnly() {
        // This implementation is for per-contract invocation, meaning it is not acceptable for the bootstrap phase.
        RuntimeAssertionError.assertTrue(false);
    }


    // Private helpers used internally.
    private static org.aion.avm.shadow.java.lang.Throwable convertVmGeneratedException(Throwable t) throws Exception {
        // First step is to convert the message and cause into shadow objects, as well.
        String originalMessage = t.getMessage();
        org.aion.avm.shadow.java.lang.String message = (null != originalMessage)
                ? new org.aion.avm.shadow.java.lang.String(originalMessage)
                : null;
        // (note that converting the cause is recusrive on the causal chain)
        Throwable originalCause = t.getCause();
        org.aion.avm.shadow.java.lang.Throwable cause = (null != originalCause)
                ? convertVmGeneratedException(originalCause)
                : null;
        
        // Then, use reflection to find the appropriate wrapper.
        String throwableName = t.getClass().getName();
        Class<?> shadowClass = lateLoader.loadClass(PackageConstants.kShadowDotPrefix + throwableName);
        return (org.aion.avm.shadow.java.lang.Throwable)shadowClass.getConstructor(org.aion.avm.shadow.java.lang.String.class, org.aion.avm.shadow.java.lang.Throwable.class).newInstance(message, cause);
    }

}
