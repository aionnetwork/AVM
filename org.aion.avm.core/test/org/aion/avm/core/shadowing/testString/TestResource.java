package org.aion.avm.core.shadowing.testString;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class TestResource {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("singleStringReturnInt")) {
                return ABIEncoder.encodeOneIntegerArray(singleStringReturnInt());
            } else if (methodName.equals("singleStringReturnBoolean")) {
                return ABIEncoder.encodeOneBooleanArray(singleStringReturnBoolean());
            }  else if (methodName.equals("singleStringReturnChar")) {
                return ABIEncoder.encodeOneCharacter(singleStringReturnChar());
            }  else if (methodName.equals("singleStringReturnBytes")) {
                return ABIEncoder.encodeOneByteArray(singleStringReturnBytes());
            }  else if (methodName.equals("singleStringReturnLowerCase")) {
                return ABIEncoder.encodeOneString(singleStringReturnLowerCase());
            }  else if (methodName.equals("singleStringReturnUpperCase")) {
                return ABIEncoder.encodeOneString(singleStringReturnUpperCase());
            }   else if (methodName.equals("stringReturnSubSequence")) {
                return ABIEncoder.encodeOneString(stringReturnSubSequence());
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

    public static String stringReturnSubSequence(){
        String str = "ReturnSubSequence";
        return str.subSequence(6, 9).toString();
    }
}
