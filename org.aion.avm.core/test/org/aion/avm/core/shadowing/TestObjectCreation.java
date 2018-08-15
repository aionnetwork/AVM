package org.aion.avm.core.shadowing;


public class TestObjectCreation {
    public static int accessObject() {
        return new Object().hashCode();
    }

    public static Object createInstance() {
        return new Object();
    }

    public static boolean isEqual(Object one, Object two) {
        return one.equals(two);
    }
}
