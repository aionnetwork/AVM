package org.aion.avm.core.miscvisitors.interfaceVisitor;

import org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces.NoFIELDSInterface;
import org.aion.avm.userlib.abi.ABIEncoder;

public class FieldsNotDefinedSuccess {

    public static byte[] main() {
        return ABIEncoder.encodeOneInteger(getSum());
    }

    private static int getSum() {
        NoFIELDSInterface.obj.toString();
        return NoFIELDSInterface.outer_string.length() +
                NoFIELDSInterface.FIELDS2.i +
                NoFIELDSInterface.FIELDS__A.i +
                NoFIELDSInterface.FIELDSA.i +
                NoFIELDSInterface.FIELDS2.FIELDS.i;
    }
}
