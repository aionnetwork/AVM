package org.aion.avm.core;

import avm.Blockchain;
import java.math.BigDecimal;
import java.math.MathContext;
import org.aion.avm.userlib.abi.ABIDecoder;

public class BigDecimalConstructorTarget {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String method = decoder.decodeMethodName();

        // Hits all of the BigDecimal(String) constructors.
        if (method.equals("createBaseTenBigDecimal")) {
            BigDecimal decimal = createBaseTenBigDecimal();
            decimal.toBigInteger();
        } else if (method.equals("createPositivelySignedBaseTenBigDecimal")) {
            BigDecimal decimal = createPositivelySignedBaseTenBigDecimal();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelySignedBaseTenBigDecimal")) {
            BigDecimal decimal = createNegativelySignedBaseTenBigDecimal();
            decimal.toBigInteger();
        } else if (method.equals("createBigDecimalWithInvalidSignCharacters")) {
            createBigDecimalWithInvalidSignCharacters();
        } else if (method.equals("createBigDecimalFromFractionString")) {
            createBigDecimalFromFractionString();
        } else if (method.equals("createBigDecimalFromExponentString")) {
            createBigDecimalFromExponentString();
        } else if (method.equals("createBigDecimalFromGarbageCharacters")) {
            createBigDecimalFromGarbageCharacters();
        } else if (method.equals("createSmallBigDecimalLength77")) {
            BigDecimal decimal = createSmallBigDecimalLength77();
            decimal.toBigInteger();
        } else if (method.equals("createLargeBigDecimalLength77")) {
            BigDecimal decimal = createLargeBigDecimalLength77();
            decimal.toBigInteger();
        } else if (method.equals("createLargeBigDecimalLength76")) {
            BigDecimal decimal = createLargeBigDecimalLength76();
            decimal.toBigInteger();
        } else if (method.equals("createBigDecimalLength79")) {
            createBigDecimalLength79();
        } else if (method.equals("createPositivelySignedBigDecimalWith77Chars")) {
            BigDecimal decimal = createPositivelySignedBigDecimalWith77Chars();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelySignedBigDecimalWith77Chars")) {
            BigDecimal decimal = createNegativelySignedBigDecimalWith77Chars();
            decimal.toBigInteger();
        } else if (method.equals("createPositivelySignedLargeBigDecimalLength77")) {
            BigDecimal decimal = createPositivelySignedLargeBigDecimalLength77();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelySignedLargeBigDecimalLength77")) {
            BigDecimal decimal = createNegativelySignedLargeBigDecimalLength77();
            decimal.toBigInteger();
        } else if (method.equals("createPositivelySignedLargeBigDecimalLength76")) {
            BigDecimal decimal = createPositivelySignedLargeBigDecimalLength76();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelySignedLargeBigDecimalLength76")) {
            BigDecimal decimal = createNegativelySignedLargeBigDecimalLength76();
            decimal.toBigInteger();
        } else if (method.equals("createPositivelySignedBigDecimalLength78")) {
            createPositivelySignedBigDecimalLength78();
        } else if (method.equals("createNegativelySignedBigDecimalLength78")) {
            createNegativelySignedBigDecimalLength78();
        }

