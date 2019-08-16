package org.aion.avm.core.transformation;

import avm.Address;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

import java.math.BigInteger;

public class SampleContract {

    private static String name;
    private static int changeCount = 0;

    static {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        name = decoder.decodeOneString();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("setName")) {
                setName(decoder.decodeOneString());
                return new byte[0];
            } else if (methodName.equals("getName")) {
                return ABIEncoder.encodeOneString(getName());
            } else if (methodName.equals("getChangeCount")) {
                return ABIEncoder.encodeOneInteger(getChangeCount());
            } else if (methodName.equals("call")) {
                call(decoder.decodeOneAddress());
                return new byte[0];
            } else if (methodName.equals("selfDestruct")) {
                selfDestruct();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    private static void setName(String newName) {
        name = newName;
        changeCount++;
    }

    private static String getName() {
        return name;
    }

    private static int getChangeCount() {
        return changeCount;
    }

    private static void call(Address target) {
        Result result = Blockchain.call(target, BigInteger.ZERO, new byte[0], Blockchain.getRemainingEnergy());
        Blockchain.require(!result.isSuccess());
    }

    private static void selfDestruct() {
        Blockchain.selfDestruct(Blockchain.getCaller());
    }
}
