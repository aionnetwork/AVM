package org.aion.avm.core.miscvisitors.interfaceVisitor;

import org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces.FIELDSInterfaceSuccess;
import org.aion.avm.userlib.abi.ABIEncoder;

public class AcceptFieldsNewAVM {
    public static byte[] main() {
        return ABIEncoder.encodeOneInteger(getSum());
    }

    private static int getSum() {
        return FIELDSInterfaceSuccess.i +
                FIELDSInterfaceSuccess.FIELDS.i +
                FIELDSInterfaceSuccess.FIELDS1.i +
                FIELDSInterfaceSuccess.FIELDS2.i +
                FIELDSInterfaceSuccess.FIELDS__A.i +
                FIELDSInterfaceSuccess.FIELDSA.i;
    }
}