        // We repeat the above cases but on the BigDecimal(String, MathContext) constructors.
        if (method.equals("createBaseTenBigDecimalWithMathContext")) {
            BigDecimal decimal = createBaseTenBigDecimalWithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createPositivelySignedBaseTenBigDecimalWithMathContext")) {
            BigDecimal decimal = createPositivelySignedBaseTenBigDecimalWithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelySignedBaseTenBigDecimalWithMathContext")) {
            BigDecimal decimal = createNegativelySignedBaseTenBigDecimalWithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createBigDecimalWithInvalidSignCharactersWithMathContext")) {
            createBigDecimalWithInvalidSignCharactersWithMathContext();
        } else if (method.equals("createBigDecimalFromFractionStringWithMathContext")) {
            createBigDecimalFromFractionStringWithMathContext();
        } else if (method.equals("createBigDecimalFromExponentStringWithMathContext")) {
            createBigDecimalFromExponentStringWithMathContext();
        } else if (method.equals("createBigDecimalFromGarbageCharactersWithMathContext")) {
            createBigDecimalFromGarbageCharactersWithMathContext();
        } else if (method.equals("createSmallBigDecimalLength77WithMathContext")) {
            BigDecimal decimal = createSmallBigDecimalLength77WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createLargeBigDecimalLength77WithMathContext")) {
            BigDecimal decimal = createLargeBigDecimalLength77WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createLargeBigDecimalLength76WithMathContext")) {
            BigDecimal decimal = createLargeBigDecimalLength76WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createBigDecimalLength79WithMathContext")) {
            createBigDecimalLength79WithMathContext();
        } else if (method.equals("createPositivelySignedBigDecimalLength78WithMathContext")) {
            createPositivelySignedBigDecimalLength78WithMathContext();
        } else if (method.equals("createNegativelySignedBigDecimalLength78WithMathContext")) {
            createNegativelySignedBigDecimalLength78WithMathContext();
        } else if (method.equals("createPositivelySignedLargeBigDecimalLength76WithMathContext")) {
            BigDecimal decimal = createPositivelySignedLargeBigDecimalLength76WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelyLargeBigDecimalLength76WithMathContext")) {
            BigDecimal decimal = createNegativelyLargeBigDecimalLength76WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createPositivelySignedLargeBigDecimalLength77WithMathContext")) {
            BigDecimal decimal = createPositivelySignedLargeBigDecimalLength77WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelySignedLargeBigDecimalLength77WithMathContext")) {
            BigDecimal decimal = createNegativelySignedLargeBigDecimalLength77WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createPositivelySignedSmallBigDecimalLength77WithMathContext")) {
            BigDecimal decimal = createPositivelySignedSmallBigDecimalLength77WithMathContext();
            decimal.toBigInteger();
        } else if (method.equals("createNegativelySignedSmallBigDecimalLength77WithMathContext")) {
            BigDecimal decimal = createNegativelySignedSmallBigDecimalLength77WithMathContext();
            decimal.toBigInteger();
        }

