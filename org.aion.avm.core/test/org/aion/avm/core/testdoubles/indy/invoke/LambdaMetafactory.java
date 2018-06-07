package org.aion.avm.core.testdoubles.indy.invoke;

import java.lang.invoke.LambdaConversionException;

/**
 * @author Roman Katerinenko
 */
public class LambdaMetafactory {
    public static boolean avm_metafactoryWasCalled;

    public static java.lang.invoke.CallSite avm_metafactory(java.lang.invoke.MethodHandles.Lookup caller,
                                                            java.lang.String invokedName,
                                                            java.lang.invoke.MethodType invokedType,
                                                            java.lang.invoke.MethodType samMethodType,
                                                            java.lang.invoke.MethodHandle implMethod,
                                                            java.lang.invoke.MethodType instantiatedMethodType)
            throws LambdaConversionException {
        avm_metafactoryWasCalled = true;
        return java.lang.invoke.LambdaMetafactory.metafactory(caller,
                invokedName,
                invokedType,
                samMethodType,
                implMethod,
                instantiatedMethodType);
    }
}