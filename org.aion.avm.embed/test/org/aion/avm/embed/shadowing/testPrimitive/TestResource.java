package org.aion.avm.embed.shadowing.testPrimitive;

import org.aion.avm.tooling.abi.Callable;

public class TestResource {

    //========================================================
    // Test for java.lang.Boolean
    //========================================================
    static class BooleanTest {
        static class Factory {
            public static void main(String[] args) throws Exception {
                if (Boolean.valueOf(true) != Boolean.TRUE)
                    throw new Exception("Truth failure");
                if (Boolean.valueOf(false) != Boolean.FALSE)
                    throw new Exception("Major fallacy");
            }
        }

        static class ParseBoolean {
            public static void main(String[] args) throws Exception {
                checkTrue(Boolean.parseBoolean("TRUE"));
                checkTrue(Boolean.parseBoolean("true"));
                checkTrue(Boolean.parseBoolean("TrUe"));

                checkFalse(Boolean.parseBoolean("false"));
                checkFalse(Boolean.parseBoolean("FALSE"));
                checkFalse(Boolean.parseBoolean("FaLse"));
                checkFalse(Boolean.parseBoolean(null));
                checkFalse(Boolean.parseBoolean("garbage"));
                checkFalse(Boolean.parseBoolean("TRUEE"));
            }

            static void checkTrue(boolean b) {
                if (!b)
                    throw new RuntimeException("test failed");
            }

            static void checkFalse(boolean b) {
                if (b)
                    throw new RuntimeException("test failed");
            }
        }
    }

    @Callable
    public static boolean testBoolean() throws Exception{
        BooleanTest.Factory.main(null);
        BooleanTest.ParseBoolean.main(null);
        return true;
    }


    //========================================================
    // Test for java.lang.Byte
    //========================================================
    static class ByteTest {
        static class Decode {

            private static void check(String val, byte expected) {
                byte n = (Byte.decode(val)).byteValue();
                if (n != expected)
                    throw new RuntimeException("Byte.decode failed. String:" +
                            val + " Result:" + n);
            }

            private static void checkFailure(String val, String message) {
                try {
                    byte n = (Byte.decode(val)).byteValue();
                    throw new RuntimeException(message);
                } catch (NumberFormatException e) { /* Okay */}
            }

            public static void main(String[] args) throws Exception {
                check(new String(""+Byte.MIN_VALUE), Byte.MIN_VALUE);
                check(new String(""+Byte.MAX_VALUE), Byte.MAX_VALUE);

                check("10",   (byte)10);
                check("0x10", (byte)16);
                check("0X10", (byte)16);
                check("010",  (byte)8);
                check("#10",  (byte)16);

                check("+10",   (byte)10);
                check("+0x10", (byte)16);
                check("+0X10", (byte)16);
                check("+010",  (byte)8);
                check("+#10",  (byte)16);

                check("-10",   (byte)-10);
                check("-0x10", (byte)-16);
                check("-0X10", (byte)-16);
                check("-010",  (byte)-8);
                check("-#10",  (byte)-16);

                check(Integer.toString((int)Byte.MIN_VALUE), Byte.MIN_VALUE);
                check(Integer.toString((int)Byte.MAX_VALUE), Byte.MAX_VALUE);

                checkFailure("0x-10",   "Byte.decode allows negative sign in wrong position.");
                checkFailure("0x+10",   "Byte.decode allows positive sign in wrong position.");

                checkFailure("+",       "Raw plus sign allowed.");
                checkFailure("-",       "Raw minus sign allowed.");

                checkFailure(Integer.toString((int)Byte.MIN_VALUE - 1), "Out of range");
                checkFailure(Integer.toString((int)Byte.MAX_VALUE + 1), "Out of range");

                checkFailure("", "Empty String");
            }
        }
    }

    @Callable
    public static boolean testByte() throws Exception{
        ByteTest.Decode.main(null);
        return true;
    }

    //========================================================
    // Test for java.lang.Character
    //========================================================

    @Callable
    public static boolean testCharacter() throws Exception {
        CharacterTest.upperAndLowerCase();
        CharacterTest.spaces();
        CharacterTest.letterDigit();
        CharacterTest.numerics();
        CharacterTest.comparisons();
        CharacterTest.constants();
        CharacterTest.string();
        CharacterTest.hashcodeEquals();
        return true;
    }

    static class CharacterTest {
        private static void fail(char c, String problem) {
            throw new AssertionError(c + "': " + problem);
        }

        private static void fail(String problem) {
            throw new AssertionError(problem);
        }

        public static void upperAndLowerCase() throws Exception {
            char lc = 'c'; // lowercase
            char uc = 'C'; // uppercase
            char space = ' ';
            char diacritic = '\u00D9'; // Ù character
            char ideogram = '\u5317'; // 北 character

            if (Character.isUpperCase(lc)) fail(lc, "should not be upper case");
            if (! Character.isLowerCase(lc)) fail(lc, "should be lowercase");
            if (Character.toUpperCase(lc) != 'C') fail(lc, "toUpperCase should be 'C'");
            if (Character.toLowerCase(lc) != 'c') fail(lc, "toLowerCase should be 'c'");

            if (! Character.isUpperCase(uc)) fail(uc, "should be upper case");
            if (Character.isLowerCase(uc)) fail(uc, "should not be lowercase");
            if (Character.toUpperCase(uc) != 'C') fail(uc, "toUpperCase should be 'C'");
            if (Character.toLowerCase(uc) != 'c') fail(uc, "toLowerCase should be 'c'");

            if (Character.isUpperCase(space)) fail(space, "should not be upper case");
            if (Character.isLowerCase(space)) fail(space, "should not be lowercase");
            if (Character.toUpperCase(space) != ' ') fail(space, "toUpperCase should be ' '");
            if (Character.toLowerCase(space) != ' ') fail(space, "toLowerCase should be ' '");

            if (! Character.isUpperCase(diacritic)) fail(diacritic, "should be upper case");
            if (Character.isLowerCase(diacritic)) fail(diacritic, "should not be lowercase");
            if (Character.toUpperCase(diacritic) != 'Ù') fail(diacritic, "toUpperCase should be 'Ù'");
            if (Character.toLowerCase(diacritic) != 'ù') fail(diacritic, "toLowerCase should be 'ù'");

            if (Character.isUpperCase(ideogram)) fail(ideogram, "should not be upper case");
            if (Character.isLowerCase(ideogram)) fail(ideogram, "should not be lowercase");
            if (Character.toUpperCase(ideogram) != '北') fail(ideogram, "toUpperCase should be '北'");
            if (Character.toLowerCase(ideogram) != '北') fail(ideogram, "toLowerCase should be '北'");
        }

