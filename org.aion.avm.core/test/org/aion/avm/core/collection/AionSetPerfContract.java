package org.aion.avm.core.collection;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionPlainSet;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.abi.ABIDecoder;

public class AionSetPerfContract {

    public static int SIZE = 5000;

    public static AionSet<Integer> target;

    public static AionPlainSet<Integer> targetB;

    static{
        target = new AionSet<>();
        targetB = new AionPlainSet<>();
    }

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("callInit")) {
                callInit();
                return new byte[0];
            } else if (methodName.equals("callAdd")) {
                callAdd();
                return new byte[0];
            } else if (methodName.equals("callContains")) {
                callContains();
                return new byte[0];
            } else if (methodName.equals("callRemove")) {
                callRemove();
                return new byte[0];
            } else if (methodName.equals("callInitB")) {
                callInitB();
                return new byte[0];
            } else if (methodName.equals("callAddB")) {
                callAddB();
                return new byte[0];
            } else if (methodName.equals("callContainsB")) {
                callContainsB();
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

    public static void callInitB(){
        for (int i = 0; i < SIZE; i++){
            targetB.add(Integer.valueOf(i));
        }
    }

    public static void callAddB(){
        for (int i = 0; i < SIZE; i++){
            targetB.add(Integer.valueOf(SIZE + 1));
        }
    }

    public static void callContainsB(){
        for (int i = 0; i < SIZE; i++){
            targetB.contains(Integer.valueOf(i));
        }
    }

    public static void callRemoveB(){
        for (int i = 0; i < SIZE; i++){
            targetB.remove(Integer.valueOf(i));
        }
    }
}
