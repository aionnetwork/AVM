package org.aion.cli;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * A test to verify that we can pass an Address to a call, via the AvmCLI.
 */
public class LengthOfAddressTarget {
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("getAddressLength")) {
                return ABIEncoder.encodeOneInteger(getAddressLength(decoder.decodeOneAddress()));
            } else {
                return new byte[0];
            }
        }
    }

    public static int getAddressLength(Address address) {
        return address.unwrap().length;
    }
}
