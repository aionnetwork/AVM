package org.aion.avm.core.shadowing.testString;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class TestResource {

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("singleStringReturnInt")) {
                return ABIEncoder.encodeOneObject(singleStringReturnInt());
            } else if (methodName.equals("singleStringReturnBoolean")) {
                return ABIEncoder.encodeOneObject(singleStringReturnBoolean());
            }  else if (methodName.equals("singleStringReturnChar")) {
                return ABIEncoder.encodeOneObject(singleStringReturnChar());
            }  else if (methodName.equals("singleStringReturnBytes")) {
                return ABIEncoder.encodeOneObject(singleStringReturnBytes());
            }  else if (methodName.equals("singleStringReturnLowerCase")) {
                return ABIEncoder.encodeOneObject(singleStringReturnLowerCase());
            }  else if (methodName.equals("singleStringReturnUpperCase")) {
                return ABIEncoder.encodeOneObject(singleStringReturnUpperCase());
            }   else if (methodName.equals("stringFromCodePoints")) {
                return ABIEncoder.encodeOneObject(stringFromCodePoints());
            } else {
                return new byte[0];
            }
        }
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
