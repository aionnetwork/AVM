package org.aion.avm.tooling;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class CryptoUtilMethodFeeBenchmarkTestTargetClass {

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

    /**
     * Entry point at a transaction call.
     */
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(CryptoUtilMethodFeeBenchmarkTestTargetClass.class, BlockchainRuntime.getData());
    }
}
