package org.aion.avm.core.miscvisitors.interfaceVisitor;

import org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces.InterfaceNoFields;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ImplementationNoFields implements InterfaceNoFields {

    @Override
    public int getA(int a) {
        return a * 1001;
    }

    public static byte[] main(){
        ImplementationNoFields implementationNoFields = new ImplementationNoFields();
        return ABIEncoder.encodeOneInteger(implementationNoFields.getA(22));
    }
}
