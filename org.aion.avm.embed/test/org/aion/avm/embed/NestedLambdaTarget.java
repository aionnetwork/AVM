package org.aion.avm.embed;

import avm.Blockchain;
import java.math.BigInteger;
import java.util.function.Function;
import org.aion.avm.tooling.abi.Callable;

public class NestedLambdaTarget {
    private static Function<String, String> function;

    @Callable
    public static void createLambda() {
        function = (s1) -> {
            Function<Integer, Integer> f2 = (i1) -> {
                Function<BigInteger, BigInteger> f3 = (bi1) -> {
                    return bi1.add(bi1);
                };
                f3.hashCode();
                f3.toString();
                return ++i1 + f3.apply(new BigInteger("2389652398")).intValue();
            };
            f2.hashCode();
            f2.toString();
            return s1 + f2.apply(5);
        };
    }

    @Callable
    public static void callLambda() {
        function.hashCode();
        function.toString();

        BigInteger bigInteger = new BigInteger("2389652398");
        int num = bigInteger.add(bigInteger).intValue();

        String expected = "sample" + (num + 6);
        String actual = function.apply("sample");

        Blockchain.require(expected.equals(actual));
    }
}
