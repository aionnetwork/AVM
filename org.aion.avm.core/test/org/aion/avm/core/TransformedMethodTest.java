package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that methods are transformed correctly so that all of the expected behaviour in Java is
 * replicated properly. Things like method overloading, array type hierarchies etc.
 */
public class TransformedMethodTest {
    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    private static Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private static byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private static Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);
    private static Address dappAddress;

    @BeforeClass
    public static void setupClass() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(TransformedMethodContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        // Deploy.
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult createResult = avm.run(new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        dappAddress = TestingHelper.buildAddress(createResult.getReturnData());
    }

    @AfterClass
    public static void tearDownClass() {
        avm.shutdown();
    }

    @Test
    public void testCallNothing() {
        byte[] argData = ABIEncoder.encodeMethodArguments("nothing");
        Transaction call = Transaction.call(deployer, dappAddress.unwrap(), kernel.getNonce(deployer), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = avm.run(new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
    }

    @Test
    public void testPrimitiveBooleanParam() {
        assertEquals(TransformedMethodContract.tryPrimitiveBool(true),
            callBooleanMethod("tryPrimitiveBool", true));
        assertEquals(TransformedMethodContract.tryPrimitiveBool(false),
            callBooleanMethod("tryPrimitiveBool", false));
    }

    @Test
    public void testObjectBooleanParam() {
        assertEquals(TransformedMethodContract.tryObjectBool(Boolean.FALSE),
            callBooleanMethod("tryObjectBool", Boolean.FALSE));
        assertEquals(TransformedMethodContract.tryObjectBool(Boolean.TRUE),
            callBooleanMethod("tryObjectBool", Boolean.TRUE));
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
    public void testObjectByteParam() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            Byte expected = TransformedMethodContract.tryObjectByte((byte) i);
            Byte result = callByteMethod("tryObjectByte", (byte) i);
            assertEquals(expected, result);
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
    public void testObjectCharParam() {
        Character c = ' ';
        assertEquals(TransformedMethodContract.tryObjectChar(c), callCharMethod("tryObjectChar", c));
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
    public void testObjectShortParam() {
        Short s = (short) 22;
        assertEquals(TransformedMethodContract.tryObjectShort(s), callShortMethod("tryObjectShort", s));
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
    public void testObjectIntParam() {
        int i = -23523;
        assertEquals(TransformedMethodContract.tryObjectInt(i), callIntMethod("tryObjectInt", i));
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
    public void testObjectLongParam() {
        Long l = 23986523523L;
        assertEquals(TransformedMethodContract.tryObjectLong(l), callLongMethod("tryObjectLong", l));
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
    public void testObjectDoubleParam() {
        Double d = 436d;
        assertEquals(TransformedMethodContract.tryObjectDouble(d), callDoubleMethod("tryObjectDouble", d), 0);
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
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        return (Integer) TestingHelper.decodeResult(runTransaction(argData));
    }

    private double callDoubleMethod(String methodName, double d) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, d);
        return (Double) TestingHelper.decodeResult(runTransaction(argData));
    }

    private double callMultipleDoublesMethod(Double d1, Double d2, double d3, double d4) {
        byte[] argData = ABIEncoder.encodeMethodArguments("collapseDoubles", d1, d2, d3, d4);
        return (Double) TestingHelper.decodeResult(runTransaction(argData));
    }

    private float callFloatMethod(String methodName, float f) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, f);
        return (Float) TestingHelper.decodeResult(runTransaction(argData));
    }

    private long callLongMethod(String methodName, long l) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, l);
        return (Long) TestingHelper.decodeResult(runTransaction(argData));
    }

    private long callMultipleLongsMethod(Long l1, Long l2, long l3, long l4) {
        byte[] argData = ABIEncoder.encodeMethodArguments("collapseLongs", l1, l2, l3, l4);
        return (Long) TestingHelper.decodeResult(runTransaction(argData));
    }

    private int callIntMethod(String methodName, int i) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, i);
        return (Integer) TestingHelper.decodeResult(runTransaction(argData));
    }

    private int callMultipleIntsMethod(Integer i1, Integer i2, int i3, int i4) {
        byte[] argData = ABIEncoder.encodeMethodArguments("collapseInts", i1, i2, i3, i4);
        return (Integer) TestingHelper.decodeResult(runTransaction(argData));
    }

    private short callShortMethod(String methodName, short s) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, s);
        return (Short) TestingHelper.decodeResult(runTransaction(argData));
    }

    private short callMultipleShortsMethod(Short s1, Short s2, short s3, short s4) {
        byte[] argData = ABIEncoder.encodeMethodArguments("collapseShorts", s1, s2, s3, s4);
        return (Short) TestingHelper.decodeResult(runTransaction(argData));
    }

    private char callCharMethod(String methodName, char b) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, b);
        return (Character) TestingHelper.decodeResult(runTransaction(argData));
    }

    private char callMultipleCharsMethod(Character c1, Character c2, char c3, char c4) {
        byte[] argData = ABIEncoder.encodeMethodArguments("collapseChars", c1, c2, c3, c4);
        return (Character) TestingHelper.decodeResult(runTransaction(argData));
    }

    private byte callByteMethod(String methodName, byte b) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, b);
        return (Byte) TestingHelper.decodeResult(runTransaction(argData));
    }

    private Byte callMultipleBytesMethod(Byte b1, Byte b2, byte b3, byte b4) {
        byte[] argData = ABIEncoder.encodeMethodArguments("collapseBytes", b1, b2, b3, b4);
        return (Byte) TestingHelper.decodeResult(runTransaction(argData));
    }

    private boolean callBooleanMethod(String methodName, boolean b) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, b);
        return (Boolean) TestingHelper.decodeResult(runTransaction(argData));
    }

    private boolean callMultipleBooleansMethod(Boolean b1, Boolean b2, boolean b3, boolean b4) {
        byte[] argData = ABIEncoder.encodeMethodArguments("collapseBools", b1, b2, b3, b4);
        return (Boolean) TestingHelper.decodeResult(runTransaction(argData));
    }

    private TransactionResult runTransaction(byte[] argData) {
        Transaction call = Transaction.call(deployer, dappAddress.unwrap(), kernel.getNonce(deployer), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = avm.run(new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return result;
    }

}
