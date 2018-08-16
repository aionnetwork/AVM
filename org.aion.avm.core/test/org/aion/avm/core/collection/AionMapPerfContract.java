package org.aion.avm.core.collection;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;

import java.util.List;

public class AionMapPerfContract {

    public static int SIZE = 10000;

    public static AionMap<Integer, Integer> target;

    static{
        target = new AionMap<>();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRun(new AionMapPerfContract(), BlockchainRuntime.getData());
    }

    public static void callInit(){
        for (int i = 0; i < SIZE; i++){
            target.put(Integer.valueOf(i), Integer.valueOf(i));
        }
    }

    public static void callPut(){
        target.put(Integer.valueOf(SIZE + 1), Integer.valueOf(SIZE + 1));
    }

    public static void callGet(){
        target.get(Integer.valueOf(SIZE / 2));
    }
}
