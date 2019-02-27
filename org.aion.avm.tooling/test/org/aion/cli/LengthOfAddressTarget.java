package org.aion.cli;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;


/**
 * A test to verify that we can pass an Address to a call, via the AvmCLI.
 */
public class LengthOfAddressTarget {
    public static byte[] main() {
        byte[] input = BlockchainRuntime.getData();
        return ABIDecoder.decodeAndRunWithClass(LengthOfAddressTarget.class, input);
    }

    public static int getAddressLength(Address address) {
        return address.unwrap().length;
    }
}
