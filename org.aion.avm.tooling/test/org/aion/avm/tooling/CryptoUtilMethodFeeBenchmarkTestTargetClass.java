package org.aion.avm.tooling;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

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
            } else {
                return new byte[0];
            }
        }
    }
}
