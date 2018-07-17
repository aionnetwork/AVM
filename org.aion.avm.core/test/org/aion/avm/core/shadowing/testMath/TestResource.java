package org.aion.avm.core.shadowing.testMath;

import java.math.MathContext;


public class TestResource {
    public boolean testMaxMin(){
        boolean ret = true;

        ret = ret && (Math.max(1, 10) == 10);
        ret = ret && (Math.min(1, 10) == 1);

        return ret;
    }

    public static Object testMathContext() {
        return new MathContext(5);
    }
}
