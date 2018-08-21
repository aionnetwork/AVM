package org.aion.avm.core.collection;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionList;

import java.util.List;

public class AionListPerfContract {

    public static int SIZE = 5000;

    public static AionList<Integer> target;

    static{
        target = new AionList<Integer>();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(new AionListPerfContract(), BlockchainRuntime.getData());
    }

    public static void callInit(){
        target.clear();
        for (int i = 0; i < SIZE; i++){
            target.add(Integer.valueOf(i));
        }
    }

    public static void callAppend(){
        for (int i = 0; i < SIZE; i++) {
            target.add(Integer.valueOf(10));
        }
    }

    public static void callInsertHead(){
        for (int i = 0; i < SIZE; i++) {
            target.add(0, Integer.valueOf(10));
        }
    }

    public static void callInsertMiddle(){
        for (int i = 0; i < SIZE; i++) {
            target.add(SIZE / 2, Integer.valueOf(10));
        }
    }

}