        public static void spaces() {
            char[] spaceAndWhitespace  = {' ', '\u2001' };
            char[] onlyWhitespace = { '\u0009', '\u000B', '\n', '\r', '\t', '\u001f' };
            char[] onlySpace = {'\u00a0', '\u2007' };
            char[] notAnySpace = {'a', '\u00D9', '\u5317', '1', '-', };

            for(char c: spaceAndWhitespace) {
                if(! Character.isWhitespace(c)) fail(c, "should be whitespace");
                if(! Character.isSpaceChar(c)) fail(c, "should be space char");
            }
            for(char c: onlyWhitespace) {
                if(! Character.isWhitespace(c)) fail(c, "should be whitespace");
                if(Character.isSpaceChar(c)) fail(c, "should not be space char");
            }
            for(char c: onlySpace) {
                if(Character.isWhitespace(c)) fail(c, "should not be whitespace");
                if(! Character.isSpaceChar(c)) fail(c, "should be space char");
            }
            for(char c: notAnySpace) {
                if(Character.isWhitespace(c)) fail(c, "should not be whitespace");
                if(Character.isSpaceChar(c)) fail(c, "should not be space char");
            }
        }

        public static void letterDigit() {
            char letter = 'c';
            char diacritic = '\u00D9'; // Ù character
            char dot = '.';
            char ideogram = '\u5317'; // 北 character
            char kana = 'カ';
            char digit = '0';

            if(Character.isDigit(letter)) fail(letter, "should not be digit");
            if(Character.isDigit(diacritic)) fail(diacritic, "should not be digit");
            if(Character.isDigit(dot)) fail(dot, "should not be digit");
            if(Character.isDigit(ideogram)) fail(ideogram, "should not be digit");
            if(Character.isDigit(kana)) fail(kana, "should not be digit");
            if(! Character.isDigit(digit)) fail(digit, "should be digit");

            if(! Character.isLetter(letter)) fail(letter, "should be letter");
            if(! Character.isLetter(diacritic)) fail(diacritic, "should be letter");
            if(Character.isLetter(dot)) fail(dot, "should not be letter");
            if(! Character.isLetter(ideogram)) fail(ideogram, "should be letter");
            if(! Character.isLetter(kana)) fail(kana, "should be letter");
            if(Character.isLetter(digit)) fail(digit, "should not be letter");

            if(! Character.isLetterOrDigit(letter)) fail(letter, "should be LetterOrDigit");
            if(! Character.isLetterOrDigit(diacritic)) fail(diacritic, "should be LetterOrDigit");
            if(Character.isLetterOrDigit(dot)) fail(dot, "should not be LetterOrDigit");
            if(! Character.isLetterOrDigit(ideogram)) fail(ideogram, "should be LetterOrDigit");
            if(! Character.isLetterOrDigit(kana)) fail(kana, "should be LetterOrDigit");
            if(! Character.isLetterOrDigit(digit)) fail(digit, "should not be LetterOrDigit");
        }

        public static void numerics() {
            // valid values
            char base36 = Character.forDigit(35, 36);
            char base2 = Character.forDigit(0, 2);
            // invalid values
            char base37 = Character.forDigit(36, 37);
            char base0 = Character.forDigit(0, 0);
            char tooBig = Character.forDigit(3, 2);
            char negative = Character.forDigit(-3, 10);

            if(base36 != 'z') fail("35_36 should be z");
            if(base2 != '0') fail("0_2 should be 0");
            if(base37 != '\u0000') fail("radix too large should be null char");
            if(base0 != '\u0000') fail("radix too small should be null char");
            if(tooBig != '\u0000') fail("number bigger than radix should be null char");
            if(negative != '\u0000') fail("negative number should be null char");

            int thirtyFive = Character.getNumericValue('z');
            int fraction = Character.getNumericValue('½');
            int notNumber = Character.getNumericValue('{');

            if(thirtyFive != 35) fail('z', "should have numeric value 36");
            if(fraction != -2) fail('½', "should have numeric value -2");
            if(notNumber != -1) fail('{', "should have numeric value -1");
        }

        public static void comparisons() {
            char small = 'a';
            char medium = 'z';
            char huge = '\u5317'; // 北 character

            if(Character.valueOf(small).compareTo(Character.valueOf(medium)) >= 0)
                fail(small, "should be smaller than " + medium);
            if(Character.valueOf(medium).compareTo(Character.valueOf(small)) <= 0)
                fail(medium, "should be larger than " + small);
            if(Character.valueOf(medium).compareTo(Character.valueOf(medium)) != 0)
                fail(medium, "should be equal to " + medium);

            if(Character.compare(small, medium) >= 0)
                fail(small, "should be smaller than " + medium);
            if(Character.compare(medium, small) <= 0)
                fail(medium, "should be larger than " + small);
            if(Character.compare(medium, medium) != 0)
                fail(medium, "should be equal to " + medium);

            if(Character.valueOf(medium).compareTo(Character.valueOf(huge)) >= 0)
                fail(medium, "should be smaller than " + huge);
            if(Character.valueOf(huge).compareTo(Character.valueOf(medium)) <= 0)
                fail(huge, "should be larger than " + medium);
            if(Character.valueOf(huge).compareTo(Character.valueOf(huge)) != 0)
                fail(huge, "should be equal to " + huge);

            if(Character.compare(small, huge) >= 0)
                fail(medium, "should be smaller than " + huge);
            if(Character.compare(huge, medium) <= 0)
                fail(huge, "should be larger than " + medium);
            if(Character.compare(huge, huge) != 0)
                fail(huge, "should be equal to " + huge);
        }

        public static void constants() {
            if(Character.SIZE != 16) fail("unexpected value for Character.SIZE");
            if(Character.BYTES != 2) fail("unexpected value for Character.BYTES");
            if(Character.MIN_RADIX != 2) fail("unexpected value for Character.MIN_RADIX");
            if(Character.MAX_RADIX != 36) fail("unexpected value for Character.MAX_RADIX");
            if(Character.MIN_VALUE != '\u0000') fail("unexpected value for Character.MIN_VALUE");
            if(Character.MAX_VALUE != '\uffff') fail("unexpected value for Character.MAX_VALUE");
        }

