package org.aion.avm.core.shadowing.testString;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class TestResource {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(TestResource.class, BlockchainRuntime.getData());
    }

    public static int[] singleStringReturnInt() {
        int[] results = new int[4];
        int i = 0;

        String str1 = new String("abc");
        results[i++] = str1.hashCode();
        results[i++] = str1.length();
        results[i++] = str1.indexOf("b");
        results[i++] = str1.indexOf("d");

        return results;
    }

    public static boolean[] singleStringReturnBoolean() {
        boolean[] results = new boolean[7];
        int i = 0;

        String str1 = new String("abc");
        results[i++] = str1.contains("c");
        results[i++] = str1.contains("d");
        results[i++] = str1.equals("abc");
        results[i++] = str1.equals("def");
        results[i++] = str1.startsWith("a");
        results[i++] = str1.startsWith("b");
        results[i++] = str1.isEmpty();

        return results;
    }

    public static char singleStringReturnChar() {
        String str1 = new String("abc");
        return str1.charAt(0);
    }

    public static byte[] singleStringReturnBytes() {
        String str1 = new String("abc");
        return str1.getBytes();
    }

    public static String singleStringReturnLowerCase() {
        String str1 = new String("abc");
        return str1.toLowerCase();
    }

    public static String singleStringReturnUpperCase() {
        String str1 = new String("abc");
        return str1.toUpperCase();
    }

    public static String stringFromCodePoints(){
        String str1 = new String(new int[]{104, 101, 108, 108, 111, 1593}, 0, 5);
        return str1;
    }
}
