package org.aion.avm.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class ShadowClassConstantsTarget {
    private static BigInteger bigZero = BigInteger.ZERO;
    private static BigInteger bigOne = BigInteger.ONE;
    private static BigInteger bigTwo = BigInteger.TWO;
    private static BigInteger bigTen = BigInteger.TEN;
    private static BigDecimal bigZeroDecimal = BigDecimal.ZERO;
    private static BigDecimal bigOneDecimal = BigDecimal.ONE;
    private static BigDecimal bigTenDecimal = BigDecimal.TEN;
    private static RoundingMode roundUp = RoundingMode.UP;
    private static RoundingMode roundDown = RoundingMode.DOWN;
    private static RoundingMode roundFloor = RoundingMode.FLOOR;
    private static RoundingMode roundCeiling = RoundingMode.CEILING;
    private static RoundingMode roundHalfDown = RoundingMode.HALF_DOWN;
    private static RoundingMode roundHalfUp = RoundingMode.HALF_UP;
    private static RoundingMode roundHalfEven = RoundingMode.HALF_EVEN;
    private static RoundingMode roundUnnecessary = RoundingMode.UNNECESSARY;
    private static MathContext unlimitedMath = MathContext.UNLIMITED;
    private static MathContext decimal32Math = MathContext.DECIMAL32;
    private static MathContext decimal64Math = MathContext.DECIMAL64;
    private static MathContext decimal128Math = MathContext.DECIMAL128;
    private static Boolean trueBool = Boolean.TRUE;
    private static Boolean falseBool = Boolean.FALSE;

    private static Class<Boolean> boolClass = Boolean.TYPE;
    private static Class<Byte> byteClass = Byte.TYPE;
    private static Class<Character> charClass = Character.TYPE;
    private static Class<Short> shortClass = Short.TYPE;
    private static Class<Integer> intClass = Integer.TYPE;
    private static Class<Long> longClass = Long.TYPE;
    private static Class<Double> doubleClass = Double.TYPE;
    private static Class<Float> floatClass = Float.TYPE;

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(ShadowClassConstantsTarget.class, BlockchainRuntime.getData());
    }

    public static void checkBigIntegerConstants() {
        BlockchainRuntime.require(bigZero == BigInteger.ZERO);
        BlockchainRuntime.require(bigZero.equals(BigInteger.ZERO));
        BlockchainRuntime.require(bigZero != BigInteger.valueOf(0));
        BlockchainRuntime.require(bigZero.equals(BigInteger.valueOf(0)));

        BlockchainRuntime.require(bigOne == BigInteger.ONE);
        BlockchainRuntime.require(bigOne.equals(BigInteger.ONE));
        BlockchainRuntime.require(bigOne != BigInteger.valueOf(1));
        BlockchainRuntime.require(bigOne.equals(BigInteger.valueOf(1)));

        BlockchainRuntime.require(bigTwo == BigInteger.TWO);
        BlockchainRuntime.require(bigTwo.equals(BigInteger.TWO));
        BlockchainRuntime.require(bigTwo != BigInteger.valueOf(2));
        BlockchainRuntime.require(bigTwo.equals(BigInteger.valueOf(2)));

        BlockchainRuntime.require(bigTen == BigInteger.TEN);
        BlockchainRuntime.require(bigTen.equals(BigInteger.TEN));
        BlockchainRuntime.require(bigTen != BigInteger.valueOf(10));
        BlockchainRuntime.require(bigTen.equals(BigInteger.valueOf(10)));
    }

    public static void checkBigDecimalConstants() {
        BlockchainRuntime.require(bigZeroDecimal == BigDecimal.ZERO);
        BlockchainRuntime.require(bigZeroDecimal.equals(BigDecimal.ZERO));
        BlockchainRuntime.require(bigZeroDecimal != BigDecimal.valueOf(0));
        BlockchainRuntime.require(bigZeroDecimal.equals(BigDecimal.valueOf(0)));

        BlockchainRuntime.require(bigOneDecimal == BigDecimal.ONE);
        BlockchainRuntime.require(bigOneDecimal.equals(BigDecimal.ONE));
        BlockchainRuntime.require(bigOneDecimal != BigDecimal.valueOf(1));
        BlockchainRuntime.require(bigOneDecimal.equals(BigDecimal.valueOf(1)));

        BlockchainRuntime.require(bigTenDecimal == BigDecimal.TEN);
        BlockchainRuntime.require(bigTenDecimal.equals(BigDecimal.TEN));
        BlockchainRuntime.require(bigTenDecimal != BigDecimal.valueOf(10));
        BlockchainRuntime.require(bigTenDecimal.equals(BigDecimal.valueOf(10)));
    }

    public static void checkRoundingModeConstants() {
        BlockchainRuntime.require(roundUp == RoundingMode.UP);
        BlockchainRuntime.require(roundDown == RoundingMode.DOWN);
        BlockchainRuntime.require(roundHalfUp == RoundingMode.HALF_UP);
        BlockchainRuntime.require(roundHalfDown == RoundingMode.HALF_DOWN);
        BlockchainRuntime.require(roundHalfEven== RoundingMode.HALF_EVEN);
        BlockchainRuntime.require(roundFloor == RoundingMode.FLOOR);
        BlockchainRuntime.require(roundCeiling == RoundingMode.CEILING);
        BlockchainRuntime.require(roundUnnecessary == RoundingMode.UNNECESSARY);
    }

    public static void checkMathContextConstants() {
        BlockchainRuntime.require(unlimitedMath == MathContext.UNLIMITED);
        BlockchainRuntime.require(unlimitedMath.equals(new MathContext(0, RoundingMode.HALF_UP)));

        BlockchainRuntime.require(decimal32Math == MathContext.DECIMAL32);
        BlockchainRuntime.require(decimal32Math.equals(new MathContext(7, RoundingMode.HALF_EVEN)));

        BlockchainRuntime.require(decimal64Math == MathContext.DECIMAL64);
        BlockchainRuntime.require(decimal64Math.equals(new MathContext(16, RoundingMode.HALF_EVEN)));

        BlockchainRuntime.require(decimal128Math == MathContext.DECIMAL128);
        BlockchainRuntime.require(decimal128Math.equals(new MathContext(34, RoundingMode.HALF_EVEN)));
    }

    public static void checkBooleanConstants() {
        BlockchainRuntime.require(trueBool == Boolean.TRUE);
        BlockchainRuntime.require(trueBool.equals(Boolean.TRUE));
        BlockchainRuntime.require(trueBool.equals(true));

        BlockchainRuntime.require(falseBool == Boolean.FALSE);
        BlockchainRuntime.require(falseBool.equals(Boolean.FALSE));
        BlockchainRuntime.require(falseBool.equals(false));

        // These are surprisingly true in Java; verifying our consistency.
        BlockchainRuntime.require(trueBool == Boolean.valueOf(true));
        BlockchainRuntime.require(falseBool == Boolean.valueOf(false));
    }

    public static void checkPrimitiveTypeConstants() {
        // These are all consistent with Java identities.
        BlockchainRuntime.require(boolClass != Boolean.class);
        BlockchainRuntime.require(boolClass == boolean.class);

        BlockchainRuntime.require(byteClass != Byte.class);
        BlockchainRuntime.require(byteClass == byte.class);

        BlockchainRuntime.require(charClass != Character.class);
        BlockchainRuntime.require(charClass == char.class);

        BlockchainRuntime.require(shortClass != Short.class);
        BlockchainRuntime.require(shortClass == short.class);

        BlockchainRuntime.require(intClass != Integer.class);
        BlockchainRuntime.require(intClass == int.class);

        BlockchainRuntime.require(longClass != Long.class);
        BlockchainRuntime.require(longClass == long.class);

        BlockchainRuntime.require(doubleClass != Double.class);
        BlockchainRuntime.require(doubleClass == double.class);

        BlockchainRuntime.require(floatClass != Float.class);
        BlockchainRuntime.require(floatClass == float.class);
    }

}
