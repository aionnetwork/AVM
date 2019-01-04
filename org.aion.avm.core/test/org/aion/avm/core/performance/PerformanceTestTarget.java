package org.aion.avm.core.performance;

import java.lang.Math;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class PerformanceTestTarget {
    private static final int heavyLevel;
    private static final int allocSize;

    static {
        int[] args = (int[]) ABIDecoder.decodeOneObject(BlockchainRuntime.getData());
        heavyLevel = args[0];
        allocSize = args[1];
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(PerformanceTestTarget.class, BlockchainRuntime.getData());
    }

    public static void cpuHeavy() {
        double x = 0;
        for(int k = 0; k < heavyLevel*1000; ++k) {
            x = Math.pow(2.0, 5.4);
        }
    }

    public static void memoryHeavy() {
        for(int i = 0; i < heavyLevel; ++i) {
            byte[] source = new byte[allocSize];
            byte[] target = new byte[allocSize];
            System.arraycopy(source, 0, target, 0, source.length);
        }
    }
}
