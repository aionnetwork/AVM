package org.aion.avm.core.shadowing;

public class TestResource2 {

    public Class<?> getStatic() {
        return Byte.TYPE;
    }

    public int localVariable() {
        String a = new String("abc");

        return a.length();
    }
}
