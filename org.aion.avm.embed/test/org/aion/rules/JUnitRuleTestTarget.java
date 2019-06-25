package org.aion.rules;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.tooling.abi.Initializable;
import org.aion.avm.userlib.AionMap;

public class JUnitRuleTestTarget {

    private static int number;
    private static AionMap<Integer, String> map1 = new AionMap<>();
    private static byte[] TOPIC1 = new byte[]{ 0xf, 0xe, 0xd, 0xc, 0xb, 0xa };
    private static byte[] DATA1 = new byte[]{ 0x1 };
    @Initializable
    private static int intVal;
    public static Address owner;

    static {
        owner = Blockchain.getCaller();
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
        Blockchain.log(DATA1);
        Blockchain.log(TOPIC1, DATA1);
    }
}

