package org.aion.avm.shadow.java.lang.invoke;

import org.aion.avm.internal.IHelper;
import org.aion.avm.shadow.java.lang.String;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.StringConcatException;
import java.util.Arrays;


/**
 * @author Roman Katerinenko
 */
//TODO:  Determine if this class should be moved into an internal package (should the user be allowed to invoke it, directly?).
public final class StringConcatFactory extends org.aion.avm.shadow.java.lang.Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static String concat(java.lang.String recipe, // for future use
                                Object[] constants, // for future use
                                String[] strings) {
        final var builder = new StringBuilder();
        Arrays.stream(strings)
                .map(String::toString)
                .forEach(builder::append);
        return new String(builder.toString());
    }

    /**
     * A bootstrap method for handling string concatination
     */
    public static java.lang.invoke.CallSite avm_makeConcatWithConstants(
            java.lang.invoke.MethodHandles.Lookup owner,
            java.lang.String invokedName,
            MethodType invokedType,
            java.lang.String recipe,
            Object... constants) throws StringConcatException, NoSuchMethodException, IllegalAccessException {
        final var concatMethodType = MethodType.methodType(
                String.class, // NOTE! First arg is return value
                java.lang.String.class,
                Object[].class,
                String[].class);
        final MethodHandle concatMethodHandle = owner
                .findStatic(StringConcatFactory.class, "concat", concatMethodType)
                .bindTo(recipe)
                .bindTo(constants)
                .asVarargsCollector(String[].class)
                .asType(invokedType);
        return new ConstantCallSite(concatMethodHandle);
    }
//    public static org.aion.avm.core.testdoubles.indy.String concat(java.lang.String recipe, // for future use
//                                                                   java.lang.Object[] constants, // for future use
//                                                                   org.aion.avm.core.testdoubles.indy.String[] strings) {
//        final var builder = new StringBuilder();
//        Arrays.stream(strings)
//                .map(org.aion.avm.core.testdoubles.indy.String::getOriginal)
//                .forEach(builder::append);
//        return new org.aion.avm.core.testdoubles.indy.String(builder.toString());
//    }
//
//    /**
//     * A bootstrap method for handling string concatination
//     */
//    public static java.lang.invoke.CallSite avm_makeConcatWithConstants(
//            java.lang.invoke.MethodHandles.Lookup owner,
//            java.lang.String invokedName,
//            java.lang.invoke.MethodType invokedType,
//            java.lang.String recipe,
//            java.lang.Object... constants) throws StringConcatException, NoSuchMethodException, IllegalAccessException {
//        final var concatMethodType = MethodType.methodType(
//                org.aion.avm.core.testdoubles.indy.String.class, // NOTE! First arg is return value
//                String.class,
//                Object[].class,
//                org.aion.avm.core.testdoubles.indy.String[].class);
//        final MethodHandle concatMethodHandle = owner
//                .findStatic(StringConcatFactory.class, "concat", concatMethodType)
//                .bindTo(recipe)
//                .bindTo(constants)
//                .asVarargsCollector(org.aion.avm.core.testdoubles.indy.String[].class)
//                .asType(invokedType);
//        return new ConstantCallSite(concatMethodHandle);
//    }

    // Cannot be instantiated.
    private StringConcatFactory() {}
    // Note:  No instances can be created so no deserialization constructor required.
}
