package org.aion.avm.shadow.java.lang.invoke;

import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InvokeDynamicChecks;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.Integer;
import org.aion.avm.shadow.java.lang.Short;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Long;
import org.aion.avm.shadow.java.lang.Double;
import org.aion.avm.shadow.java.lang.Float;
import org.aion.avm.shadow.java.lang.Character;
import org.aion.avm.shadow.java.lang.Byte;
import org.aion.avm.shadow.java.lang.Boolean;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


public final class StringConcatFactory extends org.aion.avm.shadow.java.lang.Object {
    private static final char RECIPE_DYNAMIC_ARGUMENT_FLAG = '\u0001';
    private static final char RECIPE_STATIC_ARGUMENT_FLAG = '\u0002';

    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public static String avm_concat(java.lang.String recipe, Object[] staticArgs, Object[] dynamicArgs) {
        // Note that we want to use a shadow StringBuilder since it correctly calls avm_toString() as opposed to toString().
        // (note that this will allocate a new object, at the level of the DApp, but only in the same way the non-invokedynamic approach would).
        final org.aion.avm.shadow.java.lang.StringBuilder builder = new org.aion.avm.shadow.java.lang.StringBuilder();
        int staticArgsIdx = 0;
        int dynamicArgsIdx = 0;
        for (int idx = 0; idx < recipe.length(); idx++) {
            char ch = recipe.charAt(idx);
            if (ch == RECIPE_DYNAMIC_ARGUMENT_FLAG) {
                org.aion.avm.shadow.java.lang.Object arg = mapBoxedType(dynamicArgs[dynamicArgsIdx++]);
                builder.avm_append(arg);
            } else if (ch == RECIPE_STATIC_ARGUMENT_FLAG) {
                org.aion.avm.shadow.java.lang.Object arg = mapBoxedType(staticArgs[staticArgsIdx++]);
                builder.avm_append(arg);
            } else {
                builder.avm_append(ch);
            }
        }
        return new String(builder.avm_toString());
    }

    private static org.aion.avm.shadow.java.lang.Object mapBoxedType(Object obj){
        org.aion.avm.shadow.java.lang.Object ret = null;
        if (null == obj){
            ret = null;
        }else if (obj instanceof org.aion.avm.shadow.java.lang.Object){
            ret = (org.aion.avm.shadow.java.lang.Object)obj;
        }else {
            java.lang.String name = obj.getClass().getSimpleName();
            switch (name) {
                case "Short":
                    ret = Short.avm_valueOf(((java.lang.Short)obj));
                    break;
                case "Integer":
                    ret = Integer.avm_valueOf(((java.lang.Integer)obj));
                    break;
                case "Long":
                    ret = Long.avm_valueOf(((java.lang.Long)obj));
                    break;
                case "Float":
                    ret = Float.avm_valueOf(((java.lang.Float)obj));
                    break;
                case "Double":
                    ret = Double.avm_valueOf(((java.lang.Double)obj));
                    break;
                case "Boolean":
                    ret = Boolean.avm_valueOf(((java.lang.Boolean)obj));
                    break;
                case "Byte":
                    ret = Byte.avm_valueOf(((java.lang.Byte)obj));
                    break;
                case "Character":
                    ret = Character.avm_valueOf(((java.lang.Character)obj));
                    break;
                default:
                    RuntimeAssertionError.unreachable("String concat receives unexpected type " + name);
            }
        }
        return ret;
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
        InvokeDynamicChecks.checkOwner(owner);
        // Note that we currently only use the avm_makeConcatWithConstants invoked name.
        RuntimeAssertionError.assertTrue("avm_makeConcatWithConstants".equals(invokedName));
        
        final MethodType concatMethodType = MethodType.methodType(
                String.class, // NOTE! First arg is return value
                java.lang.String.class,
                Object[].class,
                Object[].class);
        final MethodHandle concatMethodHandle = owner
                .findStatic(StringConcatFactory.class, "avm_concat", concatMethodType)
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
