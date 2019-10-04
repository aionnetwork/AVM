package org.aion.avm.core.miscvisitors;


/**
 * AKI-418: No non-strict behaviour seems to be observable in the modern JVM and supported hardware.
 * Still, this test-case allows a demonstration of our strict application and some tests which verify
 * our floating-point assumptions (could be useful when deploying on different hardware).
 */
public class StrictFPVisitorTestResource {
    public static double v = Double.MAX_VALUE;

    public static double maxCalc(double arg) {
        return (v * 1.1) / 1.1 + arg;
    }

    strictfp public static double maxCalcStrict(double arg) {
        return (v * 1.1) / 1.1 + arg;
    }

    public static double maxMultiply(double arg) {
        float f = Float.MAX_VALUE;
        float g = Float.MAX_VALUE;
        return f * g + arg;
    }

    strictfp public static double maxMultiplyStrict(double arg) {
        float f = Float.MAX_VALUE;
        float g = Float.MAX_VALUE;
        return f * g + arg;
    }

    public static double zeroMultiply(double arg) {
        float f = Float.MIN_VALUE;
        float g = Float.MIN_VALUE;
        return f * g + arg;
    }

    strictfp public static double zeroMultiplyStrict(double arg) {
        float f = Float.MIN_VALUE;
        float g = Float.MIN_VALUE;
        return f * g + arg;
    }

    public static void testNaNLimits() {
        // Create various representations of NaN (different bit patterns which all map to the same logical concept).
        double nan1 = Double.NaN;
        double nan2 = Double.longBitsToDouble(0x7ff8000000000000L);
        double nan3 = Double.longBitsToDouble(0x7ff4000000000000L);
        
        // Convert these back into bits and verify that they have all been mapped to the same pattern.
        long s1 = Double.doubleToLongBits(nan1);
        long s2 = Double.doubleToLongBits(nan2);
        long s3 = Double.doubleToLongBits(nan3);
        if (s1 != s2) {
            throw new RuntimeException();
        }
        if (s2 != s3) {
            throw new RuntimeException();
        }
        if (s3 != s1) {
            throw new RuntimeException();
        }
        
        // Verify that NaN is not even equal to itself (so it won't matter which variant it used, internally).
        if (nan1 == nan1) {
            throw new RuntimeException();
        }
    }

    public static void testMantissaScale(long lLarge, long lSmall) {
        double dLarge = (double)lLarge;
        double dSmall = (double)lSmall;
        
        // lLarge + lSmall, alone, is a precise double, but it is the limit of this, so make some modifications to force it over this limit.
        long lLimit = (lLarge + lSmall);
        double dLimit = (dLarge + dSmall);
        
        long lTotal = (lLarge + lSmall) / 2 + lLarge;
        double dTotal = (dLarge + dSmall) / 2.0d + dLarge;
        
        if (lLimit != ((long) dLimit)) {
            // 52 bits didn't fit into the mantissa, which must succeed.
            throw new RuntimeException();
        }
        if (lTotal != (1 + (long)dTotal)) {
            // We were expecting the total to be off by one due to missing precision.
            throw new RuntimeException();
        }
    }

    strictfp public static void testMantissaScaleStrict(long lLarge, long lSmall) {
        double dLarge = (double)lLarge;
        double dSmall = (double)lSmall;
        
        // lLarge + lSmall, alone, is a precise double, but it is the limit of this, so make some modifications to force it over this limit.
        long lLimit = (lLarge + lSmall);
        double dLimit = (dLarge + dSmall);
        
        long lTotal = (lLarge + lSmall) / 2 + lLarge;
        double dTotal = (dLarge + dSmall) / 2.0d + dLarge;
        
        if (lLimit != ((long) dLimit)) {
            // 52 bits didn't fit into the mantissa, which must succeed.
            throw new RuntimeException();
        }
        if (lTotal != (1 + (long)dTotal)) {
            // We were expecting the total to be off by one due to missing precision.
            throw new RuntimeException();
        }
    }

    public static byte[] main() {
        for (int i = 0; i < 2_000_000_000; i += 200_000) {
            double arg = i;
            // Verify multiplications around max value.
            if (maxCalcStrict(arg) != maxCalc(arg)) {
                throw new RuntimeException();
            }
            
            // Verify overflow cases.
            double maxMultiply = maxMultiply(arg);
            if ((maxMultiplyStrict(arg) != maxMultiply) || (Double.POSITIVE_INFINITY != maxMultiply)) {
                throw new RuntimeException();
            }
            
            // Verify zero cases where the argument is dominating.
            double zeroMultiply = zeroMultiply(arg);
            if ((zeroMultiplyStrict(arg) != zeroMultiply) || (arg != zeroMultiply)) {
                throw new RuntimeException();
            }
        }
        testNaNLimits();
        
        // Create 2 numbers which can both be precisely expressed as doubles but whose sum would stretch the mantissa to the point where
        // it can't be expressed.
        // (note that these are passed in as args just to make it less likely some tooling will short-circuit the test).
        long lLarge = 1L << 53;
        long lSmall = 2L;
        testMantissaScale(lLarge, lSmall);
        testMantissaScaleStrict(lLarge, lSmall);
        return null;
    }
}
