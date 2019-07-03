package org.aion.avm.tooling;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

public class ChargeEnergyTarget {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String method = decoder.decodeMethodName();

        try {
            if (method.equals("systemArrayCopyNegative")) {
                systemArrayCopyNegative();
            } else if (method.equals("systemArrayCopyOverflow")) {
                systemArrayCopyOverflow();
            } else if (method.equals("stringRegionMatches1Negative")) {
                stringRegionMatches1Negative();
            } else if (method.equals("stringRegionMatches1Overflow")) {
                stringRegionMatches1Overflow();
            } else if (method.equals("stringRegionMatches2Negative")) {
                stringRegionMatches2Negative();
            } else if (method.equals("stringRegionMatches2Overflow")) {
                stringRegionMatches2Overflow();
            } else if (method.equals("stringValueOfNegative")) {
                stringValueOfNegative();
            } else if (method.equals("stringValueOfOverflow")) {
                stringValueOfOverflow();
            } else if (method.equals("stringCopyValueOfNegative")) {
                stringCopyValueOfNegative();
            } else if (method.equals("stringCopyValueOfOverflow")) {
                stringCopyValueOfOverflow();
            } else if (method.equals("stringGetCharsOverflow")) {
                stringGetCharsOverflow();
            } else if (method.equals("stringIndexOfOverflow")) {
                stringIndexOfOverflow();
            } else if (method.equals("stringLastIndexOf1Overflow")) {
                stringLastIndexOf1Overflow();
            } else if (method.equals("stringLastIndexOf2Overflow")) {
                stringLastIndexOf2Overflow();
            } else if (method.equals("stringSubstring1Overflow")) {
                stringSubstring1Overflow();
            } else if (method.equals("stringSubstring2Overflow")) {
                stringSubstring2Overflow();
            } else if (method.equals("stringSubsequenceOverflow")) {
                stringSubsequenceOverflow();
            }
        } catch (Exception e) {
            // This is to prevent the call from failing, so that the actual tampered-with limit gets through.
        }

        return null;
    }

    public static void stringSubsequenceOverflow() {
        "".subSequence(0, Integer.MAX_VALUE);
    }

    public static void stringSubstring1Overflow() {
        // Internally we are doing string length (zero) minus -(Integer.MIN_VALUE + 1)
        // Note -(Integer.MIN_VALUE + 1) == Integer.MAX_VALUE
        "".substring(Integer.MIN_VALUE + 1);
    }

    public static void stringSubstring2Overflow() {
        "".substring(0, Integer.MAX_VALUE);
    }

    public static void stringLastIndexOf1Overflow() {
        // Internally we are doing string length (zero) minus -(Integer.MIN_VALUE + 1)
        // Note -(Integer.MIN_VALUE + 1) == Integer.MAX_VALUE
        "".lastIndexOf(' ', Integer.MIN_VALUE + 1);
    }

    public static void stringLastIndexOf2Overflow() {
        // Internally we are doing string length (zero) minus -(Integer.MIN_VALUE + 1)
        // Note -(Integer.MIN_VALUE + 1) == Integer.MAX_VALUE
        "".lastIndexOf("", Integer.MIN_VALUE + 1);
    }

    public static void stringIndexOfOverflow() {
        // Internally we are doing string length (zero) minus -(Integer.MIN_VALUE + 1)
        // Note -(Integer.MIN_VALUE + 1) == Integer.MAX_VALUE
        "".indexOf(' ', Integer.MIN_VALUE + 1);
    }

    public static void stringGetCharsOverflow() {
        "".getChars(0, Integer.MAX_VALUE, new char[0], 0);
    }

    public static void stringCopyValueOfNegative() {
        String.copyValueOf(new char[0], 0, Integer.MIN_VALUE);
    }

    public static void stringCopyValueOfOverflow() {
        String.copyValueOf(new char[0], 0, Integer.MAX_VALUE);
    }

    public static void stringValueOfNegative() {
        String.valueOf(new char[0], 0, Integer.MIN_VALUE);
    }

    public static void stringValueOfOverflow() {
        String.valueOf(new char[0], 0, Integer.MAX_VALUE);
    }

    public static void stringRegionMatches1Negative() {
        "".regionMatches(0, "", 0, Integer.MIN_VALUE);
    }

    public static void stringRegionMatches1Overflow() {
        "".regionMatches(0, "", 0, Integer.MAX_VALUE);
    }

    public static void stringRegionMatches2Negative() {
        "".regionMatches(true, 0, "", 0, Integer.MIN_VALUE);
    }

    public static void stringRegionMatches2Overflow() {
        "".regionMatches(true, 0, "", 0, Integer.MAX_VALUE);
    }

    public static void systemArrayCopyNegative() {
        System.arraycopy(new byte[0], 0, new byte[0], 0, Integer.MIN_VALUE);
    }

    public static void systemArrayCopyOverflow() {
        System.arraycopy(new byte[0], 0, new byte[0], 0, Integer.MAX_VALUE);
    }
}
