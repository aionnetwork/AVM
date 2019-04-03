package org.aion.avm.core.performance;

import java.lang.StrictMath;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;

public class PerformanceTestTarget {
    private static final int heavyLevel;
    private static final int allocSize;

    static {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        int[] args = decoder.decodeOneIntegerArray();
        heavyLevel = args[0];
        allocSize = args[1];
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("memoryHeavy")) {
                memoryHeavy();
                return new byte[0];
            } else if (methodName.equals("cpuHeavy")) {
                cpuHeavy();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    public static void cpuHeavy() {
        double x = 0;
        for(int k = 0; k < heavyLevel*1000; ++k) {
            x = StrictMath.pow(2.0, 5.4);
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
