package org.aion.avm.core.collection;

import avm.Blockchain;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.abi.ABIDecoder;


public class AionListPerfContract {

    public static int SIZE = 5000;

    public static AionList<Integer> target;

    static{
        target = new AionList<Integer>();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("callInit")) {
                callInit();
                return new byte[0];
            } else if (methodName.equals("callAppend")) {
                callAppend();
                return new byte[0];
            } else if (methodName.equals("callInsertHead")) {
                callInsertHead();
                return new byte[0];
            } else if (methodName.equals("callInsertMiddle")) {
                callInsertMiddle();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
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
