package org.aion.avm.core.shadowing;

public class TestResource {

    public int multi(int a, int b) {
        return Math.multiplyExact(a + 1, b + 1);
    }

    public String[] newarray(int sz) {
        return new String[sz];
    }

    public String returnString() {
        return "hello";
    }

    public Class<?> returnClass() {
        return String.class;
    }

    public String callToString() {
        return new TestResource().toString();
    }
}
