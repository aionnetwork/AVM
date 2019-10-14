package org.aion.avm.core;

import avm.Address;
import avm.Blockchain;
import avm.Result;

import java.math.BigInteger;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * Used by MetaTransactionTestas to verify some energy billing tangentially related to meta-transactions as part of AKI-429.
 */
public class InternalTransactionEnergyTarget {
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("doNothing")) {
                return ABIEncoder.encodeOneByteArray(doNothing());
            } else if (methodName.equals("costOfCall")) {
                return ABIEncoder.encodeOneLong(costOfCall(decoder.decodeOneAddress(), decoder.decodeOneByteArray()));
            } else if (methodName.equals("costOfCreate")) {
                return ABIEncoder.encodeOneLong(costOfCreate(decoder.decodeOneByteArray()));
            } else if (methodName.equals("costOfInvoke")) {
                return ABIEncoder.encodeOneLong(costOfInvoke(decoder.decodeOneByteArray()));
            } else {
                return new byte[0];
            }
        }
    }

    public static byte[] doNothing() {
        // Just return a 1-element array to prove that we ran.
        return new byte[] { 42 };
    }

    public static long costOfCall(Address target, byte[] data) {
        BigInteger value = BigInteger.ZERO;
        long energyLimit = Blockchain.getEnergyLimit();
        long before = Blockchain.getRemainingEnergy();
        Result result = Blockchain.call(target, value, data, energyLimit);
        long after = Blockchain.getRemainingEnergy();
        Blockchain.require(result.isSuccess());
        return before - after;
    }

    public static long costOfCreate(byte[] data) {
        BigInteger value = BigInteger.ZERO;
        long energyLimit = Blockchain.getEnergyLimit();
        long before = Blockchain.getRemainingEnergy();
        Result result = Blockchain.create(value, data, energyLimit);
        long after = Blockchain.getRemainingEnergy();
        Blockchain.require(result.isSuccess());
        return before - after;
    }

    public static long costOfInvoke(byte[] data) {
        long energyLimit = Blockchain.getEnergyLimit();
        long before = Blockchain.getRemainingEnergy();
        Result result = Blockchain.invokeTransaction(data, energyLimit);
        long after = Blockchain.getRemainingEnergy();
        Blockchain.require(result.isSuccess());
        return before - after;
    }
}
