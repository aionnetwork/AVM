package org.aion.avm.shadow.java.lang.invoke;

import java.lang.invoke.LambdaConversionException;

import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InvokeDynamicChecks;
import org.aion.avm.internal.RuntimeAssertionError;


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
            callSite = java.lang.invoke.LambdaMetafactory.metafactory(owner,
                invokedName,
                invokedType,
                samMethodType,
                implMethod,
                instantiatedMethodType);
        } else if (org.aion.avm.shadow.java.util.function.Function.class == returnType) {
            callSite = java.lang.invoke.LambdaMetafactory.metafactory(owner,
                    invokedName,
                    invokedType,
                    samMethodType,
                    implMethod,
                    instantiatedMethodType);
        } else {
            throw RuntimeAssertionError.unreachable("Invalid invokeType in LambdaMetaFactory (return type: " + returnType + ")");
        }
        
        return callSite;
    }

    // Cannot be instantiated.
    private LambdaMetafactory() {}
    // Note:  No instances can be created so no deserialization constructor required.
}