        public static void string() {
            // test 'a'
            if(! Character.toString('a').equals("a")) fail("Character.toString('a') should equal \"a\"");
            if(! Character.valueOf('a').toString().equals("a")) fail("'a'.toString should equal \"a\"");

            // test '北'
            if(! Character.valueOf('\u5317' ).toString().equals("北")) fail("'\\u5317'.toString should equal \"北\"");
            if(! Character.toString('\u5317').equals("北")) fail("Character.toString('\\u5317') should equal \"北\"");
        }

        public static void hashcodeEquals() {
            Character trivial = 'a';
            if(! trivial.equals(trivial)) fail("a Character object should equals itself");
            if(trivial.hashCode() != trivial.hashCode()) fail("a Character object should always return same hashCode");
            if(Character.hashCode(trivial.charValue()) != Character.hashCode(trivial.charValue())) {
                fail("a Character object should always return same hashCode");
            }

            Character lhs, rhs;

            lhs = Character.valueOf('c');
            rhs = Character.valueOf('C');
            if(lhs.equals(rhs)) fail("'c' should not equal 'C'");
            if(rhs.equals(lhs)) fail("'c' should not equal 'C'");
            if(lhs.equals('C')) fail("'c' should not equal 'C'");
            if(lhs.hashCode() == rhs.hashCode()) fail("'c' should not have same hashCode as 'C'");
            if(Character.hashCode(lhs.charValue()) == Character.hashCode(rhs.charValue())) {
                fail("'c' should not have same hashCode as 'C'");
            }

            lhs = Character.valueOf('c');
            rhs = Character.valueOf('c');
            if(! lhs.equals(rhs)) fail("'c' should equal 'c'");
            if(! rhs.equals(lhs)) fail("'c' should equal 'c'");
            if(! lhs.equals('c')) fail("'c' should equal 'c'");
            if(lhs.hashCode() != rhs.hashCode()) fail("'c' should have same hashCode as 'c'");
            if(Character.hashCode(lhs.charValue()) != Character.hashCode(rhs.charValue())) {
                fail("'c' should have same hashCode as 'c'");
            }

            lhs = Character.valueOf('c');
            rhs = Character.valueOf('\u5317');
            if(lhs.equals(rhs)) fail("'c' should not equal '北'");
            if(rhs.equals(lhs)) fail("'c' should not equal '北'");
            if(lhs.equals('北')) fail("'c' should not equal '北'");
            if(lhs.hashCode() == rhs.hashCode()) fail("'c' should not have same hashCode as '北'");
            if(Character.hashCode(lhs.charValue()) == Character.hashCode(rhs.charValue())) {
                fail("'c' should not have same hashCode as '北'");
            }

            lhs = Character.valueOf('\u5317');
            rhs = Character.valueOf('\u5317');
            if(! lhs.equals(rhs)) fail("'北' should equal '北'");
            if(! rhs.equals(lhs)) fail("'北' should equal '北'");
            if(! lhs.equals('北')) fail("'北' should equal '北'");
            if(lhs.hashCode() != rhs.hashCode()) fail("'北' should have same hashCode as '北'");
            if(Character.hashCode(lhs.charValue()) != Character.hashCode(rhs.charValue())) {
                fail("'北' should have same hashCode as '北'");
            }

        }
    }

    //========================================================
    // Test for java.lang.Double
    //========================================================

    static class DoubleTest {
        static class Constants {
            /*
             * This compile-only test is to make sure that the primitive
             * public static final fields in java.lang.Double are "constant
             * expressions" as defined by "The Java Language Specification,
             * 2nd edition" section 15.28; a different test checks the values
             * of those fields.
             */
            public static void main(String[] args) throws Exception {
                int i = 0;
                switch (i) {
                    case (int)Double.NaN:                   // 0
                        break;
                    case (int)Double.MIN_VALUE + 1:         // 0 + 1
                        break;
                    case (int)Double.MIN_NORMAL + 2:        // 0 + 2
                        break;
                    case Double.MIN_EXPONENT:               // -1022
                        break;
                    case Double.MAX_EXPONENT:               // 1023
                        break;
                    case (int)Double.MAX_VALUE - 1:         // Integer.MAX_VALUE - 1
                        break;
                    case (int)Double.POSITIVE_INFINITY:     // Integer.MAX_VALUE
                        break;
                    case (int)Double.NEGATIVE_INFINITY:     // Integer.MIN_VALUE
                        break;
                }
            }
        }

        static class Extrema {
            public static void main(String[] args) throws Exception {
                if (Double.MIN_VALUE != Double.longBitsToDouble(0x1L))
                    throw new RuntimeException("Double.MIN_VALUE is not equal "+
                            "to longBitsToDouble(0x1L).");

                if (Double.MIN_NORMAL != Double.longBitsToDouble(0x0010000000000000L))
                    throw new RuntimeException("Double.MIN_NORMAL is not equal "+
                            "to longBitsToDouble(0x0010000000000000L).");

                if (Double.MAX_VALUE != Double.longBitsToDouble(0x7fefffffffffffffL))
                    throw new RuntimeException("Double.MAX_VALUE is not equal "+
                            "to longBitsToDouble(0x7fefffffffffffffL).");
            }
        }

        static class NaNInfinityParsing {

            static String NaNStrings[] = {
                    "NaN",
                    "+NaN",
                    "-NaN"
            };

            static String infinityStrings[] = {
                    "Infinity",
                    "+Infinity",
                    "-Infinity",
            };

