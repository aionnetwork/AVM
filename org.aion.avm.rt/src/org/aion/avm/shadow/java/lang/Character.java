package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


public class Character extends Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final int avm_MIN_RADIX = 2;

    public static final int avm_MAX_RADIX = 36;

    public static final char avm_MIN_VALUE = '\u0000';

    public static final char avm_MAX_VALUE = '\uFFFF';

    public static final Class<Character> avm_TYPE = new Class(java.lang.Character.TYPE);

    public static final byte avm_UNASSIGNED = 0;

    public static final byte avm_UPPERCASE_LETTER = 1;

    public static final byte avm_LOWERCASE_LETTER = 2;

    public static final byte avm_TITLECASE_LETTER = 3;

    public static final byte avm_MODIFIER_LETTER = 4;

    public static final byte avm_OTHER_LETTER = 5;

    public static final byte avm_NON_SPACING_MARK = 6;

    public static final byte avm_ENCLOSING_MARK = 7;

    public static final byte avm_COMBINING_SPACING_MARK = 8;

    public static final byte avm_DECIMAL_DIGIT_NUMBER = 9;

    public static final byte avm_LETTER_NUMBER = 10;

    public static final byte avm_OTHER_NUMBER = 11;

    public static final byte avm_SPACE_SEPARATOR = 12;

    public static final byte avm_LINE_SEPARATOR = 13;

    public static final byte avm_PARAGRAPH_SEPARATOR = 14;

    public static final byte avm_CONTROL = 15;

    public static final byte avm_FORMAT = 16;

    public static final byte avm_PRIVATE_USE = 18;

    public static final byte avm_SURROGATE = 19;

    public static final byte avm_DASH_PUNCTUATION = 20;

    public static final byte avm_START_PUNCTUATION = 21;

    public static final byte avm_END_PUNCTUATION = 22;

    public static final byte avm_CONNECTOR_PUNCTUATION = 23;

    public static final byte avm_OTHER_PUNCTUATION = 24;

    public static final byte avm_MATH_SYMBOL = 25;

    public static final byte avm_CURRENCY_SYMBOL = 26;

    public static final byte avm_MODIFIER_SYMBOL = 27;

    public static final byte avm_OTHER_SYMBOL = 28;

    public static final byte avm_INITIAL_QUOTE_PUNCTUATION = 29;

    public static final byte avm_FINAL_QUOTE_PUNCTUATION = 30;

    public static final byte avm_DIRECTIONALITY_UNDEFINED = -1;

    public static final byte avm_DIRECTIONALITY_LEFT_TO_RIGHT = 0;

    public static final byte avm_DIRECTIONALITY_RIGHT_TO_LEFT = 1;

    public static final byte avm_DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;

    public static final byte avm_DIRECTIONALITY_EUROPEAN_NUMBER = 3;

    public static final byte avm_DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;

    public static final byte avm_DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;

    public static final byte avm_DIRECTIONALITY_ARABIC_NUMBER = 6;

    public static final byte avm_DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;

    public static final byte avm_DIRECTIONALITY_NONSPACING_MARK = 8;

    public static final byte avm_DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;

    public static final byte avm_DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;

    public static final byte avm_DIRECTIONALITY_SEGMENT_SEPARATOR = 11;

    public static final byte avm_DIRECTIONALITY_WHITESPACE = 12;

    public static final byte avm_DIRECTIONALITY_OTHER_NEUTRALS = 13;

    public static final byte avm_DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;

    public static final byte avm_DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;

    public static final byte avm_DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;

    public static final byte avm_DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;

    public static final byte avm_DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;

    public static final byte avm_DIRECTIONALITY_LEFT_TO_RIGHT_ISOLATE = 19;

    public static final byte avm_DIRECTIONALITY_RIGHT_TO_LEFT_ISOLATE = 20;

    public static final byte avm_DIRECTIONALITY_FIRST_STRONG_ISOLATE = 21;

    public static final byte avm_DIRECTIONALITY_POP_DIRECTIONAL_ISOLATE = 22;

    public static final char avm_MIN_HIGH_SURROGATE = '\uD800';

    public static final char avm_MAX_HIGH_SURROGATE = '\uDBFF';

    public static final char avm_MIN_LOW_SURROGATE  = '\uDC00';

    public static final char avm_MAX_LOW_SURROGATE  = '\uDFFF';

    public static final char avm_MIN_SURROGATE = avm_MIN_HIGH_SURROGATE;

    public static final char avm_MAX_SURROGATE = avm_MAX_LOW_SURROGATE;

    public static final int avm_MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;

    public static final int avm_MIN_CODE_POINT = 0x000000;

    public static final int avm_MAX_CODE_POINT = 0X10FFFF;

    public Character(char c) {
        this.v = c;
    }

    public static Character avm_valueOf(char c) {
        return new Character(c);
    }

    public char avm_charValue() {
        lazyLoad();
        return v;
    }

    @Override
    public int avm_hashCode() {
        lazyLoad();
        return Character.avm_hashCode(v);
    }

    public static int avm_hashCode(char value) {
        return (int)value;
    }

    public boolean equals(IObject obj) {
        if (obj instanceof Character) {
            return v == ((Character)obj).avm_charValue();
        }
        return false;
    }

    public String avm_toString() {
        lazyLoad();
        char buf[] = {v};
        CharArray a = new CharArray(buf);
        return String.avm_valueOf(a);
    }

    public static String avm_toString(char c) {
        return new String(java.lang.Character.toString(c));
    }

    public static boolean avm_isValidCodePoint(int codePoint){
        return java.lang.Character.isValidCodePoint(codePoint);
    }

    public static boolean avm_isBmpCodePoint(int codePoint){
        return java.lang.Character.isBmpCodePoint(codePoint);
    }

    public static boolean avm_isSupplementaryCodePoint(int codePoint){
        return java.lang.Character.isSupplementaryCodePoint(codePoint);
    }

    public static boolean avm_isHighSurrogate(char ch) {
        return java.lang.Character.isHighSurrogate(ch);
    }

    public static boolean avm_isLowSurrogate(char ch) {
        return java.lang.Character.isLowSurrogate(ch);
    }

    public static boolean avm_isSurrogate(char ch){
        return java.lang.Character.isSurrogate(ch);
    }

    public static boolean avm_isSurrogatePair(char high, char low) {
        return java.lang.Character.isSurrogatePair(high, low);
    }

    public static int avm_charCount(int codePoint) {
        return java.lang.Character.charCount(codePoint);
    }

    public static int avm_toCodePoint(char high, char low) {
        return java.lang.Character.toCodePoint(high, low);
    }

    public static int avm_codePointAt(CharSequence seq, int index) {
        return java.lang.Character.codePointAt(seq.avm_toString().getUnderlying(), index);
    }

    public static int avm_codePointAt(CharArray c, int index) {
        return java.lang.Character.codePointAt(c.getUnderlying(), index);
    }

    public static int avm_codePointAt(CharArray c, int index, int limit) {
        return java.lang.Character.codePointAt(c.getUnderlying(), index, limit);
    }

    public static int avm_codePointBefore(CharSequence seq, int index) {
        return java.lang.Character.codePointAt(seq.avm_toString().getUnderlying(), index);
    }

    public static int avm_codePointBefore(CharArray c, int index) {
        return java.lang.Character.codePointBefore(c.getUnderlying(), index);
    }

    public static int avm_codePointBefore(CharArray c, int index, int start) {
        return java.lang.Character.codePointBefore(c.getUnderlying(), index, start);
    }

    public static char avm_highSurrogate(int codePoint) {
        return java.lang.Character.highSurrogate(codePoint);
    }

    public static char avm_lowSurrogate(int codePoint) {
        return java.lang.Character.lowSurrogate(codePoint);
    }

    public static int avm_toChars(int codePoint, CharArray des, int dstIndex) {
        return java.lang.Character.toChars(codePoint, des.getUnderlying(), dstIndex);
    }

    public static CharArray avm_toChars(int codePoint) {
        return new CharArray(java.lang.Character.toChars(codePoint));
    }

    public static int avm_codePointCount(CharSequence seq, int beginIndex, int endIndex){
        return java.lang.Character.codePointCount(seq.avm_toString().getUnderlying(), beginIndex, endIndex);
    }

    public static int avm_codePointCount(CharArray a, int offset, int count) {
        return java.lang.Character.codePointCount(a.getUnderlying(), offset, count);
    }

    public static int avm_offsetByCodePoints(CharSequence seq, int index, int codePointOffset) {
        return java.lang.Character.offsetByCodePoints(seq.avm_toString().getUnderlying(), index, codePointOffset);
    }

    public static int avm_offsetByCodePoints(CharArray a, int start, int count,
                                         int index, int codePointOffset) {
        return java.lang.Character.offsetByCodePoints(a.getUnderlying(), start, count,
                index, codePointOffset);
    }

    public static boolean avm_isLowerCase(char ch){
        return java.lang.Character.isLowerCase(ch);
    }

    public static boolean avm_isLowerCase(int codePoint){
        return java.lang.Character.isLowerCase(codePoint);
    }

    public static boolean avm_isUpperCase(char ch){
        return java.lang.Character.isUpperCase(ch);
    }

    public static boolean avm_isUpperCase(int codePoint){
        return java.lang.Character.isUpperCase(codePoint);
    }

    public static boolean avm_isTitleCase(char ch){
        return java.lang.Character.isTitleCase(ch);
    }

    public static boolean avm_isTitleCase(int codePoint){
        return java.lang.Character.isTitleCase(codePoint);
    }

    public static boolean avm_isDigit(char ch){
        return java.lang.Character.isDigit(ch);
    }

    public static boolean avm_isDigit(int codePoint){
        return java.lang.Character.isDigit(codePoint);
    }

    public static boolean avm_isDefined(char ch){
        return java.lang.Character.isDefined(ch);
    }

    public static boolean avm_isDefined(int codePoint){
        return java.lang.Character.isDefined(codePoint);
    }

    public static boolean avm_isLetter(char ch){
        return java.lang.Character.isLetter(ch);
    }

    public static boolean avm_isLetter(int codePoint){
        return java.lang.Character.isLetter(codePoint);
    }

    public static boolean avm_isLetterOrDigit(char ch){
        return java.lang.Character.isLetterOrDigit(ch);
    }

    public static boolean avm_isLetterOrDigit(int codePoint){
        return java.lang.Character.isLetterOrDigit(codePoint);
    }

    public static boolean avm_isAlphabetic(int codePoint){
        return java.lang.Character.isAlphabetic(codePoint);
    }

    public static boolean avm_isIdeographic(int codePoint){
        return java.lang.Character.isIdeographic(codePoint);
    }

    public static boolean avm_isJavaIdentifierStart(char ch){
        return java.lang.Character.isJavaIdentifierStart(ch);
    }

    public static boolean avm_isJavaIdentifierStart(int codePoint){
        return java.lang.Character.isJavaIdentifierStart(codePoint);
    }

    public static boolean avm_isJavaIdentifierPart(char ch){
        return java.lang.Character.isJavaIdentifierPart(ch);
    }

    public static boolean avm_isJavaIdentifierPart(int codePoint){
        return java.lang.Character.isJavaIdentifierPart(codePoint);
    }

    public static boolean avm_isUnicodeIdentifierStart(char ch){
        return java.lang.Character.isUnicodeIdentifierStart(ch);
    }

    public static boolean avm_isUnicodeIdentifierStart(int codePoint){
        return java.lang.Character.isUnicodeIdentifierStart(codePoint);
    }

    public static boolean avm_isUnicodeIdentifierPart(char ch){
        return java.lang.Character.isUnicodeIdentifierPart(ch);
    }

    public static boolean avm_isUnicodeIdentifierPart(int codePoint){
        return java.lang.Character.isUnicodeIdentifierPart(codePoint);
    }

    public static boolean avm_isIdentifierIgnorable(char ch){
        return java.lang.Character.isIdentifierIgnorable(ch);
    }

    public static boolean avm_isIdentifierIgnorable(int codePoint){
        return java.lang.Character.isIdentifierIgnorable(codePoint);
    }

    public static char avm_toLowerCase(char ch){
        return java.lang.Character.toLowerCase(ch);
    }

    public static int avm_toLowerCase(int codePoint){
        return java.lang.Character.toLowerCase(codePoint);
    }

    public static char avm_toUpperCase(char ch){
        return java.lang.Character.toUpperCase(ch);
    }

    public static int avm_toUpperCase(int codePoint){
        return java.lang.Character.toUpperCase(codePoint);
    }

    public static char avm_toTitleCase(char ch){
        return java.lang.Character.toTitleCase(ch);
    }

    public static int avm_toTitleCase(int codePoint){
        return java.lang.Character.toTitleCase(codePoint);
    }

    public static int avm_digit(char ch, int radix){
        return java.lang.Character.digit(ch, radix);
    }

    public static int avm_digit(int codePoint, int radix){
        return java.lang.Character.digit(codePoint, radix);
    }

    public static int avm_getNumericValue(char ch){
        return java.lang.Character.getNumericValue(ch);
    }

    public static int avm_getNumericValue(int codePoint){
        return java.lang.Character.getNumericValue(codePoint);
    }

    public static boolean avm_isSpaceChar(char ch){
        return java.lang.Character.isSpaceChar(ch);
    }

    public static boolean avm_isSpaceChar(int codePoint){
        return java.lang.Character.isSpaceChar(codePoint);
    }

    public static boolean avm_isWhitespace(char ch){
        return java.lang.Character.isWhitespace(ch);
    }

    public static boolean avm_isWhitespace(int codePoint){
        return java.lang.Character.isWhitespace(codePoint);
    }

    public static boolean avm_isISOControl(char ch){
        return java.lang.Character.isISOControl(ch);
    }

    public static boolean avm_isISOControl(int codePoint){
        return java.lang.Character.isISOControl(codePoint);
    }

    public static int avm_getType(char ch){
        return java.lang.Character.getType(ch);
    }

    public static int avm_getType(int codePoint){
        return java.lang.Character.getType(codePoint);
    }

    public static char avm_forDigit(int digit, int radix) {
        return java.lang.Character.forDigit(digit, radix);
    }

    public static byte avm_getDirectionality(char ch){
        return java.lang.Character.getDirectionality(ch);
    }

    public static byte avm_getDirectionality(int codePoint){
        return java.lang.Character.getDirectionality(codePoint);
    }

    public static boolean avm_isMirrored(char ch){
        return java.lang.Character.isMirrored(ch);
    }

    public static boolean avm_isMirrored(int codePoint){
        return java.lang.Character.isMirrored(codePoint);
    }

    public int avm_compareTo(Character anotherCharacter) {
        return avm_compare(this.v, anotherCharacter.v);
    }

    public static int avm_compare(char x, char y) {
        return x - y;
    }

    public static final int avm_SIZE = java.lang.Character.SIZE;

    public static final int avm_BYTES = java.lang.Character.BYTES;

    public static char avm_reverseBytes(char ch) {
        return java.lang.Character.reverseBytes(ch);
    }

    public static String avm_getName(int codePoint) {
        return new String(java.lang.Character.getName(codePoint));
    }

    public static int avm_codePointOf(String name) {
        return java.lang.Character.codePointOf(name.getUnderlying());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Character(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
        lazyLoad();
    }

    private char v;

    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof Character && this.v == ((Character) obj).v;
    }

    @Override
    public java.lang.String toString() {
        return java.lang.Character.toString(this.v);
    }


    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    // public static boolean isJavaLetter(char ch)

    // public static boolean isJavaLetterOrDigit(char ch)

    // public static boolean isSpace(char ch)

}