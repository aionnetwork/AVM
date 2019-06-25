package org.aion.avm.embed.shadowapi;

import avm.Address;
import org.aion.avm.tooling.abi.Callable;


public class AddressTestTarget {

    @Callable
    public static String getToString(Address address) {
        return address.toString();
    }

    @Callable
    public static boolean getEquals(Address address1, Address address2) {
        return address1.equals(address2);
    }

    @Callable
    public static int getHashCode(Address address) {
        return address.hashCode();
    }
}
