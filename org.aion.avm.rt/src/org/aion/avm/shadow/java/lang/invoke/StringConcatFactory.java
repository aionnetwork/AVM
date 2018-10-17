package org.aion.avm.shadow.java.lang.invoke;

import org.aion.avm.RuntimeMethodFeeSchedule;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


/**
 * @author Roman Katerinenko
 */
public final class StringConcatFactory extends org.aion.avm.shadow.java.lang.Object {
    private static final char RECIPE_DYNAMIC_ARGUMENT_FLAG = '\u0001';
    private static final char RECIPE_STATIC_ARGUMENT_FLAG = '\u0002';

    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static String concat(java.lang.String recipe, Object[] staticArgs, Object[] dynamicArgs) {
        // Note that we want to use a shadow StringBuilder since it correctly calls avm_toString() as opposed to toString().
        // (note that this will allocate a new object, at the level of the DApp, but only in the same way the non-invokedynamic approach would).
        final org.aion.avm.shadow.java.lang.StringBuilder builder = new org.aion.avm.shadow.java.lang.StringBuilder();
        int staticArgsIdx = 0;
        int dynamicArgsIdx = 0;
        for (int idx = 0; idx < recipe.length(); idx++) {
            char ch = recipe.charAt(idx);
            if (ch == RECIPE_DYNAMIC_ARGUMENT_FLAG) {
                org.aion.avm.shadow.java.lang.Object arg = (org.aion.avm.shadow.java.lang.Object)dynamicArgs[dynamicArgsIdx++];
                builder.avm_append(arg);
            } else if (ch == RECIPE_STATIC_ARGUMENT_FLAG) {
                org.aion.avm.shadow.java.lang.Object arg = (org.aion.avm.shadow.java.lang.Object)staticArgs[staticArgsIdx++];
                builder.avm_append(arg);
            } else {
                builder.avm_append(ch);
            }
        }
        return new String(builder.avm_toString());
    }

    /**
     * A bootstrap method for handling string concatenation
     *
     * @see java.lang.invoke.StringConcatFactory#makeConcatWithConstants(MethodHandles.Lookup, java.lang.String, MethodType, java.lang.String, Object...)
     */
    public static java.lang.invoke.CallSite avm_makeConcatWithConstants(
            java.lang.invoke.MethodHandles.Lookup owner,
            java.lang.String invokedName,
            MethodType concatType,
            java.lang.String recipe,
            Object... constants) throws NoSuchMethodException, IllegalAccessException {
        // Note that we currently only use the avm_makeConcatWithConstants invoked name.
        RuntimeAssertionError.assertTrue("avm_makeConcatWithConstants".equals(invokedName));
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringConcatFactory_avm_makeConcatWithConstants);
        final MethodType concatMethodType = MethodType.methodType(
                String.class, // NOTE! First arg is return value
                java.lang.String.class,
                Object[].class,
                Object[].class);
        final MethodHandle concatMethodHandle = owner
                .findStatic(StringConcatFactory.class, "concat", concatMethodType)
                .bindTo(recipe)
                .bindTo(constants)
                .asVarargsCollector(Object[].class)
                .asType(concatType);
        return new ConstantCallSite(concatMethodHandle);
    }

    // Cannot be instantiated.
    private StringConcatFactory() {
    }
    // Note:  No instances can be created so no deserialization constructor required.
}
