package org.aion.avm.core.shadowing.testString;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class TestResource {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
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
            }  else if (methodName.equals("equalsIgnoreCase")) {
                return ABIEncoder.encodeOneBoolean(equalsIgnoreCase());
            } else if (methodName.equals("regionMatches")) {
                return ABIEncoder.encodeOneBoolean(regionMatches());
            }  else if (methodName.equals("valueOf")) {
                return ABIEncoder.encodeOneBoolean(valueOf());
            } else if (methodName.equals("regionMatchesInvalidLength")) {
                regionMatchesInvalidLength();
            } else if (methodName.equals("regionMatchesDoNotIgnoreCaseInvalidLength")) {
                regionMatchesDoNotIgnoreCaseInvalidLength();
            } else if (methodName.equals("copyValueOfInvalidCount")) {
                copyValueOfInvalidCount();
            } else if (methodName.equals("valueOfInvalidCount")) {
                valueOfInvalidCount();
            } else if (methodName.equals("contentEquals")) {
                contentEquals();
            } else if (methodName.equals("compareTo")) {
                compareTo();
            } else {
                return new byte[0];
            }
        }
        return new byte[0];
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

    public static boolean equalsIgnoreCase(){
        String str = "equalsIgnoreCase";
        return str.equalsIgnoreCase(null);
    }

    public static boolean regionMatches() {
        String str = "my test String str";
        boolean res = str.regionMatches(3, "test", 0, 4);
        return res & str.regionMatches(true, 8, "STR", 0, 3);
    }

    public static boolean valueOf() {
        boolean res = String.valueOf(new char[] {'a', 'b', 'c', 'd'}, 0, 2).equals("ab");
        return res & String.copyValueOf(new char[] {'a', 'b', 'c', 'd'}, 2, 2).equals("cd");
    }

    public static void regionMatchesInvalidLength() {
        String s = "asdfghjklertuyuiuiop";
        long remainingEnergy = Blockchain.getRemainingEnergy();
        s.regionMatches(0, "asdfghjk", 0, -1000000);
        Blockchain.require(remainingEnergy > Blockchain.getRemainingEnergy());
    }

    public static void regionMatchesDoNotIgnoreCaseInvalidLength() {
        String s = "asdfghjklertuyuiuiop";
        long remainingEnergy = Blockchain.getRemainingEnergy();
        s.regionMatches(false, 0, "asdfghjk", 0, -1000000);
        Blockchain.require(remainingEnergy > Blockchain.getRemainingEnergy());
    }

    public static void valueOfInvalidCount(){
        String s = "asdfghjklertuyuiuiop";
        boolean exceptionThrown = false;
        long remainingEnergy = 0l;
        try {
            char[] c = new char[]{'a', 's', 's', 'f'};
            remainingEnergy = Blockchain.getRemainingEnergy();
            String.valueOf(c, 0, -100000);

        } catch (StringIndexOutOfBoundsException e){
            exceptionThrown = true;
            Blockchain.require(remainingEnergy > Blockchain.getRemainingEnergy());
        }
        Blockchain.require(exceptionThrown);
    }

    public static void copyValueOfInvalidCount(){
        String s = "asdfghjklertuyuiuiop";
        boolean exceptionThrown = false;
        long remainingEnergy = 0l;
        try {
            char[] c = new char[]{'a', 's', 's', 'f'};
            remainingEnergy = Blockchain.getRemainingEnergy();
            String.copyValueOf(c, 0, -10000);

        } catch (StringIndexOutOfBoundsException e){
            exceptionThrown = true;
            Blockchain.require(remainingEnergy > Blockchain.getRemainingEnergy());
        }
        Blockchain.require(exceptionThrown);
    }

    public static void contentEquals(){
        String str1 = "First String";
        String str2 = "Second String";
        StringBuffer str3 = new StringBuffer("Second String");
        CharSequence str4 = "First String";

        Blockchain.require(!str1.contentEquals(str3));
        Blockchain.require(str2.contentEquals(str3));
        Blockchain.require(str1.contentEquals(str4));
        Blockchain.require(!str2.contentEquals(str4));

        str3 = null;
        str4 = null;

        boolean NPEThrownForStringBuffer = false;
        boolean NPEThrownForCharSequence = false;
        long energy = 0;

        try {
            energy = Blockchain.getRemainingEnergy();
            str1.contentEquals(str3);
        } catch (NullPointerException e){
            long energyAfter = Blockchain.getRemainingEnergy();
            // ensure contentEquals consumed energy
            Blockchain.require(energyAfter + 400 < energy);
            NPEThrownForStringBuffer = true;
        }

        try {
            energy = Blockchain.getRemainingEnergy();
            str2.contentEquals(str4);
        } catch (NullPointerException e){
            long energyAfter = Blockchain.getRemainingEnergy();
            // ensure contentEquals consumed energy
            Blockchain.require(energyAfter + 400 < energy);
            NPEThrownForCharSequence = true;
        }

        Blockchain.require(NPEThrownForStringBuffer && NPEThrownForCharSequence);

        energy = Blockchain.getRemainingEnergy();
        str2.equals(null);
        Blockchain.require(Blockchain.getRemainingEnergy() + 300 < energy);
    }

    public static void compareTo() {
        String str1 = "First String";
        String str2 = "Second String";
        Blockchain.require(str1.compareTo(str2) < 0);

        long energy = 0;
        boolean NPEThrown = false;

        try {
            energy = Blockchain.getRemainingEnergy();
            str1.compareTo(null);
        } catch (NullPointerException e){
            long energyAfter = Blockchain.getRemainingEnergy();
            // ensure compareTo consumed energy
            Blockchain.require(energyAfter + 400 < energy);
            NPEThrown = true;
        }

        Blockchain.require(NPEThrown);
        NPEThrown = false;

        try {
            energy = Blockchain.getRemainingEnergy();
            str1.compareToIgnoreCase(null);
        } catch (NullPointerException e){
            long energyAfter = Blockchain.getRemainingEnergy();
            // ensure compareToIgnoreCase consumed energy
            Blockchain.require(energyAfter + 400 < energy);
            NPEThrown = true;
        }
        Blockchain.require(NPEThrown);
    }
}
