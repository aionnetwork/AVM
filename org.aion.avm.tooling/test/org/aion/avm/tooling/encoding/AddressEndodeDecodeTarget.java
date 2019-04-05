package org.aion.avm.tooling.encoding;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class AddressEndodeDecodeTarget {

    private static Address storedAddress;

    @Callable
    public static boolean addressEncodeDecode() {
        byte[] encoded = ABIEncoder.encodeOneAddress(Blockchain.getAddress());
        ABIDecoder decoder = new ABIDecoder(encoded);
        Address result = decoder.decodeOneAddress();
        return result.equals(Blockchain.getAddress());
    }

    @Callable
    public static boolean createAddress() {
        Address tempAddress = new Address(bytesOfLength(Address.LENGTH));
        byte[] encoded = ABIEncoder.encodeOneAddress(tempAddress);
        ABIDecoder decoder = new ABIDecoder(encoded);
        Address result = decoder.decodeOneAddress();
        return result.equals(Blockchain.getAddress());
    }

    @Callable
    public static void saveAddress(Address input) {
        storedAddress = input;
    }

    @Callable
    public static Address getStoredAddress() {
        return storedAddress;
    }

    @Callable
    public static boolean checkAddressArrayArgument(Address[] contractAddresses) {
        for (Address contractAddress : contractAddresses) {
            if (contractAddress.equals(storedAddress))
                return true;
        }
        return false;
    }

    @Callable
    public static Address[] getAddressArray(int count) {
        Address[] addressArray = new Address[count];
        for (int i = 0; i < count; i++) {
            addressArray[i] = new Address(bytesOfLength(Address.LENGTH));
        }
        return addressArray;
    }

    @Callable
    public static Address[] addressArrayEncodeDecode(Address[] addresses) {
        byte[] encoded = ABIEncoder.encodeOneAddressArray(addresses);
        ABIDecoder decoder = new ABIDecoder(encoded);
        Address[] result = decoder.decodeOneAddressArray();
        return result;
    }

    private static byte[] bytesOfLength(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) i;
        }
        return data;
    }
}
