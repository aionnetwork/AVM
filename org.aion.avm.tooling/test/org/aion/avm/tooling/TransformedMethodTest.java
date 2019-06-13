package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.kernel.AvmTransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests that methods are transformed correctly so that all of the expected behaviour in Java is
 * replicated properly. Things like method overloading, array type hierarchies etc.
 */
public class TransformedMethodTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    private static Address deployer = avmRule.getPreminedAccount();
    private static Address dappAddress;

    @BeforeClass
    public static void setupClass() {
        dappAddress =  avmRule.deploy(deployer, BigInteger.ZERO, avmRule.getDappBytes(TransformedMethodContract.class, new byte[0]), ENERGY_LIMIT, ENERGY_PRICE).getDappAddress();
    }

    @Test
    public void testCallNothing() {
        byte[] argData = ABIUtil.encodeMethodArguments("nothing");
        AvmTransactionResult result = avmRule.call(deployer, dappAddress, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testPrimitiveBooleanParam() {
        assertEquals(TransformedMethodContract.tryPrimitiveBool(true),
            callBooleanMethod("tryPrimitiveBool", true));
        assertEquals(TransformedMethodContract.tryPrimitiveBool(false),
            callBooleanMethod("tryPrimitiveBool", false));
    }

    @Test
    public void testMultipleBooleanParams() {
        Boolean b1 = Boolean.FALSE, b2 = Boolean.TRUE;
        boolean b3 = false, b4 = false;
        assertEquals(TransformedMethodContract.collapseBools(b1, b2, b3, b4),
            callMultipleBooleansMethod(b1, b2, b3, b4));
    }

    @Test
    public void testPrimitiveByteParam() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            byte result = callByteMethod("tryPrimitiveByte", (byte) i);
            assertEquals(TransformedMethodContract.tryPrimitiveByte((byte) i), result);
        }
    }

    @Test
    public void testMultipleByteParams() {
        Byte b1 = (byte) 0x1, b2 = (byte) 0x2;
        byte b3 = (byte) 0x3, b4 = (byte) 0x4;
        assertEquals(TransformedMethodContract.collapseBytes(b1, b2, b3, b4),
            callMultipleBytesMethod(b1, b2, b3, b4));
    }

    @Test
    public void testPrimitiveCharParam() {
        char c = 'z';
        assertEquals(TransformedMethodContract.tryPrimitiveChar(c), callCharMethod("tryPrimitiveChar", c));
    }

    @Test
    public void testMultipleCharParams() {
        Character c1 = 'a', c2 = 'A';
        char c3 = ' ', c4 = '!';
        assertEquals(TransformedMethodContract.collapseChars(c1, c2, c3, c4), callMultipleCharsMethod(c1, c2, c3, c4));
    }

    @Test
    public void testPrimitiveShortParam() {
        short s = (short) 87;
        assertEquals(TransformedMethodContract.tryPrimitiveShort(s), callShortMethod("tryPrimitiveShort", s));
    }

    @Test
    public void testMultipleShortParams() {
        Short s1 = 16, s2 = 44;
        short s3 = 9823, s4 = -4574;
        short expected = TransformedMethodContract.collapseShorts(s1, s2, s3, s4);
        short result = callMultipleShortsMethod(s1, s2, s3, s4);
        assertEquals(expected, result);
    }

    @Test
    public void testPrimitiveIntParam() {
        int i = 34634;
        assertEquals(TransformedMethodContract.tryPrimitiveInt(i), callIntMethod("tryPrimitiveInt", i));
    }

    @Test
    public void testMultipleIntParams() {
        Integer i1 = 346, i2 = 9823;
        int i3 = 42, i4 = 198;
        assertEquals(TransformedMethodContract.collapseInts(i1, i2, i3, i4), callMultipleIntsMethod(i1, i2, i3, i4));
    }

    @Test
    public void testPrimitiveLongParam() {
        long l = 325;
        assertEquals(TransformedMethodContract.tryPrimitiveLong(l), callLongMethod("tryPrimitiveLong", l));
    }

    @Test
    public void testMultipleLongParams() {
        Long l1 = 43634L, l2 = 8934342L;
        long l3 = 435, l4 = 9283;
        long expected = TransformedMethodContract.collapseLongs(l1, l2, l3, l4);
        long result = callMultipleLongsMethod(l1, l2, l3, l4);
        assertEquals(expected, result);
    }

    @Test
    public void testPrimitiveFloatParam() {
        float f = 325;
        assertEquals(TransformedMethodContract.tryPrimitiveFloat(f), callFloatMethod("tryPrimitiveFloat", f), 0);
    }

    @Test
    public void testPrimitiveDoubleParam() {
        double d = 32345;
        assertEquals(TransformedMethodContract.tryPrimitiveDouble(d), callDoubleMethod("tryPrimitiveDouble", d), 0);
    }

    @Test
    public void testMultipleDoubleParams() {
        Double d1 = 435d, d2 = 23785d;
        double d3 = 56, d4 = 9823;
        double expected = TransformedMethodContract.collapseDoubles(d1, d2, d3, d4);
        double result = callMultipleDoublesMethod(d1, d2, d3, d4);
        assertEquals(expected, result, 0);
    }

    @Test
    public void testOverloadedMethods1() {
        assertEquals(TransformedMethodContract.triggerOverloadedF1(), callParameterlessMethod("triggerOverloadedF1"));
    }

    @Test
    public void testOverloadedMethods2() {
        assertEquals(TransformedMethodContract.triggerOverloadedF2(), callParameterlessMethod("triggerOverloadedF2"));
    }

    @Test
    public void testOverloadedMethods3() {
        assertEquals(TransformedMethodContract.triggerOverloadedF3(), callParameterlessMethod("triggerOverloadedF3"));
    }

    @Test
    public void testOverloadedMethods4() {
        assertEquals(TransformedMethodContract.triggerOverloadedF4(), callParameterlessMethod("triggerOverloadedF4"));
    }

    @Test
    public void testOverloadedMethods5() {
        assertEquals(TransformedMethodContract.triggerOverloadedD1(), callParameterlessMethod("triggerOverloadedD1"));
    }

    @Test
    public void testOverloadedMethods6() {
        assertEquals(TransformedMethodContract.triggerOverloadedD2(), callParameterlessMethod("triggerOverloadedD2"));
    }

    @Test
    public void testOverloadedMethods7() {
        assertEquals(TransformedMethodContract.triggerOverloadedD3(), callParameterlessMethod("triggerOverloadedD3"));
    }

    @Test
    public void testOverloadedMethods8() {
        assertEquals(TransformedMethodContract.triggerOverloadedC1(), callParameterlessMethod("triggerOverloadedC1"));
    }

    @Test
    public void testOverloadedMethods9() {
        assertEquals(TransformedMethodContract.triggerOverloadedC2(), callParameterlessMethod("triggerOverloadedC2"));
    }

    @Test
    public void testOverloadedMethods10() {
        assertEquals(TransformedMethodContract.triggerOverloadedB1(), callParameterlessMethod("triggerOverloadedB1"));
    }

    @Test
    public void testOverloadedMethods11() {
        assertEquals(TransformedMethodContract.triggerOverloadedB2(), callParameterlessMethod("triggerOverloadedB2"));
    }

    @Test
    public void testDistill() {
        assertEquals(TransformedMethodContract.triggerDistill(), callParameterlessMethod("triggerDistill"));
    }

    @Test
    public void testUpcast() {
        assertEquals(TransformedMethodContract.triggerUpcast(), callParameterlessMethod("triggerUpcast"));
    }

    @Test
    public void testMixedTypes() {
        assertEquals(TransformedMethodContract.triggerMixedTypes(), callParameterlessMethod("triggerMixedTypes"));
    }

    @Test
    public void test2Darrays() {
        assertEquals(TransformedMethodContract.trigger2Darrays(), callParameterlessMethod("trigger2Darrays"));
    }

    @Test
    public void test3Darrays() {
        assertEquals(TransformedMethodContract.trigger3Darrays(), callParameterlessMethod("trigger3Darrays"));
    }

    @Test
    public void testMultiDimensionalPrimitiveToObjectTypeConversions() {
        assertEquals(TransformedMethodContract.triggerMultiDimPrimitiveToObjectArray(),
            callParameterlessMethod("triggerMultiDimPrimitiveToObjectArray"));
    }

    // ------------------------

    private int callParameterlessMethod(String methodName) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneInteger();
    }

    private double callDoubleMethod(String methodName, double d) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, d);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneDouble();
    }

    private double callMultipleDoublesMethod(Double d1, Double d2, double d3, double d4) {
        byte[] argData = ABIUtil.encodeMethodArguments("collapseDoubles", d1, d2, d3, d4);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneDouble();
    }

    private float callFloatMethod(String methodName, float f) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, f);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneFloat();
    }

    private long callLongMethod(String methodName, long l) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, l);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneLong();
    }

    private long callMultipleLongsMethod(Long l1, Long l2, long l3, long l4) {
        byte[] argData = ABIUtil.encodeMethodArguments("collapseLongs", l1, l2, l3, l4);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneLong();
    }

    private int callIntMethod(String methodName, int i) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, i);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneInteger();
    }

    private int callMultipleIntsMethod(Integer i1, Integer i2, int i3, int i4) {
        byte[] argData = ABIUtil.encodeMethodArguments("collapseInts", i1, i2, i3, i4);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneInteger();
    }

    private short callShortMethod(String methodName, short s) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, s);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneShort();
    }

    private short callMultipleShortsMethod(Short s1, Short s2, short s3, short s4) {
        byte[] argData = ABIUtil.encodeMethodArguments("collapseShorts", s1, s2, s3, s4);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneShort();
    }

    private char callCharMethod(String methodName, char b) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, b);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneCharacter();
    }

    private char callMultipleCharsMethod(Character c1, Character c2, char c3, char c4) {
        byte[] argData = ABIUtil.encodeMethodArguments("collapseChars", c1, c2, c3, c4);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneCharacter();
    }

    private byte callByteMethod(String methodName, byte b) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, b);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneByte();
    }

    private byte callMultipleBytesMethod(Byte b1, Byte b2, byte b3, byte b4) {
        byte[] argData = ABIUtil.encodeMethodArguments("collapseBytes", b1, b2, b3, b4);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneByte();
    }

    private boolean callBooleanMethod(String methodName, boolean b) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, b);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneBoolean();
    }

    private boolean callMultipleBooleansMethod(Boolean b1, Boolean b2, boolean b3, boolean b4) {
        byte[] argData = ABIUtil.encodeMethodArguments("collapseBools", b1, b2, b3, b4);
        return new ABIDecoder(runTransaction(argData).getReturnData()).decodeOneBoolean();
    }

    private AvmTransactionResult runTransaction(byte[] argData) {
        AvmTransactionResult result = avmRule.call(deployer, dappAddress, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return result;
    }

}
