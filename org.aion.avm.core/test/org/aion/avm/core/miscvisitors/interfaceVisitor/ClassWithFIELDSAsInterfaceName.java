package org.aion.avm.core.miscvisitors.interfaceVisitor;

import org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces.InnerFIELDSInterface;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ClassWithFIELDSAsInterfaceName {
    public static byte[] main(){
        InnerFIELDSImplementation innerFIELDSImplementation = new InnerFIELDSImplementation();
        return ABIEncoder.encodeOneInteger(InnerFIELDSInterface.a + InnerFIELDSInterface.FIELDS.a +
                innerFIELDSImplementation.getA());
    }
}
class InnerFIELDSImplementation implements InnerFIELDSInterface.FIELDS{
    public int getA(){
        return this.hashCode();
    }
}
