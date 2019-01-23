package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class CryptoUtilMethodFeeBenchmarkTestTargetClass {

    private final byte[] MESSAGE = "test message".getBytes();
    private final byte[] SIGNATURE = "0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61".getBytes();
    private final byte[] PK = "8c11e9a4772bb651660a5a5e412be38d".getBytes();

    private CryptoUtilMethodFeeBenchmarkTestTargetClass(){
        // initialize to default
    }

    public void callBlake2b(int count, byte[] message){
        for (int i = 0; i < count; i++){
             BlockchainRuntime.blake2b(message);
        }
    }

    public void callSha(int count, byte[] message){
        for (int i = 0; i < count; i++){
            BlockchainRuntime.sha256(message);
        }
    }

    public void callKeccak(int count, byte[] message){
        for (int i = 0; i < count; i++){
            BlockchainRuntime.keccak256(message);
        }
    }

    public void callEdverify(int count, byte[] message) throws IllegalArgumentException{
        for (int i = 0; i < count; i++){
            BlockchainRuntime.edVerify(message, SIGNATURE, PK);
        }
    }


    private static org.aion.avm.core.CryptoUtilMethodFeeBenchmarkTestTargetClass testTarget;

    /**
     * Initialization code executed once at the Dapp deployment.
     */
    static {
        testTarget = new org.aion.avm.core.CryptoUtilMethodFeeBenchmarkTestTargetClass();
    }

    /**
     * Entry point at a transaction call.
     */
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(testTarget, BlockchainRuntime.getData());
    }
}
