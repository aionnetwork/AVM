package org.aion.avm.shadow.java.lang.invoke;

import org.aion.avm.internal.IHelper;
import org.aion.avm.shadow.java.lang.String;

import java.lang.invoke.*;
import java.util.Arrays;


/**
 * @author Roman Katerinenko
 */
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
     * A bootstrap method for handling string concatenation
     *
     * @see java.lang.invoke.StringConcatFactory#makeConcatWithConstants(MethodHandles.Lookup, java.lang.String, MethodType, java.lang.String, Object...)
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

    // Cannot be instantiated.
    private StringConcatFactory() {
    }
    // Note:  No instances can be created so no deserialization constructor required.
}