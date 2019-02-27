package org.aion.avm.tooling.encoding;


import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class AddressEndodeDecodeTarget {

    private static Address storedAddress;

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(AddressEndodeDecodeTarget.class, BlockchainRuntime.getData());
    }

    public static boolean addressEncodeDecode() {
        byte[] encoded = ABIEncoder.encodeOneObject(BlockchainRuntime.getAddress());
        Address result = (Address) ABIDecoder.decodeOneObject(encoded);
        return result.equals(BlockchainRuntime.getAddress());
    }

    public static boolean createAddress() {
        Address tempAddress = new Address(bytesOfLength(Address.LENGTH));
        byte[] encoded = ABIEncoder.encodeOneObject(tempAddress);
        Address result = (Address) ABIDecoder.decodeOneObject(encoded);
        return result.equals(BlockchainRuntime.getAddress());
    }

    public static void saveAddress(Address input) {
        storedAddress = input;
    }

    public static Address getStoredAddress() {
        return storedAddress;
    }

    public static boolean checkAddressArrayArgument(Address[] contractAddresses) {
        for (Address contractAddress : contractAddresses) {
            if (contractAddress.equals(storedAddress))
                return true;
        }
        return false;
    }

    public static Address[] getAddressArray(int count) {
        Address[] addressArray = new Address[count];
        for (int i = 0; i < count; i++) {
            addressArray[i] = new Address(bytesOfLength(Address.LENGTH));
        }
        return addressArray;
    }

    public static Address[] addressArrayEncodeDecode(Address[] addresses) {
        byte[] encoded = ABIEncoder.encodeOneObject(addresses);
        Address[] result = (Address[]) ABIDecoder.decodeOneObject(encoded);
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
