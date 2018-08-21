package org.aion.avm.core.collection;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionSet;

import java.util.Set;

public class AionSetPerfContract {

    public static int SIZE = 5000;

    public static AionSet<Integer> target;

    static{
        target = new AionSet<>();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(new AionSetPerfContract(), BlockchainRuntime.getData());
    }

    public static void callInit(){
        for (int i = 0; i < SIZE; i++){
            target.add(Integer.valueOf(i));
        }
    }

    public static void callAdd(){
        for (int i = 0; i < SIZE; i++){
            target.add(Integer.valueOf(SIZE + 1));
        }
    }

    public static void callContains(){
        for (int i = 0; i < SIZE; i++){
            target.contains(Integer.valueOf(i));
        }
    }

    public static void callRemove(){
        for (int i = 0; i < SIZE; i++){
            target.remove(Integer.valueOf(i));
        }
    }


}
