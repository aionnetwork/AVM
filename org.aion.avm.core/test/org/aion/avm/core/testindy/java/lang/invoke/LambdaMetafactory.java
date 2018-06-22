package org.aion.avm.core.testindy.java.lang.invoke;

import java.lang.invoke.LambdaConversionException;

/**
 * @author Roman Katerinenko
 */
public class LambdaMetafactory {
    public static boolean avm_metafactoryWasCalled;

    public static java.lang.invoke.CallSite avm_metafactory(java.lang.invoke.MethodHandles.Lookup owner,
                                                            String invokedName,
                                                            java.lang.invoke.MethodType invokedType,
                                                            java.lang.invoke.MethodType samMethodType,
                                                            java.lang.invoke.MethodHandle implMethod,
                                                            java.lang.invoke.MethodType instantiatedMethodType)
            throws LambdaConversionException {
        avm_metafactoryWasCalled = true;
        return org.aion.avm.shadow.java.lang.invoke.LambdaMetafactory.avm_metafactory(owner,
                invokedName,
                invokedType,
                samMethodType,
                implMethod,
                instantiatedMethodType);
    }
}