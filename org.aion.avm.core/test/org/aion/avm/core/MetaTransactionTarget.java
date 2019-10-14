package org.aion.avm.core;

import avm.Address;
import avm.Blockchain;
import avm.Result;

import java.math.BigInteger;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * A test of the AIP #044 meta-transaction functionality.
 */
public class MetaTransactionTarget {
    private static byte[] transaction;

    static {
        // Note that we optionally send a meta-transaction within the deployment to test invocations within <clinit>.
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        boolean expectHighEnergyLimit = decoder.decodeOneBoolean();
        byte[] invokable = decoder.decodeOneByteArray();
        boolean interpretAsApiCreate = decoder.decodeOneBoolean();
        if (null != invokable) {
            Result result = null;
            if (interpretAsApiCreate) {
                result = Blockchain.create(BigInteger.ZERO, invokable, Blockchain.getEnergyLimit());
            } else {
                result = Blockchain.invokeTransaction(invokable, Blockchain.getEnergyLimit());
            }
            Blockchain.require(result.isSuccess());
        }
        // Most of the time, we are expecting to see a high limit for the deployment, but a few cases are lower (invoked from within a call).
        if (expectHighEnergyLimit) {
            Blockchain.require(Blockchain.getEnergyLimit() > 2_000_000L);
        } else {
            Blockchain.require(Blockchain.getEnergyLimit() <= 2_000_000L);
        }
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            // Note that all these methods return a byte array so that recursive calls don't need special knowledge.
            if (methodName.equals("store")) {
                return ABIEncoder.encodeOneByteArray(store(decoder.decodeOneByteArray()));
            } else if (methodName.equals("call")) {
                return ABIEncoder.encodeOneByteArray(call());
            } else if (methodName.equals("callInline")) {
                return ABIEncoder.encodeOneByteArray(callInline(decoder.decodeOneByteArray()));
            } else if (methodName.equals("createInline")) {
                return ABIEncoder.encodeOneByteArray(createInline(decoder.decodeOneByteArray()));
            } else if (methodName.equals("identity")) {
                return ABIEncoder.encodeOneByteArray(identity(decoder.decodeOneByteArray()));
            } else if (methodName.equals("checkOrigin")) {
                return ABIEncoder.encodeOneByteArray(checkOrigin());
            } else if (methodName.equals("checkEnergyLimit")) {
                return ABIEncoder.encodeOneByteArray(checkEnergyLimit());
            } else {
                return new byte[0];
            }
        }
    }

    public static byte[] store(byte[] transaction) {
        MetaTransactionTarget.transaction = transaction;
        // We will return a 1-element array on successful store.
        return new byte[] { 42 };
    }

    public static byte[] call() {
        return doCall(MetaTransactionTarget.transaction);
    }

    public static byte[] callInline(byte[] transaction) {
        return doCall(transaction);
    }

    public static byte[] createInline(byte[] transaction) {
        return doCreate(transaction);
    }

    public static byte[] identity(byte[] data) {
        return data;
    }

    public static byte[] checkOrigin() {
        // We verify that the caller and origin are the same, then return the origin.
        Address origin = Blockchain.getOrigin();
        Address caller = Blockchain.getCaller();
        Blockchain.require(origin.equals(caller));
        return origin.toByteArray();
    }

    public static byte[] checkEnergyLimit() {
        // We just cheat this by returning the encoding of the long (so we don't need a new utility to decode it later).
        return ABIEncoder.encodeOneLong(Blockchain.getEnergyLimit());
    }


    private static byte[] doCall(byte[] transaction) {
        Result result = Blockchain.invokeTransaction(transaction, Blockchain.getEnergyLimit());
        byte[] response = null;
        if (result.isSuccess()) {
            byte[] data = result.getReturnData();
            response = (null != data)
                    ? new ABIDecoder(data).decodeOneByteArray()
                    : null;
        }
        return response;
    }

    private static byte[] doCreate(byte[] transaction) {
        Result result = Blockchain.invokeTransaction(transaction, Blockchain.getEnergyLimit());
        return (result.isSuccess())
                ? result.getReturnData()
                : null;
    }
}
