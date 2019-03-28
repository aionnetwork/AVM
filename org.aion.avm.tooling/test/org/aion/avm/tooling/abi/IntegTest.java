package org.aion.avm.tooling.abi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Rule;
import org.junit.Test;

public class IntegTest {

    @Rule
    public AvmRule avmRule = new AvmRule(true);

    private static final long ENERGY_LIMIT = 2_000_000L;
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
        assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return new Address(createResult.getReturnData());
    }

    private Object callStatic(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, arguments);
        TransactionResult result =
                avmRule.call(
                        avmRule.getPreminedAccount(),
                        dapp,
                        BigInteger.ZERO,
                        argData,
                        ENERGY_LIMIT,
                        ENERGY_PRICE)
                        .getTransactionResult();
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        if (result.getReturnData() != null) {
            return ABIDecoder.decodeOneObject(result.getReturnData());
        } else {
            return null;
        }
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
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertArrayEquals(new byte[0], result.getReturnData());
    }

    //Simplest case.
    @Test
    public void testSimpleDApp() {

        byte[] jar = avmRule.getDappBytes(DAppNoMainWithFallbackTarget.class, new byte[0]);
        Address dapp = installTestDApp(jar);

        boolean ret = (Boolean) callStatic(dapp, "test1", true);
        assertTrue(ret);

        ret = (Boolean) callStatic(dapp, "test2", 1, "test2", new long[]{1, 2, 3});
        assertTrue(ret);

        balanceTransfer(dapp);
    }

    //Multiple classes. One class is imported.
    @Test
    public void testChattyCalculator() {

        byte[] jar =
                avmRule.getDappBytes(ChattyCalculatorTarget.class, new byte[0], SilentCalculatorTarget.class);
        Address dapp = installTestDApp(jar);

        String ret = (String) callStatic(dapp, "amIGreater", 3, 4);
        assertEquals("No, 3, you are NOT greater than 4", ret);
        ret = (String) callStatic(dapp, "amIGreater", 5, 4);
        assertEquals("Yes, 5, you are greater than 4", ret);
    }

    //One complicated class with multiple types of arguments, multiple types of return values, complicated function body.
    @Test
    public void testComplicatedDApp() {

        byte[] jar = avmRule.getDappBytes(TestDAppTarget.class, new byte[0]);

        Address dapp = installTestDApp(jar);

        String ret = (String) callStatic(dapp, "returnHelloWorld");
        assertEquals("Hello world", ret);

        ret = (String) callStatic(dapp, "returnGoodbyeWorld");
        assertEquals("Goodbye world", ret);

        ret = (String) callStatic(dapp, "returnEcho", "Code meets world");
        assertEquals("Code meets world", ret);

        Address addr = new Address(Helpers.randomAddress().toBytes());

        Address retAddr = (Address) callStatic(dapp, "returnEchoAddress", addr);
        assertEquals(addr, retAddr);

        ret = (String) callStatic(dapp, "returnAppended", "alpha", "bet");
        assertEquals("alphabet", ret);

        ret = (String) callStatic(dapp, "returnAppendedMultiTypes", "alpha", "bet", false, 123);
        assertEquals("alphabetfalse123", ret);

        int[] expectedArray = new int[] {1,2,3};

        int[] intArray = (int[]) callStatic(dapp, "returnArrayOfInt", 1, 2, 3);
        assertArrayEquals(expectedArray, intArray);

        intArray = (int[]) callStatic(dapp, "returnArrayOfIntEcho", expectedArray);
        assertArrayEquals(expectedArray, intArray);

        int[][] expectedArray2D = new int[][]{{1, 2},{3, 4}};

        int[][] intArray2D = (int[][]) callStatic(dapp, "returnArrayOfInt2D", 1, 2, 3, 4);
        assertArrayEquals(expectedArray2D, intArray2D);

        intArray2D = (int[][]) callStatic(dapp, "returnArrayOfInt2DEcho", new Object[]{intArray2D});
        assertArrayEquals(expectedArray2D, intArray2D);

        String[] strArray = (String[]) callStatic(dapp, "returnArrayOfString", "hello", "world", "!");
        assertArrayEquals(new String[]{"hello", "world", "!"}, strArray);

        callStatic(dapp, "doNothing");
    }

    @Test
    public void testFallbackSuccess() {
        byte[] jar =
            avmRule.getDappBytes(DAppNoMainWithFallbackTarget.class, new byte[0]);

        Address dapp = installTestDApp(jar);

        int oldVal = (Integer) callStatic(dapp, "getValue");
        callStatic(dapp, "garbageMethod", 7);
        int newVal = (Integer) callStatic(dapp, "getValue");

        assertEquals(oldVal + 10, newVal);
        callStatic(dapp, "", 7);

        newVal = (Integer) callStatic(dapp, "getValue");
        assertEquals(oldVal + 20, newVal);
    }

    @Test
    public void testFallbackFail() {
        byte[] jar =
            avmRule.getDappBytes(DAppNoMainNoFallbackTarget.class, new byte[0]);
        Address dapp = installTestDApp(jar);


        byte[] argData = ABIEncoder.encodeMethodArguments("noSuchMethod");
        TransactionResult result =
            avmRule.call(
                avmRule.getPreminedAccount(),
                dapp,
                BigInteger.ZERO,
                argData,
                ENERGY_LIMIT,
                ENERGY_PRICE)
                .getTransactionResult();
        assertEquals(Code.FAILED_REVERT, result.getResultCode());
        assertNull(result.getReturnData());
    }
}
