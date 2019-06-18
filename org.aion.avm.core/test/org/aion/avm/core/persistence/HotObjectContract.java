package org.aion.avm.core.persistence;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

import java.math.BigInteger;

public class HotObjectContract {

    private static int value = 5;
    private static String str = "initial";
    private static Object[] objArr;
    private static int[] intArr = new int[]{0, 0, 0, 0};
    private static int callCount = 0;

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            switch (methodName) {
                case "doubleStaticValue":
                    return ABIEncoder.encodeOneInteger(doubleStaticValue());
                case "revert":
                    revert();
                    break;
                case "exception":
                    exception();
                    break;
                case "getValue":
                    return ABIEncoder.encodeOneInteger(getValue());
                case "getCallCount":
                    return ABIEncoder.encodeOneInteger(getCallCount());
                case "getStr":
                    return ABIEncoder.encodeOneString(getStr());
                case "makeCall":
                    makeCall(decoder.decodeOneAddress(), decoder.decodeOneByteArray());
                    break;
                case "writeToObjectArray":
                    writeToObjectArray(decoder.decodeOneInteger(), decoder.decodeOneInteger());
                    break;
                case "updateIntArray":
                    updateIntArray(decoder.decodeOneInteger());
                    break;
                case "readIntArray":
                    return ABIEncoder.encodeOneIntegerArray(readIntArray());
                case "selfDestruct":
                    selfDestruct();
                    break;
            }
            return null;
        }
    }

    public static int doubleStaticValue() {
        value *= 2;
        return value;
    }

    public static void revert() {
        str = "modifiedRevert";
        // false condition
        Blockchain.require(str.equals("initial"));
    }

    public static void exception() {
        str = "modifiedException";
        // false condition
        if (!str.equals("initial")) {
            throw new RuntimeException();
        }
    }

    public static void makeCall(Address callee, byte[] data) {
        callCount++;
        Blockchain.call(callee, BigInteger.ZERO, data, 1000000);
    }

    public static void updateIntArray(int index) {
        intArr[index] = 10;
    }

    public static int[] readIntArray() {
        return intArr;
    }

    public static int getValue() {
        return value;
    }

    public static String getStr() {
        return str;
    }

    public static int getCallCount() {
        return callCount;
    }

    public static void writeToObjectArray(int objectArraySize, int intArraySize) {
        objArr = new Object[objectArraySize];
        for (int i = 0; i < objArr.length; i++) {
            objArr[i] = new int[intArraySize];
        }
    }

    public static void selfDestruct(){
        Blockchain.selfDestruct(Blockchain.getCaller());
    }
}
