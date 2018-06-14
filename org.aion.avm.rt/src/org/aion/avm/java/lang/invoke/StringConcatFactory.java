package org.aion.avm.java.lang.invoke;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.StringConcatException;
import java.util.Arrays;

/**
 * @author Roman Katerinenko
 */
public final class StringConcatFactory {

    public static org.aion.avm.java.lang.String concat(String recipe, // for future use
                                                                   Object[] constants, // for future use
                                                       org.aion.avm.java.lang.String[] strings) {
        final var builder = new StringBuilder();
        Arrays.stream(strings)
                .map(org.aion.avm.java.lang.String::toString)
                .forEach(builder::append);
        return new org.aion.avm.java.lang.String(builder.toString());
    }

    /**
     * A bootstrap method for handling string concatination
     */
    public static java.lang.invoke.CallSite avm_makeConcatWithConstants(
            java.lang.invoke.MethodHandles.Lookup owner,
            String invokedName,
            MethodType invokedType,
            String recipe,
            Object... constants) throws StringConcatException, NoSuchMethodException, IllegalAccessException {
        final var concatMethodType = MethodType.methodType(
                org.aion.avm.java.lang.String.class, // NOTE! First arg is return value
                String.class,
                Object[].class,
                org.aion.avm.java.lang.String[].class);
        final MethodHandle concatMethodHandle = owner
                .findStatic(StringConcatFactory.class, "concat", concatMethodType)
                .bindTo(recipe)
                .bindTo(constants)
                .asVarargsCollector(org.aion.avm.java.lang.String[].class)
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
//                .findStatic(org.aion.avm.java.lang.invoke.StringConcatFactory.class, "concat", concatMethodType)
//                .bindTo(recipe)
//                .bindTo(constants)
//                .asVarargsCollector(org.aion.avm.core.testdoubles.indy.String[].class)
//                .asType(invokedType);
//        return new ConstantCallSite(concatMethodHandle);
//    }
}