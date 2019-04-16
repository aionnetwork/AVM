package org.aion.avm.tooling;

import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class ShadowClassConstantsTarget {
    private static BigInteger bigZero = BigInteger.ZERO;
    private static BigInteger bigOne = BigInteger.ONE;
    private static BigInteger bigTwo = BigInteger.TWO;
    private static BigInteger bigTen = BigInteger.TEN;
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
    private static Class<Void> voidClass = Void.TYPE;

    private static TimeUnit days = TimeUnit.DAYS;
    private static TimeUnit hours = TimeUnit.HOURS;
    private static TimeUnit minutes = TimeUnit.MINUTES;
    private static TimeUnit seconds = TimeUnit.SECONDS;
    private static TimeUnit milliseconds = TimeUnit.MILLISECONDS;
    private static TimeUnit microseconds = TimeUnit.MICROSECONDS;
    private static TimeUnit nanoseconds = TimeUnit.NANOSECONDS;

    @Callable
    public static void checkBigIntegerConstants() {
        Blockchain.require(bigZero == BigInteger.ZERO);
        Blockchain.require(bigZero.equals(BigInteger.ZERO));
        Blockchain.require(bigZero != BigInteger.valueOf(0));
        Blockchain.require(bigZero.equals(BigInteger.valueOf(0)));

        Blockchain.require(bigOne == BigInteger.ONE);
        Blockchain.require(bigOne.equals(BigInteger.ONE));
        Blockchain.require(bigOne != BigInteger.valueOf(1));
        Blockchain.require(bigOne.equals(BigInteger.valueOf(1)));

        Blockchain.require(bigTwo == BigInteger.TWO);
        Blockchain.require(bigTwo.equals(BigInteger.TWO));
        Blockchain.require(bigTwo != BigInteger.valueOf(2));
        Blockchain.require(bigTwo.equals(BigInteger.valueOf(2)));

        Blockchain.require(bigTen == BigInteger.TEN);
        Blockchain.require(bigTen.equals(BigInteger.TEN));
        Blockchain.require(bigTen != BigInteger.valueOf(10));
        Blockchain.require(bigTen.equals(BigInteger.valueOf(10)));
    }

    @Callable
    public static void checkRoundingModeConstants() {
        Blockchain.require(roundUp == RoundingMode.UP);
        Blockchain.require(roundDown == RoundingMode.DOWN);
        Blockchain.require(roundHalfUp == RoundingMode.HALF_UP);
        Blockchain.require(roundHalfDown == RoundingMode.HALF_DOWN);
        Blockchain.require(roundHalfEven== RoundingMode.HALF_EVEN);
        Blockchain.require(roundFloor == RoundingMode.FLOOR);
        Blockchain.require(roundCeiling == RoundingMode.CEILING);
        Blockchain.require(roundUnnecessary == RoundingMode.UNNECESSARY);
    }

    @Callable
    public static void checkMathContextConstants() {
        Blockchain.require(unlimitedMath == MathContext.UNLIMITED);
        Blockchain.require(unlimitedMath.equals(new MathContext(0, RoundingMode.HALF_UP)));

        Blockchain.require(decimal32Math == MathContext.DECIMAL32);
        Blockchain.require(decimal32Math.equals(new MathContext(7, RoundingMode.HALF_EVEN)));

        Blockchain.require(decimal64Math == MathContext.DECIMAL64);
        Blockchain.require(decimal64Math.equals(new MathContext(16, RoundingMode.HALF_EVEN)));

        Blockchain.require(decimal128Math == MathContext.DECIMAL128);
        Blockchain.require(decimal128Math.equals(new MathContext(34, RoundingMode.HALF_EVEN)));
    }

    @Callable
    public static void checkBooleanConstants() {
        Blockchain.require(trueBool == Boolean.TRUE);
        Blockchain.require(trueBool.equals(Boolean.TRUE));
        Blockchain.require(trueBool.equals(true));

        Blockchain.require(falseBool == Boolean.FALSE);
        Blockchain.require(falseBool.equals(Boolean.FALSE));
        Blockchain.require(falseBool.equals(false));

        // These are surprisingly true in Java; verifying our consistency.
        Blockchain.require(trueBool == Boolean.valueOf(true));
        Blockchain.require(falseBool == Boolean.valueOf(false));
    }

    @Callable
    public static void checkPrimitiveTypeConstants() {
        // These are all consistent with Java identities.
        Blockchain.require(boolClass != Boolean.class);
        Blockchain.require(boolClass == boolean.class);

        Blockchain.require(byteClass != Byte.class);
        Blockchain.require(byteClass == byte.class);

        Blockchain.require(charClass != Character.class);
        Blockchain.require(charClass == char.class);

        Blockchain.require(shortClass != Short.class);
        Blockchain.require(shortClass == short.class);

        Blockchain.require(intClass != Integer.class);
        Blockchain.require(intClass == int.class);

        Blockchain.require(longClass != Long.class);
        Blockchain.require(longClass == long.class);

        Blockchain.require(doubleClass != Double.class);
        Blockchain.require(doubleClass == double.class);

        Blockchain.require(floatClass != Float.class);
        Blockchain.require(floatClass == float.class);

        Blockchain.require(voidClass != Void.class);
        Blockchain.require(voidClass == void.class);
    }

    @Callable
    public static void checkTimeUnitConstants() {
        Blockchain.require(days == TimeUnit.DAYS);
        Blockchain.require(days.equals(TimeUnit.DAYS));
        Blockchain.require(days.equals(TimeUnit.valueOf("DAYS")));

        Blockchain.require(hours == TimeUnit.HOURS);
        Blockchain.require(hours.equals(TimeUnit.HOURS));
        Blockchain.require(hours.equals(TimeUnit.valueOf("HOURS")));

        Blockchain.require(minutes == TimeUnit.MINUTES);
        Blockchain.require(minutes.equals(TimeUnit.MINUTES));
        Blockchain.require(minutes.equals(TimeUnit.valueOf("MINUTES")));

        Blockchain.require(seconds == TimeUnit.SECONDS);
        Blockchain.require(seconds.equals(TimeUnit.SECONDS));
        Blockchain.require(seconds.equals(TimeUnit.valueOf("SECONDS")));

        Blockchain.require(milliseconds == TimeUnit.MILLISECONDS);
        Blockchain.require(milliseconds.equals(TimeUnit.MILLISECONDS));
        Blockchain.require(milliseconds.equals(TimeUnit.valueOf("MILLISECONDS")));

        Blockchain.require(microseconds == TimeUnit.MICROSECONDS);
        Blockchain.require(microseconds.equals(TimeUnit.MICROSECONDS));
        Blockchain.require(microseconds.equals(TimeUnit.valueOf("MICROSECONDS")));

        Blockchain.require(nanoseconds == TimeUnit.NANOSECONDS);
        Blockchain.require(nanoseconds.equals(TimeUnit.NANOSECONDS));
        Blockchain.require(nanoseconds.equals(TimeUnit.valueOf("NANOSECONDS")));

    }

}