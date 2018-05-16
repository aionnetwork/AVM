package org.aion.avm.internal;

public class Helper {

    public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
        return new org.aion.avm.java.lang.Class<T>(input);
    }

    public static org.aion.avm.java.lang.String wrapAsString(String input) {
        return new org.aion.avm.java.lang.String(input);
    }
}
