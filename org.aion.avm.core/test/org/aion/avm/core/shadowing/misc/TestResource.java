package org.aion.avm.core.shadowing.misc;


public class TestResource {
    public static Object returnObject() {
        return new Object();
    }

    public static String returnString() {
        return "hello";
    }

    public static Class<?> returnClass() {
        return String.class;
    }

    public static boolean cast(Class<?> clazz, Object instance) {
        boolean success = false;
        try {
            clazz.cast(instance);
            success = true;
        } catch (ClassCastException e) {
            // Some tests are looking for this.  We want to prove we throw it in the right cases but can also catch it here.
            success = false;
        }
        return success;
    }

    public static Class<?> getClass(Object instance) {
        return instance.getClass();
    }

    public static Class<?> getSuperclass(Class<?> clazz) {
        return clazz.getSuperclass();
    }

    public static int checkValueOf() {
        return String.valueOf(new TestResource()).length();
    }

    public static int checkStringAppend() {
        return (" " + new TestResource() + " ").length();
    }

    /**
     * Note that this just like checkStringAppend() but directly uses StringBuilder since the other typically uses invokedynamic.
     */
    public static int checkStringBuilderAppend() {
        StringBuilder builder = new StringBuilder();
        builder.append(" ");
        builder.append(new TestResource());
        builder.append(" ");
        return builder.toString().length();
    }

    /**
     * At one point, we didn't have correct null handling in all the StringBuilder.append methods.
     */
    public static int checkNullStringBuilderAppend() {
        StringBuilder builder = new StringBuilder();
        builder.append((Object)null);
        builder.append((String)null);
        builder.append((StringBuffer)null);
        try {
            builder.append((char[])null);
        } catch (NullPointerException e) {
            // This is actually expected, for this case.
        }
        try {
            builder.append((char[])null, 0, 1);
        } catch (NullPointerException e) {
            // This is actually expected, for this case.
        }
        builder.append((CharSequence)null);
        builder.append((CharSequence)null, 0, 1);
        return builder.toString().length();
    }

    @Override
    public String toString() {
        // Return an empty string (since the default clearly doesn't do that).
        return "";
    }
}