            static String invalidStrings[] = {
                    "+",
                    "-",
                    "@",
                    "N",
                    "Na",
                    "Nan",
                    "NaNf",
                    "NaNd",
                    "NaNF",
                    "NaND",
                    "+N",
                    "+Na",
                    "+Nan",
                    "+NaNf",
                    "+NaNd",
                    "+NaNF",
                    "+NaND",
                    "-N",
                    "-Na",
                    "-Nan",
                    "-NaNf",
                    "-NaNd",
                    "-NaNF",
                    "-NaND",
                    "I",
                    "In",
                    "Inf",
                    "Infi",
                    "Infin",
                    "Infini",
                    "Infinit",
                    "InfinitY",
                    "Infinityf",
                    "InfinityF",
                    "Infinityd",
                    "InfinityD",
                    "+I",
                    "+In",
                    "+Inf",
                    "+Infi",
                    "+Infin",
                    "+Infini",
                    "+Infinit",
                    "+InfinitY",
                    "+Infinityf",
                    "+InfinityF",
                    "+Infinityd",
                    "+InfinityD",
                    "-I",
                    "-In",
                    "-Inf",
                    "-Infi",
                    "-Infin",
                    "-Infini",
                    "-Infinit",
                    "-InfinitY",
                    "-Infinityf",
                    "-InfinityF",
                    "-Infinityd",
                    "-InfinityD",
                    "NaNInfinity",
                    "InfinityNaN",
                    "nan",
                    "infinity"
            };

            public static void main(String [] argv) throws Exception {
                int i;
                double d;

                // Test valid NaN strings
                for(i = 0; i < NaNStrings.length; i++) {
                    if(!Double.isNaN(d=Double.parseDouble(NaNStrings[i]))) {
                        throw new RuntimeException("NaN string ``" + NaNStrings[i]
                                + "'' did not parse as a NaN; returned " +
                                d + " instead.");
                    }
                }

                // Test valid Infinity strings
                for(i = 0; i < infinityStrings.length; i++) {
                    if(!Double.isInfinite(d=Double.parseDouble(infinityStrings[i]))) {
                        throw new RuntimeException("Infinity string ``" +
                                infinityStrings[i] +
                                "'' did not parse as infinity; returned " +
                                d + "instead.");
                    }
                    // check sign of result

                    boolean negative = (infinityStrings[i].charAt(0) == '-');
                    if(d != (negative?Double.NEGATIVE_INFINITY:
                            Double.POSITIVE_INFINITY))
                        throw new RuntimeException("Infinity has wrong sign;" +
                                (negative?"positive instead of negative.":
                                        "negative instead of positive."));
                }

                // Test almost valid strings
                for(i = 0; i < invalidStrings.length; i++) {
                    try {
                        double result;
                        d = Double.parseDouble(invalidStrings[i]);
                        throw new RuntimeException("Invalid string ``" +
                                invalidStrings[i]
                                +"'' parsed as " + d + ".");
                    }
                    catch(NumberFormatException e) {
                        // expected
                    }
                }

            }
        }

        static class ToString {

            public static void main(String args[]) {
                if (!Double.toString(0.001).equals("0.001"))
                    throw new RuntimeException("Double.toString(0.001) is not \"0.001\"");
                if (!Double.toString(0.002).equals("0.002"))
                    throw new RuntimeException("Double.toString(0.002) is not \"0.002\"");
            }
        }

        static class Equals {

            public static void main(String args[]){
                Double double1 = 1.0d;
                if (!double1.equals(1.0d))
                    throw new RuntimeException("Double 1.0 does not equal 1.0d");
            }
        }

        static class BitwiseConversion {
            public static void main(String args[]) {
                if (Double.longBitsToDouble(Double.doubleToLongBits(Double.POSITIVE_INFINITY)) != Double.POSITIVE_INFINITY)
                    throw new RuntimeException("Double.longBitsToDouble(Double.doubleToLongBits(start)) did not return start");
                if (Double.longBitsToDouble(Double.doubleToLongBits(Double.NEGATIVE_INFINITY)) != Double.NEGATIVE_INFINITY)
                    throw new RuntimeException("Double.longBitsToDouble(Double.doubleToLongBits(start)) did not return start");
                if (!Double.isNaN(Double.longBitsToDouble(Double.doubleToLongBits(Double.NaN))))
                    throw new RuntimeException("Double.longBitsToDouble(Double.doubleToLongBits(start)) did not return start");
            }
        }
    }

    @Callable
    public static boolean testDouble() throws Exception{
        DoubleTest.Constants.main(null);
        DoubleTest.Extrema.main(null);
        DoubleTest.NaNInfinityParsing.main(null);
        DoubleTest.ToString.main(null);
        DoubleTest.Equals.main(null);
        DoubleTest.BitwiseConversion.main(null);
        return true;
    }

    //========================================================
    // Test for java.lang.Float
    //========================================================
    static class FloatTest {
        static class Constants {
            /*
             * This compile-only test is to make sure that the primitive
             * public static final fields in java.lang.Float are "constant
             * expressions" as defined by "The Java Language Specification,
             * 2nd edition" section 15.28; a different test checks the values
             * of those fields.
             */
            public static void main(String[] args) throws Exception {
                int i = 0;
                switch (i) {
                    case (int)Float.NaN:                    // 0
                        break;
                    case (int)Float.MIN_VALUE + 1:          // 0 + 1
                        break;
                    case (int)Float.MIN_NORMAL + 2:         // 0 + 2
                        break;
                    case Float.MIN_EXPONENT:                // -126
                        break;
                    case Float.MAX_EXPONENT:                // 127
                        break;
                    case (int)Float.MAX_VALUE - 1:          // Integer.MAX_VALUE - 1
                        break;
                    case (int)Float.POSITIVE_INFINITY:      // Integer.MAX_VALUE
                        break;
                    case (int)Float.NEGATIVE_INFINITY:      // Integer.MIN_VALUE
                        break;
                }
            }
        }

        static class Extrema {
            public static void main(String[] args) throws Exception {
                if (Float.MIN_VALUE != Float.intBitsToFloat(0x1))
                    throw new RuntimeException("Float.MIN_VALUE is not equal "+
                            "to intBitsToFloat(0x1).");

                if (Float.MIN_NORMAL != Float.intBitsToFloat(0x00800000))
                    throw new RuntimeException("Float.MIN_NORMAL is not equal "+
                            "to intBitsToFloat(0x00800000).");

                if (Float.MAX_VALUE != Float.intBitsToFloat(0x7f7fffff))
                    throw new RuntimeException("Float.MAX_VALUE is not equal "+
                            "to intBitsToFloat(0x7f7fffff).");
            }
        }

        static class NaNInfinityParsing {

            static String NaNStrings[] = {
                    "NaN",
                    "+NaN",
                    "-NaN"
            };

            static String infinityStrings[] = {
                    "Infinity",
                    "+Infinity",
                    "-Infinity",
            };

