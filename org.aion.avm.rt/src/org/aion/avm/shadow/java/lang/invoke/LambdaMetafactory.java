package org.aion.avm.shadow.java.lang.invoke;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.LambdaConversionException;

import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InvokeDynamicChecks;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadowapi.avm.InternalFunction;
import org.aion.avm.shadowapi.avm.InternalRunnable;


public final class LambdaMetafactory extends org.aion.avm.shadow.java.lang.Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public static java.lang.invoke.CallSite avm_metafactory(java.lang.invoke.MethodHandles.Lookup owner,
                                                            java.lang.String invokedName,
                                                            java.lang.invoke.MethodType invokedType,
                                                            java.lang.invoke.MethodType samMethodType,
                                                            java.lang.invoke.MethodHandle implMethod,
                                                            java.lang.invoke.MethodType instantiatedMethodType)
            throws LambdaConversionException {
        InvokeDynamicChecks.checkOwner(owner);
        // We don't expect any uses of this to be able to exist without the "avm_" prefix.
        RuntimeAssertionError.assertTrue(invokedName.startsWith("avm_"));
        InvokeDynamicChecks.checkBootstrapMethodType(invokedType);
        InvokeDynamicChecks.checkMethodHandle(implMethod);
        
        // We directly interpret the Runnable and Function, but everything else is invalid and should have been rejected, earlier.
        Class<?> returnType = invokedType.returnType();
        java.lang.invoke.CallSite callSite = null;
        if (org.aion.avm.shadow.java.lang.Runnable.class == returnType) {
            // Create the instance of the Runnable.
            InternalRunnable runnable = InternalRunnable.createRunnable(implMethod);
            // Since this instance knows about the target, we just need to return a CallSite which knows how to return this instance as a Runnable.
            java.lang.invoke.MethodHandle target = null;
            try {
                target = java.lang.invoke.MethodHandles.lookup()
                        .findVirtual(InternalRunnable.class, "self", invokedType)
                        .bindTo(runnable);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // This would be a static error, internally.
                throw RuntimeAssertionError.unexpected(e);
            }
            callSite = new ConstantCallSite(target);
        } else if (org.aion.avm.shadow.java.util.function.Function.class == returnType) {
            // Create the instance of the Function.
            InternalFunction function = InternalFunction.createFunction(implMethod);
            // Since this instance knows about the target, we just need to return a CallSite which knows how to return this instance as a Function.
            java.lang.invoke.MethodHandle target = null;
            try {
                target = java.lang.invoke.MethodHandles.lookup()
                        .findVirtual(InternalFunction.class, "self", invokedType)
                        .bindTo(function);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // This would be a static error, internally.
                throw RuntimeAssertionError.unexpected(e);
            }
            callSite = new ConstantCallSite(target);
        } else {
            throw RuntimeAssertionError.unreachable("Invalid invokeType in LambdaMetaFactory (return type: " + returnType + ")");
        }
        
        return callSite;
    }

    // Cannot be instantiated.
    private LambdaMetafactory() {}
    // Note:  No instances can be created so no deserialization constructor required.
}
