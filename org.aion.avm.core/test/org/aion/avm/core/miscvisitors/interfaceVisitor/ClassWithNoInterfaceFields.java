package org.aion.avm.core.miscvisitors.interfaceVisitor;

public class ClassWithNoInterfaceFields {
    interface InnerInterface {
        private void f(){}
        private int f2(){ return 2;}
    }
}

interface outerInterfaceNoFields{
    private void f(){}
}
