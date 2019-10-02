package org.aion.avm.core.miscvisitors.interfaceVisitor;

import org.aion.avm.userlib.abi.ABIEncoder;

public class ClassWithFieldsInterface {
    interface FIELDS {
        String b = "abc";
        Object c = new Object();
        class FIELDSA{
            static Object d = new Object();
            static int f = d.hashCode();
        }
    }

    public static byte[] main() {
        return ABIEncoder.encodeOneInteger(FIELDS.b.length() + FIELDS.c.hashCode() + FIELDS.FIELDSA.f);
    }
}