            static String invalidStrings[] = {
                    "+",
                    "-",
                    "@",
                    "N",
                    "Na",
                    "Nan",
                    "NaNf",
                    "NaNd",
                    "NaNF",
                    "NaND",
                    "+N",
                    "+Na",
                    "+Nan",
                    "+NaNf",
                    "+NaNd",
                    "+NaNF",
                    "+NaND",
                    "-N",
                    "-Na",
                    "-Nan",
                    "-NaNf",
                    "-NaNd",
                    "-NaNF",
                    "-NaND",
                    "I",
                    "In",
                    "Inf",
                    "Infi",
                    "Infin",
                    "Infini",
                    "Infinit",
                    "InfinitY",
                    "Infinityf",
                    "InfinityF",
                    "Infinityd",
                    "InfinityD",
                    "+I",
                    "+In",
                    "+Inf",
                    "+Infi",
                    "+Infin",
                    "+Infini",
                    "+Infinit",
                    "+InfinitY",
                    "+Infinityf",
                    "+InfinityF",
                    "+Infinityd",
                    "+InfinityD",
                    "-I",
                    "-In",
                    "-Inf",
                    "-Infi",
                    "-Infin",
                    "-Infini",
                    "-Infinit",
                    "-InfinitY",
                    "-Infinityf",
                    "-InfinityF",
                    "-Infinityd",
                    "-InfinityD",
                    "NaNInfinity",
                    "InfinityNaN",
                    "nan",
                    "infinity"
            };

            public static void main(String [] argv) throws Exception {
                int i;
                float d;

                // Test valid NaN strings
                for(i = 0; i < NaNStrings.length; i++) {
                    if(!Float.isNaN(d=Float.parseFloat(NaNStrings[i]))) {
                        throw new RuntimeException("NaN string ``" + NaNStrings[i]
                                + "'' did not parse as a NaN; returned " +
                                d + " instead.");
                    }
                }

                // Test valid Infinity strings
                for(i = 0; i < infinityStrings.length; i++) {
                    if(!Float.isInfinite(d=Float.parseFloat(infinityStrings[i]))) {
                        throw new RuntimeException("Infinity string ``" +
                                infinityStrings[i] +
                                "'' did not parse as infinity; returned " +
                                d + "instead.");
                    }
                    // check sign of result

                    boolean negative = (infinityStrings[i].charAt(0) == '-');
                    if(d != (negative?Float.NEGATIVE_INFINITY:
                            Float.POSITIVE_INFINITY))
                        throw new RuntimeException("Infinity has wrong sign;" +
                                (negative?"positive instead of negative.":
                                        "negative instead of positive."));
                }

                // Test almost valid strings
                for(i = 0; i < invalidStrings.length; i++) {
                    try {
                        float result;
                        d = Float.parseFloat(invalidStrings[i]);
                        throw new RuntimeException("Invalid string ``" +
                                invalidStrings[i]
                                +"'' parsed as " + d + ".");
                    }
                    catch(NumberFormatException e) {
                        // expected
                    }
                }

            }
        }

        static class BitwiseConversion {
            public static void main(String args[]) {
                if (Float.intBitsToFloat(Float.floatToIntBits(Float.POSITIVE_INFINITY)) != Float.POSITIVE_INFINITY)
                    throw new RuntimeException("Float.intBitsToFloat(Float.floatToIntBits(start)) did not return start");
                if (Float.intBitsToFloat(Float.floatToIntBits(Float.NEGATIVE_INFINITY)) != Float.NEGATIVE_INFINITY)
                    throw new RuntimeException("Float.intBitsToFloat(Float.floatToIntBits(start)) did not return start");
                if (!Float.isNaN(Float.intBitsToFloat(Float.floatToIntBits(Float.NaN))))
                    throw new RuntimeException("Float.intBitsToFloat(Float.floatToIntBits(start)) did not return start");
            }
        }
    }

    @Callable
    public static boolean testFloat() throws Exception{
        FloatTest.Constants.main(null);
        FloatTest.Extrema.main(null);
        FloatTest.NaNInfinityParsing.main(null);
        FloatTest.BitwiseConversion.main(null);
        return true;
    }

    //========================================================
    // Test for java.lang.Integer
    //========================================================

    static class IntegerTest {
        public static class Decode {

            private static void check(String val, int expected) {
                int n = (Integer.decode(val)).intValue();
                if (n != expected)
                    throw new RuntimeException("Integer.decode failed. String:" +
                            val + " Result:" + n);
            }

            private static void checkFailure(String val, String message) {
                try {
                    int n = (Integer.decode(val)).intValue();
                    throw new RuntimeException(message);
                } catch (NumberFormatException e) { /* Okay */}
            }

            public static void main(String[] args) throws Exception {
                check(new String(""+Integer.MIN_VALUE), Integer.MIN_VALUE);
                check(new String(""+Integer.MAX_VALUE), Integer.MAX_VALUE);

                check("10",   10);
                check("0x10", 16);
                check("0X10", 16);
                check("010",  8);
                check("#10",  16);

                check("+10",   10);
                check("+0x10", 16);
                check("+0X10", 16);
                check("+010",  8);
                check("+#10",  16);

                check("-10",   -10);
                check("-0x10", -16);
                check("-0X10", -16);
                check("-010",  -8);
                check("-#10",  -16);

                check(Long.toString(Integer.MIN_VALUE), Integer.MIN_VALUE);
                check(Long.toString(Integer.MAX_VALUE), Integer.MAX_VALUE);

                checkFailure("0x-10",   "Integer.decode allows negative sign in wrong position.");
                checkFailure("0x+10",   "Integer.decode allows positive sign in wrong position.");

                checkFailure("+",       "Raw plus sign allowed.");
                checkFailure("-",       "Raw minus sign allowed.");

                checkFailure(Long.toString((long)Integer.MIN_VALUE - 1L), "Out of range");
                checkFailure(Long.toString((long)Integer.MAX_VALUE + 1L), "Out of range");

                checkFailure("", "Empty String");

                try {
                    Integer.decode(null);
                    throw new RuntimeException("Integer.decode(null) expected to throw NPE");
                } catch (NullPointerException npe) {/* Okay */}
            }
        }

        public static class ParsingTest {

