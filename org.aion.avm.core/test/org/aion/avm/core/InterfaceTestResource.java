package org.aion.avm.core;

public class InterfaceTestResource {
    interface InnerInterface {
        int a = 1;
        String b = "abc";
        Object c = new Object();
    }

    public static int f() {
        return InnerInterface.b.length() + OuterInteface.b.length();
    }
}

interface OuterInteface {
    int a = 1;
    String b = "abc";
    Object c = new Object();
}
