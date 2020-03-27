package org.aion.avm.core;

import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.abi.ABIDecoder;

import java.math.BigInteger;

public class NonDefaultConditionTarget {

    public static byte[] getStorage(byte[] key) {
        return Blockchain.getStorage(key);
    }

    public static void putStorage(byte[] key, byte[] value) {
        Blockchain.putStorage(key, value);
    }

    public static void selfDestruct() {
        Blockchain.selfDestruct(Blockchain.getCaller());
    }

    public static void call(byte[] dappBytes, boolean expectedResult) {
        Result createResult = Blockchain.create(BigInteger.ZERO, dappBytes, Blockchain.getRemainingEnergy());
        Blockchain.require(expectedResult == createResult.isSuccess());
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();

        if (methodName == null) {
            return new byte[0];
        } else {
            switch (methodName) {
                case "getStorage": {
                    byte[] key = decoder.decodeOneByteArray();
                    return getStorage(key);
                }
                case "putStorage": {
                    byte[] key = decoder.decodeOneByteArray();
                    byte[] value = decoder.decodeOneByteArray();
                    putStorage(key, value);
                    break;
                }
                case "selfDestruct":
                    selfDestruct();
                    break;
                case "call":
                    call(decoder.decodeOneByteArray(), decoder.decodeOneBoolean());
                    break;
            }
            return new byte[0];
        }
    }
}
