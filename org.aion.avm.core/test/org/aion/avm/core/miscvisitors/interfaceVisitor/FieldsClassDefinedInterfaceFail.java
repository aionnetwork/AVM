package org.aion.avm.core.miscvisitors.interfaceVisitor;

import avm.Blockchain;
import org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces.FIELDSInterfaceFail;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class FieldsClassDefinedInterfaceFail {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String method = decoder.decodeMethodName();

        if (method.equals("getInnerClassString")) {
            return ABIEncoder.encodeOneString(getInnerClassString());
        } else if (method.equals("getInterfaceString")) {
            return ABIEncoder.encodeOneString(getInterfaceString());
        }
        return new byte[0];
    }

    private static String getInnerClassString() {
        return FIELDSInterfaceFail.FIELDS.inner_string;
    }

    private static String getInterfaceString() {
        return FIELDSInterfaceFail.outer_str;
    }
}