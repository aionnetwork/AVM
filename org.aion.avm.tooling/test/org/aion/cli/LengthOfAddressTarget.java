package org.aion.cli;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * A test to verify that we can pass an Address to a call, via the AvmCLI.
 */
public class LengthOfAddressTarget {
    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("getAddressLength")) {
                return ABIEncoder.encodeOneObject(getAddressLength((Address) argValues[0]));
            } else {
                return new byte[0];
            }
        }
    }

    public static int getAddressLength(Address address) {
        return address.unwrap().length;
    }
}
