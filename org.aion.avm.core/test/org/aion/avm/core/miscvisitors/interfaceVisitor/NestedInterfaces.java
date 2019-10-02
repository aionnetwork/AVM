package org.aion.avm.core.miscvisitors.interfaceVisitor;

public class NestedInterfaces {
    interface InnerInterface {
        int a = 1;
        String b = "abc";

        interface InnerInterfaceLevel2 {
            int a = 2;
            String c = "edfged";

            interface InnerInterfaceLevel3 {
                int a = 3;
                String d = "edfgedqwe";
            }
        }
    }

    public static int f() {
        return InnerInterface.b.length() + InnerInterface.InnerInterfaceLevel2.c.length() +
                InnerInterface.InnerInterfaceLevel2.InnerInterfaceLevel3.d.length();
    }
}
