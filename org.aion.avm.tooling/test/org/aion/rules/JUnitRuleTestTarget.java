package org.aion.rules;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;

public class JUnitRuleTestTarget {

    private static int number;
    private static AionMap<Integer, String> map1 = new AionMap<>();
    private static byte[] TOPIC1 = new byte[]{ 0xf, 0xe, 0xd, 0xc, 0xb, 0xa };
    private static byte[] DATA1 = new byte[]{ 0x1 };
    private static int intVal;
    public static Address owner;

    static {
        Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
        intVal =  (Integer) arguments[0];
        owner = BlockchainRuntime.getCaller();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(JUnitRuleTestTarget.class, BlockchainRuntime.getData());
    }

    public static int sum(int a, int b) {
        return a + b;
    }

    public static boolean increaseNumber(int input) {
        if (input > number) {
            number = input;
            return true;
        } else
            return false;
    }

    public static void mapPut(int key, String value) {
        map1.put(key, value);
    }

    public static String mapGet(int key) {
        return map1.get(key);
    }

    public static void logEvent(){
        BlockchainRuntime.log(DATA1);
        BlockchainRuntime.log(TOPIC1, DATA1);
    }
}

