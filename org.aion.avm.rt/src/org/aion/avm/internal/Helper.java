package org.aion.avm.internal;

import org.aion.avm.rt.BlockchainRuntime;

import java.lang.reflect.Array;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Helper {

    private static ThreadLocal<BlockchainRuntime> blockchainRuntime = new ThreadLocal<>();
    private static ThreadLocal<AtomicLong> energyLeft = new ThreadLocal<>();
    private static ClassLoader lateLoader;
    private static int nextHashCode;

    /**
     * We can't return a different wrapper for the same string instance so we need to intern these mappings to give back the same wrapper, each time.
     * It would probably be a good idea build an implementation of this which has WeakReference keys so we don't keep around unreachable wrappers.
     */
    private static IdentityHashMap<String, org.aion.avm.java.lang.String> internedStringWrappers;

    // Set forceExitState to non-null to re-throw at the entry to every block (forces the contract to exit).
    private static AvmException forceExitState;

    public static void setBlockchainRuntime(BlockchainRuntime rt) {
        blockchainRuntime.set(rt);
        energyLeft.set(new AtomicLong(rt.getEnergyLimit()));
        StackWatcher.reset();
        nextHashCode = 1;
        
        // Reset our interning state.
        internedStringWrappers = new IdentityHashMap<String, org.aion.avm.java.lang.String>();
    }

    public static void setLateClassLoader(ClassLoader loader) {
        // If we set the lateLoader twice, there is a serious problem in our configuration.
        RuntimeAssertionError.assertTrue(null == lateLoader);
        RuntimeAssertionError.assertTrue(null != loader);
        lateLoader = loader;
    }

    public static void clearTestingState() {
        // Currently intended only for use in testing since we expect the real deployment to load this in the DApp class
        // loader and discard after the call completes.
        lateLoader = null;
        forceExitState = null;
    }

    public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
        return new org.aion.avm.java.lang.Class<T>(input);
    }

    public static org.aion.avm.java.lang.String wrapAsString(String input) {
        org.aion.avm.java.lang.String wrapper = internedStringWrappers.get(input);
        if (null == wrapper) {
            wrapper = new org.aion.avm.java.lang.String(input);
            internedStringWrappers.put(input, wrapper);
        }
        return wrapper;
    }

    public static org.aion.avm.java.lang.Object unwrapThrowable(Throwable t) {
        org.aion.avm.java.lang.Object shadow = null;
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
            } else {
                // This is one of our wrappers.
                org.aion.avm.exceptionwrapper.java.lang.Throwable wrapper = (org.aion.avm.exceptionwrapper.java.lang.Throwable)t;
                shadow = (org.aion.avm.java.lang.Object)wrapper.unwrap();
            }
        } catch (Throwable err) {
            // Unrecoverable internal error.
            RuntimeAssertionError.unexpected(err);
        }
        return shadow;
    }

    public static Throwable wrapAsThrowable(org.aion.avm.java.lang.Object arg) {
        Throwable result = null;
        try {
            // In this case, we just want to look up the appropriate wrapper (using reflection) and instantiate a wrapper for this.
            String objectClass = arg.getClass().getName();
            // We know that this MUST be one of our shadow objects.
            RuntimeAssertionError.assertTrue(objectClass.startsWith(PackageConstants.kTopLevelDotPrefix));
            String wrapperClassName = PackageConstants.kExceptionWrapperDotPrefix + objectClass.substring(PackageConstants.kTopLevelDotPrefix.length());
            Class<?> wrapperClass = lateLoader.loadClass(wrapperClassName);
            result = (Throwable)wrapperClass.getConstructor(Object.class).newInstance(arg);
        } catch (Throwable err) {
            // Unrecoverable internal error.
            RuntimeAssertionError.unexpected(err);
        } 
        return result;
    }

    public static void chargeEnergy(long cost) throws OutOfEnergyError {
        // This is called at the beginning of a block so see if we are being asked to exit.
        if (null != forceExitState) {
            throw forceExitState;
        }
        
        // Bill for the block.
        if (energyLeft.get().addAndGet(-cost) < 0) {
            // Note that this is a reason to force the exit so set this.
            OutOfEnergyError error = new OutOfEnergyError();
            forceExitState = error;
            throw error;
        }
    }

    public static long energyLeft() {
        return energyLeft.get().get();
    }

    public static Object multianewarray1(int d1, Class<?> cl) {
        return Array.newInstance(cl, d1);
    }

    public static Object multianewarray2(int d1, int d2, Class<?> cl) {
        return Array.newInstance(cl, d1, d2);
    }

    public static Object multianewarray3(int d1, int d2, int d3, Class<?> cl) {
        return Array.newInstance(cl, d1, d2, d3);
    }

    public static int getNextHashCode() {
        return nextHashCode++;
    }

    // Private helpers used internally.
    private static org.aion.avm.java.lang.Throwable convertVmGeneratedException(Throwable t) throws Exception {
        // First step is to convert the message and cause into shadow objects, as well.
        String originalMessage = t.getMessage();
        org.aion.avm.java.lang.String message = (null != originalMessage)
                ? wrapAsString(originalMessage)
                : null;
        // (note that converting the cause is recusrive on the causal chain)
        Throwable originalCause = t.getCause();
        org.aion.avm.java.lang.Throwable cause = (null != originalCause)
                ? convertVmGeneratedException(originalCause)
                : null;
        
        // Then, use reflection to find the appropriate wrapper.
        String throwableName = t.getClass().getName();
        Class<?> shadowClass = lateLoader.loadClass(PackageConstants.kTopLevelDotPrefix + throwableName);
        return (org.aion.avm.java.lang.Throwable)shadowClass.getConstructor(org.aion.avm.java.lang.String.class, org.aion.avm.java.lang.Throwable.class).newInstance(message, cause);
    }
}
