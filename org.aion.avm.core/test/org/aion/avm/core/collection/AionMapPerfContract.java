package org.aion.avm.core.collection;

import avm.Blockchain;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionPlainMap;
import org.aion.avm.userlib.abi.ABIDecoder;

public class AionMapPerfContract {

    public static int SIZE = 500;

    public static AionMap<Integer, Integer> target;

    public static AionPlainMap<Integer, Integer> targetB;

    static{
        target = new AionMap<>();
        targetB = new AionPlainMap<>();
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
            } else if (methodName.equals("callPut")) {
                callPut();
                return new byte[0];
            } else if (methodName.equals("callGet")) {
                callGet();
                return new byte[0];
            } else if (methodName.equals("callRemove")) {
                callRemove();
                return new byte[0];
            } else if (methodName.equals("callInitB")) {
                callInitB();
                return new byte[0];
            } else if (methodName.equals("callPutB")) {
                callPutB();
                return new byte[0];
            } else if (methodName.equals("callGetB")) {
                callGetB();
                return new byte[0];
            } else if (methodName.equals("callRemoveB")) {
                callRemoveB();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    public static void callInit(){
        for (int i = 0; i < SIZE; i++){
            target.put(Integer.valueOf(i * 2), Integer.valueOf(i));
        }
    }

    public static void callPut(){
        for (int i = 0; i < SIZE; i++) {
            target.put(Integer.valueOf(i * 2 + 1), Integer.valueOf(i));
        }
    }

    public static void callGet(){
        for (int i = 0; i < SIZE; i++) {
            target.get(Integer.valueOf(i));
        }
    }

    public static void callRemove(){
        for (int i = 0; i < SIZE; i++) {
            target.remove(Integer.valueOf(i * 2));
        }
    }

    public static void callInitB(){
        for (int i = 0; i < SIZE; i++){
            targetB.put(Integer.valueOf(i * 2), Integer.valueOf(i));
        }
    }

    public static void callPutB(){
        for (int i = 0; i < SIZE; i++) {
            targetB.put(Integer.valueOf(i * 2 + 1), Integer.valueOf(i));
        }
    }

    public static void callGetB(){
        for (int i = 0; i < SIZE; i++) {
            targetB.get(Integer.valueOf(i));
        }
    }

    public static void callRemoveB(){
        for (int i = 0; i < SIZE; i++) {
            targetB.remove(Integer.valueOf(i * 2));
        }
    }
}
