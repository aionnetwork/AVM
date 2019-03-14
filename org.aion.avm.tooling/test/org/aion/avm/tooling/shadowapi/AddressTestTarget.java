package org.aion.avm.tooling.shadowapi;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;


public class AddressTestTarget {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(AddressTestTarget.class, BlockchainRuntime.getData());
    }

    public static String getToString(Address address) {
        return address.toString();
    }

    public static boolean getEquals(Address address1, Address address2) {
        return address1.equals(address2);
    }

    public static int getHashCode(Address address) {
        return address.hashCode();
    }
}
