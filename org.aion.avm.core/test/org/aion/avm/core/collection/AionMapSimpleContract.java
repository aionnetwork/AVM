package org.aion.avm.core.collection;

import avm.Blockchain;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class AionMapSimpleContract {

    private static AionMap<Integer, String> map = new AionMap<>();

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("put")) {
                put(decoder.decodeOneInteger());
                return new byte[0];
            } else if (methodName.equals("get")) {
                return ABIEncoder.encodeOneString(get(decoder.decodeOneInteger()));
            } else if (methodName.equals("remove")) {
                return ABIEncoder.encodeOneString(remove(decoder.decodeOneInteger()));
            } else {
                return new byte[0];
            }
        }
    }

    public static void put(int key) {
        map.put(key, "STRING");
    }

    public static String get(int key) {
        return map.get(key);
    }

    public static String remove(int key) {
        return map.remove(key);
    }
}
