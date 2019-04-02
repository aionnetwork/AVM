package org.aion.avm.tooling.shadowing.testMath;

import java.math.MathContext;
import java.math.RoundingMode;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;

public class TestResource {

    @Callable
    public static boolean testMaxMin(){
        boolean ret = true;

        ret = ret && (Math.max(1, 10) == 10);
        ret = ret && (Math.min(1, 10) == 1);

        return ret;
    }

    @Callable
    public static int testMathContext() {
        return new MathContext(5).getPrecision();
    }

    @Callable
    public static void getRoundingMode(){
        MathContext mc1, mc2;

        mc1 = new MathContext(4);
        mc2 = new MathContext(5, RoundingMode.CEILING);

        BlockchainRuntime.require(mc1.getRoundingMode().equals(RoundingMode.HALF_UP));
        BlockchainRuntime.require(mc2.getRoundingMode().equals(RoundingMode.CEILING));
    }
}
