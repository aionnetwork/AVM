package org.aion.avm.tooling;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

import java.util.Arrays;

public class CryptoUtilMethodFeeBenchmarkTestTargetClass {

    private static final byte[] SIGNATURE = "0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61".getBytes();
    private static final byte[] PK = "8c11e9a4772bb651660a5a5e412be38d".getBytes();

    private CryptoUtilMethodFeeBenchmarkTestTargetClass(){
        // initialize to default
    }

    public static void callBlake2b(int count, byte[] message){
        for (int i = 0; i < count; i++){
             Blockchain.blake2b(message);
        }
    }

    public static void callSha(int count, byte[] message){
        for (int i = 0; i < count; i++){
            Blockchain.sha256(message);
        }
    }

    public static void callKeccak(int count, byte[] message){
        for (int i = 0; i < count; i++){
            Blockchain.keccak256(message);
        }
    }

    public static void callEdverify(int count, byte[] message) throws IllegalArgumentException{
        for (int i = 0; i < count; i++){
            Blockchain.edVerify(message, SIGNATURE, PK);
        }
    }

    public static void blake2bLargeInput(int loopCount, int size) {
        byte[] message = new byte[size];
        Arrays.fill(message, 0, message.length, Byte.MAX_VALUE);
        for (int i = 0; i < loopCount; i++) {
            Blockchain.blake2b(message);
        }
    }

    public static void shaLargeInput(int loopCount, int size) {
        byte[] message = new byte[size];
        Arrays.fill(message, 0, message.length, Byte.MAX_VALUE);
        for (int i = 0; i < loopCount; i++){
        Blockchain.sha256(message);
        }
    }

    public static void keccakLargeInput(int loopCount, int size) {
        byte[] message = new byte[size];
        Arrays.fill(message, 0, message.length, Byte.MAX_VALUE);
        for (int i = 0; i < loopCount; i++){
        Blockchain.keccak256(message);
        }
    }

    /**
     * Entry point at a transaction call.
     */
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("callBlake2b")) {
                callBlake2b(decoder.decodeOneInteger(), decoder.decodeOneByteArray());
                return new byte[0];
            } else if (methodName.equals("callSha")) {
                callSha(decoder.decodeOneInteger(), decoder.decodeOneByteArray());
                return new byte[0];
            } else if (methodName.equals("callKeccak")) {
                callKeccak(decoder.decodeOneInteger(), decoder.decodeOneByteArray());
                return new byte[0];
            } else if (methodName.equals("callEdverify")) {
                callEdverify(decoder.decodeOneInteger(), decoder.decodeOneByteArray());
                return new byte[0];
            } else if (methodName.equals("blake2bLargeInput")) {
                blake2bLargeInput(decoder.decodeOneInteger(), decoder.decodeOneInteger());
                return new byte[0];
            } else if (methodName.equals("shaLargeInput")) {
                shaLargeInput(decoder.decodeOneInteger(), decoder.decodeOneInteger());
                return new byte[0];
            } else if (methodName.equals("keccakLargeInput")) {
                keccakLargeInput(decoder.decodeOneInteger(), decoder.decodeOneInteger());
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }
}
