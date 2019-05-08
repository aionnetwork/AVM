package org.aion.avm.tooling;

import avm.Address;
import avm.Blockchain;
import avm.Result;
import java.math.BigInteger;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIEncoder;

public class DappManipulator {
    private static Address address;

    static {
        address = Blockchain.getAddress();
    }

    @Callable
    public static byte[] getAddress() {
        return address.toByteArray();
    }

    @Callable
    public static void manipulateField(Address address) {
        byte[] data = ABIEncoder.encodeOneString("getField");
        Result result = Blockchain.call(address, BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        Blockchain.require(result.isSuccess());

        // Modify the returned data.
        byte flippedByte = (byte) ~result.getReturnData()[0];
        result.getReturnData()[0] = flippedByte;

        // Call again and verify that the original result is returned back.
        result = Blockchain.call(address, BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        Blockchain.require(result.isSuccess());

        Blockchain.require(result.getReturnData()[0] != flippedByte);
    }

    @Callable
    public static byte[] manipulateDeployAddress(byte[] jar) {
        Result result = Blockchain.create(BigInteger.ZERO, jar, Blockchain.getRemainingEnergy());
        Blockchain.require(result.isSuccess());

        byte[] originalAddress = new byte[32];
        System.arraycopy(result.getReturnData(), 0, originalAddress, 0, 32);

        // Modify the returned data.
        result.getReturnData()[0] = (byte) ~result.getReturnData()[0];

        // Return the original address.
        return originalAddress;
    }
}
