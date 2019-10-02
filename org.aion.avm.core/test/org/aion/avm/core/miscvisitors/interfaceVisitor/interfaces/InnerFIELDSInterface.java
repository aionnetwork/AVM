package org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces;

public interface InnerFIELDSInterface {
    int a = 100;
    interface FIELDS {
        Object o = new Object();
        int a = 200 + o.hashCode();
        int getA();
    }
}
