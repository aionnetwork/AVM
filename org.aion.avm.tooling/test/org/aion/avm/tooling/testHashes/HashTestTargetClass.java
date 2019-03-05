package org.aion.avm.tooling.testHashes;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class HashTestTargetClass {

    private static byte[] hashedVal = null;
    private static final int LENGTH_LIMIT = 256;

    public static boolean callBlake2b(byte[] input){
        if (input.length > LENGTH_LIMIT){
            return false;
        } else {
            try{
                hashedVal = BlockchainRuntime.blake2b(input);
                return true;
            } catch (Exception e){
                hashedVal = null;
                return false;
            }
        }
    }

    public static boolean callSha(byte[] input){
        if (input.length > LENGTH_LIMIT){
            return false;
        } else {
            try{
                hashedVal = BlockchainRuntime.sha256(input);
                return true;
            } catch (Exception e){
                hashedVal = null;
                return false;
            }
        }
    }

    public static boolean callKeccak(byte[] input){
        if (input.length > LENGTH_LIMIT){
            return false;
        } else {
            try{
                hashedVal = BlockchainRuntime.keccak256(input);
                return true;
            } catch (Exception e){
                hashedVal = null;
                return false;
            }
        }
    }

    public static byte[] getHashedVal() {
        return hashedVal;
    }

    private static HashTestTargetClass hashTestTarget;

    /**
     * Initialization code executed once at the Dapp deployment.
     */
    static {
        hashTestTarget = new HashTestTargetClass();
    }

    /**
     * Entry point at a transaction call.
     */
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(HashTestTargetClass.class, BlockchainRuntime.getData());
    }
}
