package org.aion.avm.core.shadowing;


public interface TestResourceInterface {
    public static String getStringForNull() {
        TestResourceInterface n = new TestResourceInterface() {};
        return middleString(n);
    }

    public static String middleString(TestResourceInterface target) {
        return actualString(target);
    }

    public static String actualString(Object target) {
        return target.toString();
    }
}
