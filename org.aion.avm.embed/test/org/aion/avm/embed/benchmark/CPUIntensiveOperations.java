package org.aion.avm.embed.benchmark;

import org.aion.avm.tooling.abi.Callable;

import avm.Blockchain;

import java.math.BigInteger;


/**
 * Tests basic CPU-intensive operations.
 */
public class CPUIntensiveOperations {
    @Callable
    public static void sqrt(int count, double a){
        for(int i =0 ; i< count; i++){
            StrictMath.sqrt(a);
        }
    }

    @Callable
    public static long fibonacciLong(int index) {
        // We use the iterative approach in order to measure the cost for different numbers of iterations.
        // Note that we assume a zero-index from 0 so index 6 would be value 8:
        // 0, 1, 1, 2, 3, 5, 8.
        long number = 0L;
        long next = 1L;
        for (int i = 0; i < index; ++i) {
            long previous = number;
            number = next;
            next = previous + number;
        }
        return number;
    }

    @Callable
    public static byte[] fibonacciBigInteger(int index) {
        // We use the iterative approach in order to measure the cost for different numbers of iterations.
        // Note that we assume a zero-index from 0 so index 6 would be value 8:
        // 0, 1, 1, 2, 3, 5, 8.
        BigInteger number = BigInteger.ZERO;
        BigInteger next = BigInteger.ONE;
        for (int i = 0; i < index; ++i) {
            BigInteger previous = number;
            number = next;
            // Note that the BigInteger will fail on overflow, so we handle that and revert.
            try {
                next = previous.add(number);
            } catch (ArithmeticException e) {
                Blockchain.revert();
            }
        }
        return number.toByteArray();
    }
}