            public static void main(String[] argv) {
                check(+100, "+100");
                check(-100, "-100");

                check(0, "+0");
                check(0, "-0");
                check(0, "+00000");
                check(0, "-00000");

                check(0, "0");
                check(1, "1");
                check(9, "9");

                checkFailure("");
                checkFailure("\u0000");
                checkFailure("\u002f");
                checkFailure("+");
                checkFailure("-");
                checkFailure("++");
                checkFailure("+-");
                checkFailure("-+");
                checkFailure("--");
                checkFailure("++100");
                checkFailure("--100");
                checkFailure("+-6");
                checkFailure("-+6");
                checkFailure("*100");

                // check offset based methods
                check(0, "+00000", 0, 6, 10);
                check(0, "-00000", 0, 6, 10);
                check(0, "test-00000", 4, 10, 10);
                check(-12345, "test-12345", 4, 10, 10);
                check(12345, "xx12345yy", 2, 7, 10);
                check(15, "xxFyy", 2, 3, 16);

                checkNumberFormatException("", 0, 0, 10);
                checkNumberFormatException("+-6", 0, 3, 10);
                checkNumberFormatException("1000000", 7, 7, 10);
                checkNumberFormatException("1000000", 0, 2, Character.MAX_RADIX + 1);
                checkNumberFormatException("1000000", 0, 2, Character.MIN_RADIX - 1);

                checkIndexOutOfBoundsException("1000000", 10, 4, 10);
                checkIndexOutOfBoundsException("1000000", -1, 2, Character.MAX_RADIX + 1);
                checkIndexOutOfBoundsException("1000000", -1, 2, Character.MIN_RADIX - 1);
                checkIndexOutOfBoundsException("1000000", 10, 2, Character.MAX_RADIX + 1);
                checkIndexOutOfBoundsException("1000000", 10, 2, Character.MIN_RADIX - 1);
                checkIndexOutOfBoundsException("-1", 0, 3, 10);
                checkIndexOutOfBoundsException("-1", 2, 3, 10);
                checkIndexOutOfBoundsException("-1", -1, 2, 10);

                checkNull(0, 1, 10);
                checkNull(-1, 0, 10);
                checkNull(0, 0, 10);
                checkNull(0, -1, 10);
                checkNull(-1, -1, -1);
            }

            private static void check(int expected, String val) {
                int n = Integer.parseInt(val);
                if (n != expected)
                    throw new RuntimeException("Integer.parseInt failed. String:" +
                            val + " Result:" + n);
            }

            private static void checkFailure(String val) {
                int n = 0;
                try {
                    n = Integer.parseInt(val);
                    throw new RuntimeException();
                } catch (NumberFormatException nfe) {
                    ; // Expected
                }
            }

            private static void checkNumberFormatException(String val, int start, int end, int radix) {
                int n = 0;
                try {
                    n = Integer.parseInt(val, start, end, radix);
                    throw new RuntimeException();
                } catch (NumberFormatException nfe) {
                    ; // Expected
                }
            }

            private static void checkIndexOutOfBoundsException(String val, int start, int end, int radix) {
                int n = 0;
                try {
                    n = Integer.parseInt(val, start, end, radix);
                    throw new RuntimeException();
                } catch (IndexOutOfBoundsException ioob) {
                    ; // Expected
                }
            }

            private static void checkNull(int start, int end, int radix) {
                int n = 0;
                try {
                    n = Integer.parseInt(null, start, end, radix);
                    throw new RuntimeException();
                } catch (NullPointerException npe) {
                    ; // Expected
                }
            }

            private static void check(int expected, String val, int start, int end, int radix) {
                int n = Integer.parseInt(val, start, end, radix);
                if (n != expected)
                    throw new RuntimeException("Integer.parsedInt failed. Expected: " + expected + " String: \"" +
                            val + "\", start: " + start + ", end: " + end + ", radix: " + radix + " Result:" + n);
            }
        }

        public static class ToString {

            public static void main(String[] args) throws Exception {
                test("-2147483648", Integer.MIN_VALUE);
                test("2147483647", Integer.MAX_VALUE);
                test("0", 0);

                // Wiggle around the exponentially increasing base.
                final int LIMIT = (1 << 15);
                int base = 10000;
                while (base < Integer.MAX_VALUE / 10) {
                    for (int d = -LIMIT; d < LIMIT; d++) {
                        int c = base + d;
                        if (c > 0) {
                            buildAndTest(c);
                        }
                    }
                    base *= 10;
                }

                for (int c = 1; c < LIMIT; c++) {
                    buildAndTest(Integer.MAX_VALUE - LIMIT + c);
                }
            }

            private static void buildAndTest(int c) {
                if (c <= 0) {
                    throw new IllegalArgumentException("Test bug: can only handle positives, " + c);
                }

                StringBuilder sbN = new StringBuilder();
                StringBuilder sbP = new StringBuilder();

                int t = c;
                while (t > 0) {
                    char digit = (char) ('0' + (t % 10));
                    sbN.append(digit);
                    sbP.append(digit);
                    t = t / 10;
                }

                sbN.append("-");
                sbN.reverse();
                sbP.reverse();

                test(sbN.toString(), -c);
                test(sbP.toString(), c);
            }

            private static void test(String expected, int value) {
                String actual = Integer.toString(value);
                if (!expected.equals(actual)) {
                    throw new RuntimeException("Expected " + expected + ", but got " + actual);
                }
            }
        }

        public static void comparableTest() {
            Comparable<Object> c = new Comparable<Object>() {
                @Override
                public int compareTo(Object o) {
                    return 0;
                }
            };
            int result = c.compareTo(1);
            if (result != 0) {
                throw new RuntimeException("Expected " + 0 + ", but got " + result);
            }
        }

        public static void reverse(){
            int value = 168;
            int reversed = Integer.reverse(value);
            if(reversed!= 352321536 || Integer.reverse(reversed)!=value){
                throw new RuntimeException("Reverse of " + value + ",was equal to " + reversed);
            }
        }

