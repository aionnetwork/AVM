package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.BlockchainRuntime;

public class BigIntegerSerializationTarget {
    private static BigInteger bigNegative = BigInteger.valueOf(-1);
    private static BigInteger bigBigNegative = BigInteger.TEN.pow(10_000).negate();
    private static BigInteger bigNeutral = BigInteger.valueOf(0);
    private static BigInteger bigPositive = BigInteger.valueOf(1);
    private static BigInteger bigBigPositive = BigInteger.TEN.pow(10_000);

    public static byte[] main() {
        BlockchainRuntime.require(bigNegative.equals(BigInteger.valueOf(-1)));
        BlockchainRuntime.require(bigBigNegative.equals(BigInteger.valueOf(10).pow(10_000).multiply(BigInteger.valueOf(-1))));
        BlockchainRuntime.require(bigNeutral.equals(BigInteger.valueOf(0)));
        BlockchainRuntime.require(bigPositive.equals(BigInteger.valueOf(1)));
        BlockchainRuntime.require(bigBigPositive.equals(BigInteger.valueOf(10).pow(10_000)));

        return new byte[0];
    }

}
