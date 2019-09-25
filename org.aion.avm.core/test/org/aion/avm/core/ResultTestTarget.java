package org.aion.avm.core;

import org.aion.avm.userlib.abi.ABIDecoder;

import avm.Address;
import avm.Blockchain;
import avm.Result;


/**
 * The test class loaded by ResultTest.
 * NOTE:  We use the ABI for parameters, for convenience, but we directly return values since we want to test the specifics of what is returned in various cases.
 */
public class ResultTestTarget {
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String method = decoder.decodeMethodName();
        byte[] result = null;

        if (method.equals("returnData")) {
            result = returnData();
        } else if (method.equals("returnNull")) {
            result = returnNull();
        } else if (method.equals("revert")) {
            result = revert();
        } else if (method.equals("uncaught")) {
            result = uncaught();
        } else if (method.equals("outOfEnergy")) {
            result = outOfEnergy();
        } else if (method.equals("testCall")) {
            result = testCall(decoder.decodeOneAddress(), decoder.decodeOneByteArray(), decoder.decodeOneBoolean());
        } else if (method.equals("testTransfer")) {
            result = testTransfer(decoder.decodeOneAddress(), decoder.decodeOneByteArray());
        } else if (method.equals("testDeploy")) {
            result = testDeploy(decoder.decodeOneByteArray());
        }

        return result;
    }

    public static byte[] returnData() {
        return new byte[] { 1, 2, 3};
    }

    public static byte[] returnNull() {
        return null;
    }

    public static byte[] revert() {
        Blockchain.revert();
        return null;
    }

    public static byte[] uncaught() {
        // Just throw an uncaught exception.
        throw new RuntimeException();
    }

    public static byte[] outOfEnergy() {
        while (true) {
            // Spin until we are out of energy.
        }
    }

    public static byte[] testCall(Address target, byte[] data, boolean expectToPass) {
        // We send in whatever value we received, so the external test can measure this.
        Result result = Blockchain.call(target, Blockchain.getValue(), data, Blockchain.getEnergyLimit());
        // Check whether this was expected to pass or fail.
        Blockchain.require(expectToPass == result.isSuccess());
        // Make sure that the toString works.
        Blockchain.require(null != result.toString());
        // Return whatever the result data was.
        return result.getReturnData();
    }

    public static byte[] testTransfer(Address target, byte[] data) {
        // We send in whatever value we received, so the external test can measure this.
        Result result = Blockchain.call(target, Blockchain.getValue(), data, Blockchain.getEnergyLimit());
        // Make sure that the toString works.
        Blockchain.require(null != result.toString());
        // A balance transfer is always a success but always returns null.
        Blockchain.require(result.isSuccess());
        byte[] returnedData = result.getReturnData();
        Blockchain.require(null == returnedData);
        return returnedData;
    }

    public static byte[] testDeploy(byte[] data) {
        // We send in whatever value we received, so the external test can measure this.
        Result result = Blockchain.create(Blockchain.getValue(), data, Blockchain.getEnergyLimit());
        // Make sure that the toString works.
        Blockchain.require(null != result.toString());
        // We expect this deploy to succeed.
        Blockchain.require(result.isSuccess());
        // Return whatever the result data was.
        return result.getReturnData();
    }
}
