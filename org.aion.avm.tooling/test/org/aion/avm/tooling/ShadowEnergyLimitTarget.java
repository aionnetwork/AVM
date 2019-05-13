package org.aion.avm.tooling;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

import java.math.BigInteger;
import java.util.Arrays;

public class ShadowEnergyLimitTarget {

    @Callable
    public static void callEdverifyMaxLengthZero() {
        byte[] n = new byte[600000];
        byte[] signature = "0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61".getBytes();
        byte[] pk = "8c11e9a4772bb651660a5a5e412be38d".getBytes();
        Blockchain.edVerify(n, signature, pk);
    }

    @Callable
    public static void callEdverifyMaxLengthOne() {
        byte[] n = new byte[460000];
        Arrays.fill(n, 0, 460000, (byte) 1);
        byte[] signature = "0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61".getBytes();
        byte[] pk = "8c11e9a4772bb651660a5a5e412be38d".getBytes();
        Blockchain.edVerify(n, signature, pk);
    }

    @Callable
    public static void callEdverifyLoop(int count) throws IllegalArgumentException{
        byte[] signature = "0367f714504761427cbc4abd5e4af97bbaa88553a7fa0076dc2fefdd200eca61".getBytes();
        byte[] pk = "8c11e9a4772bb651660a5a5e412be38d".getBytes();
        for (int i = 0; i < count; i++){
            Blockchain.edVerify(signature, signature, pk);
        }
    }

    @Callable
    public static void callBigIntegerSqrt(int count) throws IllegalArgumentException{
        byte[] arr1 = new byte[32];
        Arrays.fill(arr1, 0, 32, Byte.MAX_VALUE);
        BigInteger testValue = new BigInteger(arr1);
        for (int i = 0; i < count; i++){
            testValue.sqrt();
        }
    }

    @Callable
    public static void callBigIntegerToString(int count) throws IllegalArgumentException{
        byte[] arr1 = new byte[32];
        Arrays.fill(arr1, 0, 32, Byte.MAX_VALUE);
        BigInteger testValue = new BigInteger(arr1);
        for (int i = 0; i < count; i++){
            testValue.toString();
        }
    }

    @Callable
    public static int forceOutOfMemory(int count) {
        String test = "A";
        // We use both the invokedynamic and manual approach to show that these both fail correctly.
        if (0 == (count % 2)) {
            for (int i = 0; i < count; ++i) {
                test += test;
            }
        } else {
            for (int i = 0; i < count; ++i) {
                test = new StringBuilder(test).append((Object)test).toString();
            }
        }
        return test.length();
    }
}