        public static void bitOperation(){
            if (Integer.highestOneBit(0) != 0)
                throw new RuntimeException("unexpected value for highestOneBit(0)");
            if (Integer.highestOneBit(-1) != Integer.MIN_VALUE)
                throw new RuntimeException("unexpected value for highestOneBit(-1)");
            if (Integer.highestOneBit(1) != 1)
                throw new RuntimeException("unexpected value for highestOneBit(1)");

            if (Integer.lowestOneBit(0) != 0)
                throw new RuntimeException("unexpected value for lowestOneBit(0)");
            if (Integer.lowestOneBit(-1) != 1)
                throw new RuntimeException("unexpected value for lowestOneBit(-1)");
            if (Integer.lowestOneBit(Integer.MIN_VALUE) != Integer.MIN_VALUE)
                throw new RuntimeException("unexpected value for lowestOneBit(MIN_VALUE)");

            if (Integer.numberOfLeadingZeros(0) != Integer.SIZE)
                throw new RuntimeException("unexpected value for numberOfLeadingZeros(0)");
            if (Integer.numberOfLeadingZeros(-1) != 0)
                throw new RuntimeException("unexpected value for numberOfLeadingZeros(-1)");
            if (Integer.numberOfLeadingZeros(1) != (Integer.SIZE - 1))
                throw new RuntimeException("unexpected value for numberOfLeadingZeros(1)");

            if (Integer.numberOfTrailingZeros(0) != Integer.SIZE)
                throw new RuntimeException("unexpected value for numberOfTrailingZeros(0)");
            if (Integer.numberOfTrailingZeros(1) != 0)
                throw new RuntimeException("unexpected value for numberOfTrailingZeros(1)");
            if (Integer.numberOfTrailingZeros(Integer.MIN_VALUE) != (Integer.SIZE - 1))
                throw new RuntimeException("unexpected value for numberOfTrailingZeros(MIN_VALUE)");

            if (Integer.bitCount(Integer.MAX_VALUE) != Integer.bitCount(Integer.reverseBytes(Integer.MAX_VALUE)))
                throw new RuntimeException("unexpected value for Integer.bitCount(Integer.reverseBytes(MAX_VALUE))");
        }
    }

    @Callable
    public static boolean testInteger() throws Exception{
        IntegerTest.Decode.main(null);
        IntegerTest.ParsingTest.main(null);
        IntegerTest.ToString.main(null);
        IntegerTest.reverse();
        IntegerTest.comparableTest();
        IntegerTest.bitOperation();
        return true;
    }

    //========================================================
    // Test for java.lang.Long
    //========================================================

    static class LongTest {
        static class Decode {

            private static void check(String val, long expected) {
                long n = (Long.decode(val)).longValue();
                if (n != expected)
                    throw new RuntimeException("Long.decode failed. String:" +
                            val + " Result:" + n);
            }

            private static void checkFailure(String val, String message) {
                try {
                    long n = (Long.decode(val)).longValue();
                    throw new RuntimeException(message);
                } catch (NumberFormatException e) { /* Okay */}
            }

            public static void main(String[] args) throws Exception {
                check(new String("" + Long.MIN_VALUE), Long.MIN_VALUE);
                check(new String("" + Long.MAX_VALUE), Long.MAX_VALUE);

                check("10", 10L);
                check("0x10", 16L);
                check("0X10", 16L);
                check("010", 8L);
                check("#10", 16L);

                check("+10", 10L);
                check("+0x10", 16L);
                check("+0X10", 16L);
                check("+010", 8L);
                check("+#10", 16L);

                check("-10", -10L);
                check("-0x10", -16L);
                check("-0X10", -16L);
                check("-010", -8L);
                check("-#10", -16L);

                check(Long.toString(Long.MIN_VALUE), Long.MIN_VALUE);
                check(Long.toString(Long.MAX_VALUE), Long.MAX_VALUE);

                checkFailure("0x-10", "Long.decode allows negative sign in wrong position.");
                checkFailure("0x+10", "Long.decode allows positive sign in wrong position.");

                checkFailure("+", "Raw plus sign allowed.");
                checkFailure("-", "Raw minus sign allowed.");

//                checkFailure(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE).toString(),
//                        "Out of range");
//                checkFailure(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString(),
//                        "Out of range");

                checkFailure("", "Empty String");

                try {
                    Long.decode(null);
                    throw new RuntimeException("Long.decode(null) expected to throw NPE");
                } catch (NullPointerException npe) {/* Okay */}
            }
        }

        static class ParsingTest {

            public static void main(String[] argv) {
                check(+100L, "+100");
                check(-100L, "-100");

                check(0L, "+0");
                check(0L, "-0");
                check(0L, "+00000");
                check(0L, "-00000");

                check(0L, "0");
                check(1L, "1");
                check(9L, "9");

                checkFailure("");
                checkFailure("\u0000");
                checkFailure("\u002f");
                checkFailure("+");
                checkFailure("-");
                checkFailure("++");
                checkFailure("+-");
                checkFailure("-+");
                checkFailure("--");
                checkFailure("++100");
                checkFailure("--100");
                checkFailure("+-6");
                checkFailure("-+6");
                checkFailure("*100");

                check(0L, "test-00000", 4, 10, 10);
                check(-12345L, "test-12345", 4, 10, 10);
                check(12345L, "xx12345yy", 2, 7, 10);
                check(123456789012345L, "xx123456789012345yy", 2, 17, 10);
                check(15L, "xxFyy", 2, 3, 16);

                checkNumberFormatException("", 0, 0, 10);
                checkNumberFormatException("+-6", 0, 3, 10);
                checkNumberFormatException("1000000", 7, 7, 10);
                checkNumberFormatException("1000000", 0, 2, Character.MAX_RADIX + 1);
                checkNumberFormatException("1000000", 0, 2, Character.MIN_RADIX - 1);

                checkIndexOutOfBoundsException("", 1, 1, 10);
                checkIndexOutOfBoundsException("1000000", 10, 4, 10);
                checkIndexOutOfBoundsException("1000000", 10, 2, Character.MAX_RADIX + 1);
                checkIndexOutOfBoundsException("1000000", 10, 2, Character.MIN_RADIX - 1);
                checkIndexOutOfBoundsException("1000000", -1, 2, Character.MAX_RADIX + 1);
                checkIndexOutOfBoundsException("1000000", -1, 2, Character.MIN_RADIX - 1);
                checkIndexOutOfBoundsException("-1", 0, 3, 10);
                checkIndexOutOfBoundsException("-1", 2, 3, 10);
                checkIndexOutOfBoundsException("-1", -1, 2, 10);

                checkNull(0, 1, 10);
                checkNull(-1, 0, 10);
                checkNull(0, 0, 10);
                checkNull(0, -1, 10);
                checkNull(-1, -1, -1);
            }

