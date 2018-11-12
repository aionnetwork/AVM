package org.aion.avm.internal;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;


public class CommonInstrumentation implements IInstrumentation {
    public StackWatcher stackWatcher;

    private long energyLeft;
    private ClassLoader lateLoader;
    private int nextHashCode;
    private boolean abortState;


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
    private IdentityHashMap<String, org.aion.avm.shadow.java.lang.String> internedStringWrappers;
    private IdentityHashMap<Class<?>, org.aion.avm.shadow.java.lang.Class<?>> internedClassWrappers;

    private Field persistenceTokenField;

    // Set forceExitState to non-null to re-throw at the entry to every block (forces the contract to exit).
    private AvmThrowable forceExitState;

    public CommonInstrumentation(ClassLoader contractLoader, long energyLeft, int nextHashCode) {
        RuntimeAssertionError.assertTrue(null != contractLoader);
        this.lateLoader = contractLoader;

        this.energyLeft = energyLeft;
        this.nextHashCode = nextHashCode;

        //TODO: inherit abortState from parent?
        this.abortState = false;
        
        // Reset our interning state.
        // Note that we want to fail on any attempt to use the interned string map which isn't the initial call (since <clinit> needs it but any
        // other attempt to use it is an error).
        if (1 == nextHashCode) {
            this.internedStringWrappers = new IdentityHashMap<String, org.aion.avm.shadow.java.lang.String>();
        }
        this.internedClassWrappers = new IdentityHashMap<Class<?>, org.aion.avm.shadow.java.lang.Class<?>>();

        // setting up a default stack watcher.
        this.stackWatcher = new StackWatcher();
        this.stackWatcher.setPolicy(StackWatcher.POLICY_SIZE);
        this.stackWatcher.setMaxStackDepth(512);
        this.stackWatcher.setMaxStackSize(16 * 1024);

        // We need to look up the persistenceTokenField so we can install the special token for classes.
        try {
            this.persistenceTokenField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("persistenceToken");
        } catch (NoSuchFieldException | SecurityException e) {
            // Clearly a fatal error.
            RuntimeAssertionError.unexpected(e);
        }
        this.persistenceTokenField.setAccessible(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> org.aion.avm.shadow.java.lang.Class<T> wrapAsClass(Class<T> input) {
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
                // We treat classes much like constants, so add the IPersistenceToken for classes so that the persistence model knows how to ignore this.
                try {
                    persistenceTokenField.set(wrapper, new ClassPersistenceToken(input.getName()));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // This is something statically wrong with the shadow JCL.
                    RuntimeAssertionError.unexpected(e);
                }
                
                internedClassWrappers.put(input, wrapper);
            }
        }
        return wrapper;
    }

    @Override
    public org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
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

    @Override
    public org.aion.avm.shadow.java.lang.Object unwrapThrowable(Throwable t) {
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

    @Override
    public Throwable wrapAsThrowable(org.aion.avm.shadow.java.lang.Object arg) {
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

    @Override
    public void chargeEnergy(long cost) throws OutOfEnergyException {
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

        // Check if we are in abort state.
        if (abortState){
            EarlyAbortException error = new EarlyAbortException();
            forceExitState = error;
            throw error;
        }
    }

    @Override
    public long energyLeft() {
        return energyLeft;
    }

    @Override
    public void setEnergy(long e) {
        energyLeft = e;
    }

    @Override
    public int getNextHashCode() {
        // NOTE:  In the case of a Class object, this value is swapped out, temporarily.
        return nextHashCode++;
    }

    @Override
    public void setAbortState() {
        abortState = true;
    }


    @Override
    public int getCurStackSize() {
        return stackWatcher.getCurStackSize();
    }

    @Override
    public int getCurStackDepth() {
        return stackWatcher.getCurStackDepth();
    }

    @Override
    public void enterMethod(int frameSize) {
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

    @Override
    public void exitMethod(int frameSize) {
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

    @Override
    public void enterCatchBlock(int depth, int size) {
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

    @Override
    public void forceNextHashCode(int nextHashCode) {
        this.nextHashCode = nextHashCode;
    }


    // Private helpers used internally.
    private org.aion.avm.shadow.java.lang.Throwable convertVmGeneratedException(Throwable t) throws Exception {
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
