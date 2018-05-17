package org.aion.avm.internal;

import org.aion.avm.rt.BlockchainRuntime;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLong;

public class Helper {

    private static ThreadLocal<BlockchainRuntime> blockchainRuntime = new ThreadLocal<>();
    private static ThreadLocal<AtomicLong> energyLeft = new ThreadLocal<>();

    public static void setBlockchainRuntime(BlockchainRuntime rt) {
        blockchainRuntime.set(rt);
        energyLeft.set(new AtomicLong(rt.getEnergyLimit()));
    }

    public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
        return new org.aion.avm.java.lang.Class<T>(input);
    }

    public static org.aion.avm.java.lang.String wrapAsString(String input) {
        return new org.aion.avm.java.lang.String(input);
    }

    public static void chargeEnergy(long cost) throws OutOfEnergyError {
        if (energyLeft.get().addAndGet(-cost) < 0) {
            throw new OutOfEnergyError();
        }
    }

    public static Object multianewarray1(int d1, Class<?> cl) {
        return Array.newInstance(cl, d1);
    }

    public static Object multianewarray2(int d1, int d2, Class<?> cl) {
        return Array.newInstance(cl, d1, d2);
    }

    public static Object multianewarray3(int d1, int d2, int d3, Class<?> cl) {
        return Array.newInstance(cl, d1, d2, d3);
    }
}