            private static void check(long expected, String val) {
                long n = Long.parseLong(val);
                if (n != expected)
                    throw new RuntimeException("Long.parseLong failed. String:" +
                            val + " Result:" + n);
            }

            private static void checkFailure(String val) {
                long n = 0L;
                try {
                    n = Long.parseLong(val);
                    throw new RuntimeException();
                } catch (NumberFormatException nfe) {
                    ; // Expected
                }
            }

            private static void checkNumberFormatException(String val, int start, int end, int radix) {
                long n = 0;
                try {
                    n = Long.parseLong(val, start, end, radix);
                    throw new RuntimeException();
                } catch (NumberFormatException nfe) {
                    ; // Expected
                }
            }

            private static void checkIndexOutOfBoundsException(String val, int start, int end, int radix) {
                long n = 0;
                try {
                    n = Long.parseLong(val, start, end, radix);
                    throw new RuntimeException();
                } catch (IndexOutOfBoundsException ioob) {
                    ; // Expected
                }
            }

            private static void checkNull(int start, int end, int radix) {
                long n = 0;
                try {
                    n = Long.parseLong(null, start, end, radix);
                    throw new RuntimeException();
                } catch (NullPointerException npe) {
                    ; // Expected
                }
            }

            private static void check(long expected, String val, int start, int end, int radix) {
                long n = Long.parseLong(val, start, end, radix);
                if (n != expected)
                    throw new RuntimeException("Long.parseLong failed. Expexted: " + expected + " String: \"" +
                            val + "\", start: " + start + ", end: " + end + " radix: " + radix + " Result: " + n);
            }
        }

        static class ToString {

            public static void main(String[] args) throws Exception {
                test("-9223372036854775808", Long.MIN_VALUE);
                test("9223372036854775807", Long.MAX_VALUE);
                test("0", 0);

                // Wiggle around the exponentially increasing base.
                final int LIMIT = (1 << 15);
                long base = 10000;
                while (base < Long.MAX_VALUE / 10) {
                    for (int d = -LIMIT; d < LIMIT; d++) {
                        long c = base + d;
                        if (c > 0) {
                            buildAndTest(c);
                        }
                    }
                    base *= 10;
                }

                for (int c = 1; c < LIMIT; c++) {
                    buildAndTest(Long.MAX_VALUE - LIMIT + c);
                }
            }

            private static void buildAndTest(long c) {
                if (c <= 0) {
                    throw new IllegalArgumentException("Test bug: can only handle positives, " + c);
                }

                StringBuilder sbN = new StringBuilder();
                StringBuilder sbP = new StringBuilder();

                long t = c;
                while (t > 0) {
                    char digit = (char) ('0' + (t % 10));
                    sbN.append(digit);
                    sbP.append(digit);
                    t = t / 10;
                }

                sbN.append("-");
                sbN.reverse();
                sbP.reverse();

                test(sbN.toString(), -c);
                test(sbP.toString(), c);
            }

            private static void test(String expected, long value) {
                String actual = Long.toString(value);
                if (!expected.equals(actual)) {
                    throw new RuntimeException("Expected " + expected + ", but got " + actual);
                }
            }
        }

    }

    @Callable
    public static boolean testLong() throws Exception{
        LongTest.Decode.main(null);
        LongTest.ParsingTest.main(null);
        LongTest.ToString.main(null);
        return true;
    }

    //========================================================
    // Test for java.lang.Short
    //========================================================

    static class ShortTest{
        static class ByteSwap {
            public static void main(String args[]) {
                if (Short.reverseBytes((short)0xaabb) != (short)0xbbaa)
                    throw new RuntimeException("short");

            }
        }

        static class Decode {

            private static void check(String ashort, short expected) {
                short sh = (Short.decode(ashort)).shortValue();
                if (sh != expected)
                    throw new RuntimeException("Short.decode failed. String:" +
                            ashort + " Result:" + sh);
            }

            private static void checkFailure(String val, String message) {
                try {
                    short n = (Short.decode(val)).shortValue();
                    throw new RuntimeException(message);
                } catch (NumberFormatException e) { /* Okay */}
            }

            public static void main(String[] args) throws Exception {
                check(new String(""+Short.MIN_VALUE), Short.MIN_VALUE);
                check(new String(""+Short.MAX_VALUE), Short.MAX_VALUE);

                check("10",   (short)10);
                check("0x10", (short)16);
                check("0X10", (short)16);
                check("010",  (short)8);
                check("#10",  (short)16);

                check("+10",   (short)10);
                check("+0x10", (short)16);
                check("+0X10", (short)16);
                check("+010",  (short)8);
                check("+#10",  (short)16);

                check("-10",   (short)-10);
                check("-0x10", (short)-16);
                check("-0X10", (short)-16);
                check("-010",  (short)-8);
                check("-#10",  (short)-16);

                check(Integer.toString((int)Short.MIN_VALUE), Short.MIN_VALUE);
                check(Integer.toString((int)Short.MAX_VALUE), Short.MAX_VALUE);

                checkFailure("0x-10",   "Short.decode allows negative sign in wrong position.");
                checkFailure("0x+10",   "Short.decode allows positive sign in wrong position.");

                checkFailure("+",       "Raw plus sign allowed.");
                checkFailure("-",       "Raw minus sign allowed.");

                checkFailure(Integer.toString((int)Short.MIN_VALUE - 1), "Out of range");
                checkFailure(Integer.toString((int)Short.MAX_VALUE + 1), "Out of range");

                checkFailure("", "Empty String");
            }
        }
    }

    @Callable
    public static boolean testShort() throws Exception{
        ShortTest.Decode.main(null);
        ShortTest.ByteSwap.main(null);
        return true;
    }


    //========================================================
    // Misc Test
    //========================================================
    @Callable
    public static boolean testAutoboxing(){
        boolean ret = true;

        Boolean     a = true;
        Character   c = 'a';
        Double      d = 0.1d;
        Float       e = 0.1f;
        Integer     f = 1;
        Long        g = 100000000L;
        Short       h = 1;

        boolean     aa = a;
        char        cc = c;
        double      dd = d;
        float       ee = e;
        int         ff = f;
        long        gg = g;
        short       hh = h;

        return ret;
    }


}
