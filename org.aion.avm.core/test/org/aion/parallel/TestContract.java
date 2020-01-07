package org.aion.parallel;

import java.math.BigInteger;
import java.util.Arrays;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


public class TestContract {

    static Address deployer;
    static int callCount;
    static int value;
    private static TestContract root;

    public TestContract nextLink;

    static {
        deployer = new Address(Blockchain.getCaller().toByteArray());
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("doTransfer")) {
                doTransfer();
                return new byte[0];
            } else if (methodName.equals("addValue")) {
                addValue();
                return new byte[0];
            } else if (methodName.equals("getCallCount")) {
                return ABIEncoder.encodeOneInteger(getCallCount());
            } else if (methodName.equals("getValue")) {
                return ABIEncoder.encodeOneInteger(getValue());
            } else if (methodName.equals("doCallThis")) {
                doCallThis(decoder.decodeOneByteArray());
                return new byte[0];
            } else if (methodName.equals("doCallOther")) {
                doCallOther(decoder.decodeOneAddress(), decoder.decodeOneByteArray());
                return new byte[0];
            } else if(methodName.equals("deploy")){
                deploy(decoder.decodeOneByteArray());
                return new byte[0];
            } else if(methodName.equals("addLink")){
                addLink();
                return new byte[0];
            } else if(methodName.equals("getBalance")) {
                return ABIEncoder.encodeOneBigInteger(getBalance(decoder.decodeOneAddress(), decoder.decodeOneInteger()));
            } else {
                return new byte[0];
            }
        }
    }

    public static void doTransfer() {
        Blockchain.call(deployer, BigInteger.valueOf(1000), new byte[0], 100_000L);
    }

    public static void addValue() {
        value++;
    }

    public static void doCallThis(byte[] data) {
        callCount++;
        Blockchain.require(Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, 1000000).isSuccess());
    }

    public static void doCallOther(Address callee, byte[] data) {
        callCount++;
        Blockchain.require(Blockchain.call(callee, BigInteger.ZERO, data, 1000000).isSuccess());
    }

    public static int getCallCount() {
        return callCount;
    }

    public static int getValue() {
        return value;
    }

    public static void deploy(byte[] data) {
        Blockchain.require(
                Blockchain.create(BigInteger.ZERO, data, Blockchain.getRemainingEnergy()).isSuccess());
    }

    public static void addLink() {
        TestContract instance = new TestContract();
        instance.nextLink = TestContract.root;
        TestContract.root = instance;
    }

    public static BigInteger getBalance(Address address, int iteration) {
        int i = 0;
        while (i < iteration) {
            byte[] n = new byte[32];
            Arrays.fill(n, 0, 32, Byte.MAX_VALUE);
            StrictMath.sqrt(Double.MAX_VALUE);
            new BigInteger(n).sqrt();
            i++;
        }
        return Blockchain.getBalance(address);
    }
}