        return null;
    }

    // Methods below hit BigDecimal(String) constructor.

    public static BigDecimal createBaseTenBigDecimal() {
        return new BigDecimal("9876543210");
    }

    public static BigDecimal createPositivelySignedBaseTenBigDecimal() {
        return new BigDecimal("+9876543210");
    }

    public static BigDecimal createNegativelySignedBaseTenBigDecimal() {
        return new BigDecimal("-9876543210");
    }

    public static void createBigDecimalWithInvalidSignCharacters() {
        new BigDecimal("9+87654321-0");
    }

    public static void createBigDecimalFromFractionString() {
        new BigDecimal("98765.43210");
    }

    public static void createBigDecimalFromExponentString() {
        new BigDecimal("9876543E210");
    }

    public static void createBigDecimalFromGarbageCharacters() {
        new BigDecimal("sdrthn$#&#%$scon~`\\][");
    }

    public static BigDecimal createSmallBigDecimalLength77() {
        return new BigDecimal(makeString(77, '1'));
    }

    public static BigDecimal createPositivelySignedBigDecimalWith77Chars() {
        return new BigDecimal("+" + makeString(77, '1'));
    }

    public static BigDecimal createNegativelySignedBigDecimalWith77Chars() {
        return new BigDecimal("-" + makeString(77, '1'));
    }

    public static BigDecimal createLargeBigDecimalLength77() {
        return new BigDecimal(makeString(77, '9'));
    }

    public static BigDecimal createPositivelySignedLargeBigDecimalLength77() {
        return new BigDecimal("+" + makeString(77, '9'));
    }

    public static BigDecimal createNegativelySignedLargeBigDecimalLength77() {
        return new BigDecimal("-" + makeString(77, '9'));
    }

    public static BigDecimal createLargeBigDecimalLength76() {
        return new BigDecimal(makeString(76, '9'));
    }

    public static BigDecimal createPositivelySignedLargeBigDecimalLength76() {
        return new BigDecimal("+" + makeString(76, '9'));
    }

    public static BigDecimal createNegativelySignedLargeBigDecimalLength76() {
        return new BigDecimal("-" + makeString(76, '9'));
    }

    public static void createBigDecimalLength79() {
        new BigDecimal(makeString(79, '0'));
    }

    public static void createPositivelySignedBigDecimalLength78() {
        new BigDecimal("+" + makeString(79, '0'));
    }

    public static void createNegativelySignedBigDecimalLength78() {
        new BigDecimal("-" + makeString(79, '0'));
    }

    // Methods below hit BigDecimal(String, MathContext) constructor.

    public static BigDecimal createBaseTenBigDecimalWithMathContext() {
        return new BigDecimal("9876543210", MathContext.UNLIMITED);
    }

    public static BigDecimal createPositivelySignedBaseTenBigDecimalWithMathContext() {
        return new BigDecimal("+9876543210", MathContext.UNLIMITED);
    }

    public static BigDecimal createNegativelySignedBaseTenBigDecimalWithMathContext() {
        return new BigDecimal("-9876543210", MathContext.UNLIMITED);
    }

    public static void createBigDecimalWithInvalidSignCharactersWithMathContext() {
        new BigDecimal("9+87654321-0", MathContext.UNLIMITED);
    }

    public static void createBigDecimalFromFractionStringWithMathContext() {
        new BigDecimal("98765.43210", MathContext.UNLIMITED);
    }

    public static void createBigDecimalFromExponentStringWithMathContext() {
        new BigDecimal("9876543E210", MathContext.UNLIMITED);
    }

    public static void createBigDecimalFromGarbageCharactersWithMathContext() {
        new BigDecimal("sdrthn$#&#%$scon~`\\][", MathContext.UNLIMITED);
    }

    public static BigDecimal createSmallBigDecimalLength77WithMathContext() {
        return new BigDecimal(makeString(77, '1'), MathContext.UNLIMITED);
    }

    public static BigDecimal createPositivelySignedSmallBigDecimalLength77WithMathContext() {
        return new BigDecimal("+" + makeString(77, '1'), MathContext.UNLIMITED);
    }

    public static BigDecimal createNegativelySignedSmallBigDecimalLength77WithMathContext() {
        return new BigDecimal("-" + makeString(77, '1'), MathContext.UNLIMITED);
    }

    public static BigDecimal createLargeBigDecimalLength77WithMathContext() {
        return new BigDecimal(makeString(77, '9'), MathContext.UNLIMITED);
    }

    public static BigDecimal createPositivelySignedLargeBigDecimalLength77WithMathContext() {
        return new BigDecimal("+" + makeString(77, '9'), MathContext.UNLIMITED);
    }

    public static BigDecimal createNegativelySignedLargeBigDecimalLength77WithMathContext() {
        return new BigDecimal("-" + makeString(77, '9'), MathContext.UNLIMITED);
    }

    public static BigDecimal createLargeBigDecimalLength76WithMathContext() {
        return new BigDecimal(makeString(76, '9'), MathContext.UNLIMITED);
    }

    public static BigDecimal createPositivelySignedLargeBigDecimalLength76WithMathContext() {
        return new BigDecimal("+" + makeString(76, '9'), MathContext.UNLIMITED);
    }

    public static BigDecimal createNegativelyLargeBigDecimalLength76WithMathContext() {
        return new BigDecimal("-" + makeString(76, '9'), MathContext.UNLIMITED);
    }

    public static void createBigDecimalLength79WithMathContext() {
        new BigDecimal(makeString(79, '0'), MathContext.UNLIMITED);
    }

    public static void createPositivelySignedBigDecimalLength78WithMathContext() {
        new BigDecimal("+" + makeString(79, '0'), MathContext.UNLIMITED);
    }

    public static void createNegativelySignedBigDecimalLength78WithMathContext() {
        new BigDecimal("-" + makeString(79, '0'), MathContext.UNLIMITED);
    }

    private static String makeString(int size, char leadingDigit) {
        StringBuilder builder = new StringBuilder(String.valueOf(leadingDigit));
        for (int i = 0; i < (size - 1); i++) {
            builder.append("9");
        }
        return builder.toString();
    }
}
