package org.aion.rules;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIDecoder;

public class JUnitRuleTestTarget {

    private static int number;
    private static AionMap<Integer, String> map1 = new AionMap<>();
    private static byte[] TOPIC1 = new byte[]{ 0xf, 0xe, 0xd, 0xc, 0xb, 0xa };
    private static byte[] DATA1 = new byte[]{ 0x1 };
    private static int intVal;
    public static Address owner;

    static {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        intVal =  decoder.decodeOneInteger();
        owner = BlockchainRuntime.getCaller();
    }

    @Callable
    public static int sum(int a, int b) {
        return a + b;
    }

    @Callable
    public static boolean increaseNumber(int input) {
        if (input > number) {
            number = input;
            return true;
        } else
            return false;
    }

    @Callable
    public static void mapPut(int key, String value) {
        map1.put(key, value);
    }

    @Callable
    public static String mapGet(int key) {
        return map1.get(key);
    }

    @Callable
    public static void logEvent(){
        BlockchainRuntime.log(DATA1);
        BlockchainRuntime.log(TOPIC1, DATA1);
    }
}

