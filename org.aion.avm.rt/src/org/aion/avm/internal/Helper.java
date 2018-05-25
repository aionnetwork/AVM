package org.aion.avm.internal;

import org.aion.avm.rt.BlockchainRuntime;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLong;

public class Helper {

    private static ThreadLocal<BlockchainRuntime> blockchainRuntime = new ThreadLocal<>();
    private static ThreadLocal<AtomicLong> energyLeft = new ThreadLocal<>();
    private static ClassLoader lateLoader;

    public static void setBlockchainRuntime(BlockchainRuntime rt) {
        blockchainRuntime.set(rt);
        energyLeft.set(new AtomicLong(rt.getEnergyLimit()));
        StackWatcher.reset();
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
    }

    public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
        return new org.aion.avm.java.lang.Class<T>(input);
    }

    public static org.aion.avm.java.lang.String wrapAsString(String input) {
        return new org.aion.avm.java.lang.String(input);
    }

    public static org.aion.avm.java.lang.Object unwrapThrowable(Throwable t) {
        org.aion.avm.java.lang.Object shadow = null;
        try {
            // NOTE:  This is called for both the cases where the throwable is a VM-generated "java.lang" exception or one of our wrappers.
            // We need to wrap the java.lang instance in a shadow and unwrap the other case to return the shadow.
            String throwableName = t.getClass().getName();
            if (throwableName.startsWith("java.lang.")) {
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
        if (energyLeft.get().addAndGet(-cost) < 0) {
            throw new OutOfEnergyError();
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
