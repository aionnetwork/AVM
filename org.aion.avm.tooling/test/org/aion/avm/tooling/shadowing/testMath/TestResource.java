package org.aion.avm.tooling.shadowing.testMath;

import java.math.MathContext;
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
}
