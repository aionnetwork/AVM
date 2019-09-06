package org.aion.avm.embed.abi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import avm.Address;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.types.TransactionResult;
import org.junit.ClassRule;
import org.junit.Test;

public class IntegTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 3_500_000L;
    private static final long ENERGY_PRICE = 1L;

    private Address installTestDApp(byte[] jar) {

        // Deploy.
        TransactionResult createResult =
                avmRule.deploy(
                        avmRule.getPreminedAccount(),
                        BigInteger.ZERO,
                        jar,
                        ENERGY_LIMIT,
                        ENERGY_PRICE)
                        .getTransactionResult();
        assertTrue(createResult.transactionStatus.isSuccess());
        return new Address(createResult.copyOfTransactionOutput().orElseThrow());
    }

    private boolean callStaticBoolean(Address dapp, String methodName, Object... arguments) {
        byte[] result = callStaticResult(dapp, methodName, arguments);
        return new ABIDecoder(result).decodeOneBoolean();
    }

    private int callStaticInteger(Address dapp, String methodName, Object... arguments) {
        byte[] result = callStaticResult(dapp, methodName, arguments);
        return new ABIDecoder(result).decodeOneInteger();
    }

    private String callStaticString(Address dapp, String methodName, Object... arguments) {
        byte[] result = callStaticResult(dapp, methodName, arguments);
        return new ABIDecoder(result).decodeOneString();
    }

    private void callStaticVoid(Address dapp, String methodName, Object... arguments) {
        byte[] result = callStaticResult(dapp, methodName, arguments);
        assertArrayEquals(new byte[0], result);
    }

    private byte[] callStaticResult(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, arguments);
        TransactionResult result =
                avmRule.call(
                        avmRule.getPreminedAccount(),
                        dapp,
                        BigInteger.ZERO,
                        argData,
                        ENERGY_LIMIT,
                        ENERGY_PRICE)
                        .getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
        return result.copyOfTransactionOutput().orElseThrow();
    }

    private void balanceTransfer(Address dapp) {
        TransactionResult result =
            avmRule.call(
                avmRule.getPreminedAccount(),
                dapp,
                BigInteger.ZERO,
                new byte[0],
                ENERGY_LIMIT,
                ENERGY_PRICE)
                .getTransactionResult();
        assertTrue(result.transactionStatus.isSuccess());
        assertArrayEquals(new byte[0], result.copyOfTransactionOutput().orElseThrow());
    }

    //Simplest case.
    @Test
    public void testSimpleDApp() {

        byte[] jar = avmRule.getDappBytes(DAppNoMainWithFallbackTarget.class, new byte[0]);
        Address dapp = installTestDApp(jar);

        boolean ret = callStaticBoolean(dapp, "test1", true);
        assertTrue(ret);

        ret = callStaticBoolean(dapp, "test2", 1, "test2", new long[]{1, 2, 3});
        assertTrue(ret);

        balanceTransfer(dapp);
    }

    //Multiple classes. One class is imported.
    @Test
    public void testChattyCalculator() {

        byte[] jar =
                avmRule.getDappBytes(ChattyCalculatorTarget.class, new byte[0], SilentCalculatorTarget.class);
        Address dapp = installTestDApp(jar);

        String ret = callStaticString(dapp, "amIGreater", 3, 4);
        assertEquals("No, 3, you are NOT greater than 4", ret);
        ret = callStaticString(dapp, "amIGreater", 5, 4);
        assertEquals("Yes, 5, you are greater than 4", ret);
    }

    //One complicated class with multiple types of arguments, multiple types of return values, complicated function body.
    @Test
    public void testComplicatedDApp() {

        byte[] jar = avmRule.getDappBytes(TestDAppTarget.class, new byte[0]);

        Address dapp = installTestDApp(jar);

        String ret = callStaticString(dapp, "returnHelloWorld");
        assertEquals("Hello world", ret);

        ret = callStaticString(dapp, "returnGoodbyeWorld");
        assertEquals("Goodbye world", ret);

        ret = callStaticString(dapp, "returnEcho", "Code meets world");
        assertEquals("Code meets world", ret);

        Address addr = new Address(Helpers.randomAddress().toByteArray());

        Address retAddr = new ABIDecoder(callStaticResult(dapp, "returnEchoAddress", addr)).decodeOneAddress();
        assertEquals(addr, retAddr);

        ret = callStaticString(dapp, "returnAppended", "alpha", "bet");
        assertEquals("alphabet", ret);

        ret = callStaticString(dapp, "returnAppendedMultiTypes", "alpha", "bet", false, 123);
        assertEquals("alphabetfalse123", ret);

        int[] expectedArray = new int[] {1,2,3};

        int[] intArray = new ABIDecoder(callStaticResult(dapp, "returnArrayOfInt", 1, 2, 3)).decodeOneIntegerArray();
        assertArrayEquals(expectedArray, intArray);

        intArray = new ABIDecoder(callStaticResult(dapp, "returnArrayOfIntEcho", expectedArray)).decodeOneIntegerArray();
        assertArrayEquals(expectedArray, intArray);

        int[][] expectedArray2D = new int[][]{{1, 2},{3, 4}};

        int[][] intArray2D = new ABIDecoder(callStaticResult(dapp, "returnArrayOfInt2D", 1, 2, 3, 4)).decodeOne2DIntegerArray();
        assertArrayEquals(expectedArray2D, intArray2D);

        intArray2D = new ABIDecoder(callStaticResult(dapp, "returnArrayOfInt2DEcho", new Object[]{intArray2D})).decodeOne2DIntegerArray();
        assertArrayEquals(expectedArray2D, intArray2D);

        String[] strArray = new ABIDecoder(callStaticResult(dapp, "returnArrayOfString", "hello", "world", "!")).decodeOneStringArray();
        assertArrayEquals(new String[]{"hello", "world", "!"}, strArray);

        callStaticVoid(dapp, "doNothing");
    }

    @Test
    public void testFallbackSuccess() {
        byte[] jar =
            avmRule.getDappBytes(DAppNoMainWithFallbackTarget.class, new byte[0]);

        Address dapp = installTestDApp(jar);

        int oldVal = callStaticInteger(dapp, "getValue");
        callStaticVoid(dapp, "garbageMethod", 7);
        int newVal = callStaticInteger(dapp, "getValue");

        assertEquals(oldVal + 10, newVal);
        callStaticVoid(dapp, "", 7);

        newVal = callStaticInteger(dapp, "getValue");
        assertEquals(oldVal + 20, newVal);
    }

    @Test
    public void testFallbackFail() {
        byte[] jar =
            avmRule.getDappBytes(DAppNoMainNoFallbackTarget.class, new byte[0]);
        Address dapp = installTestDApp(jar);


        byte[] argData = ABIUtil.encodeMethodArguments("noSuchMethod");
        TransactionResult result =
            avmRule.call(
                avmRule.getPreminedAccount(),
                dapp,
                BigInteger.ZERO,
                argData,
                ENERGY_LIMIT,
                ENERGY_PRICE)
                .getTransactionResult();
        assertTrue(result.transactionStatus.isReverted());
        assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    @Test
    public void testClinitGeneration() {
        byte[] deploymentArgs = ABIUtil.encodeDeploymentArguments(5, "hello");
        byte[] jar =
            avmRule.getDappBytes(DAppNoClinitTarget.class, deploymentArgs);

        Address dapp = installTestDApp(jar);

        int intResult = callStaticInteger(dapp, "getInt");
        assertEquals(5, intResult);

        String stringResult = callStaticString(dapp, "getString");
        assertEquals("hello", stringResult);

        jar =
            avmRule.getDappBytes(StaticInitializersTarget.class, deploymentArgs, SilentCalculatorTarget.class);

        dapp = installTestDApp(jar);

        intResult = callStaticInteger(dapp, "getInt");
        // this value should be 10, since the class's static initializer should override the deployment arg
        assertEquals(10, intResult);

        stringResult = callStaticString(dapp, "getString");
        assertEquals("hello", stringResult);
    }

}
