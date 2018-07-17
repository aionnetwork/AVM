package org.aion.avm.shadow.java.lang.invoke;

import java.lang.invoke.LambdaConversionException;

import org.aion.avm.internal.IHelper;


/**
 * @author Roman Katerinenko
 */
public class LambdaMetafactory {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static java.lang.invoke.CallSite avm_metafactory(java.lang.invoke.MethodHandles.Lookup owner,
                                                            String invokedName,
                                                            java.lang.invoke.MethodType invokedType,
                                                            java.lang.invoke.MethodType samMethodType,
                                                            java.lang.invoke.MethodHandle implMethod,
                                                            java.lang.invoke.MethodType instantiatedMethodType)
            throws LambdaConversionException {
        return java.lang.invoke.LambdaMetafactory.metafactory(owner,
                invokedName,
                invokedType,
                samMethodType,
                implMethod,
                instantiatedMethodType);
    }
}