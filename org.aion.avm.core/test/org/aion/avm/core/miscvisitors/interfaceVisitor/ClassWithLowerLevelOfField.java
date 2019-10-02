package org.aion.avm.core.miscvisitors.interfaceVisitor;

public class ClassWithLowerLevelOfField {
    interface InnerInterface {
        int a = 1;
        String b = "abc";
        Object c = new Object();
        class innerClass{
            int f = a;
            Object d = new Object();
            static class FIELDS {
                public String fields = "FIELDS";
            }
        }
    }

    public static int f() {
        InnerInterface.innerClass.FIELDS f = new InnerInterface.innerClass.FIELDS();
        return InnerInterface.b.length() + f.fields.length() + OuterInterfaceFields.innerClass.getLength();
    }
}

interface OuterInterfaceFields {
    int a = 2;
    String b = "def";
    Object c = new Object();
    class innerClass{
        static int getLength() {
            FIELDS f = new FIELDS();
            return f.fields.length();
        }
        static class FIELDS {
            public String fields = "OUTER_FIELDS";
        }
    }
}
