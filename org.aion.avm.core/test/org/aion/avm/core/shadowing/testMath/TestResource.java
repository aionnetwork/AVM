package org.aion.avm.core.shadowing.testMath;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

import java.math.MathContext;

public class TestResource {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(TestResource.class, BlockchainRuntime.getData());
    }

    public static boolean testMaxMin(){
        boolean ret = true;

        ret = ret && (Math.max(1, 10) == 10);
        ret = ret && (Math.min(1, 10) == 1);

        return ret;
    }

    public static int testMathContext() {
        return new MathContext(5).getPrecision();
    }
}
