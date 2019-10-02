package org.aion.avm.core.miscvisitors.interfaceVisitor;

public class ClassWithFields {
    interface InnerInterface {
        int a = 1;
        String b = "abc";
        Object c = new Object();
        class FIELDS{
            static int d = a;
            Object e = new Object();
        }
    }

    public static int f() {
        return InnerInterface.b.length() + InnerInterface.FIELDS.d;
    }
}

interface OuterInterfaceWithFieldsClass {
    int a = 1;
    String b = "abc";
    Object c = new Object();
    class FIELDS{
        int f = a;
        Object d = new Object();
    }
}
