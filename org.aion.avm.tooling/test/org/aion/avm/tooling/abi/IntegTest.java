package org.aion.avm.tooling.abi;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.tooling.AvmRule;
import org.aion.kernel.AvmTransactionResult;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class IntegTest {

    @Rule
    public AvmRule avmRule = new AvmRule(true);

    private static ABICompiler compiler;

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    @Before
    public void setup() {
        compiler = new ABICompiler();
    }

    private Address installTestDApp() {

        byte[] jar =
                JarBuilder.buildJarForExplicitClassNamesAndBytecode(
                        compiler.getMainClassName(),
                        compiler.getMainClassBytes(),
                        compiler.getClassMap());
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        // Deploy.
        TransactionResult createResult =
                avmRule.deploy(
                        avmRule.getPreminedAccount(),
                        BigInteger.ZERO,
                        txData,
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

    //Simplest case.
    @Test
    public void testSimpleDApp() {

        byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppNoMainWithFallbackTarget.class);
        compiler.compile(new ByteArrayInputStream(jar));
        Address dapp = installTestDApp();

        boolean ret = (Boolean) callStatic(dapp, "test1", true);
        assertTrue(ret);

        ret = (Boolean) callStatic(dapp, "test2", 1, "test2", new long[]{1, 2, 3});
        assertTrue(ret);
    }

    //Multiple classes. One class is imported.
    @Test
    public void testChattyCalculator() {

        byte[] jar =
                JarBuilder.buildJarForMainAndClasses(ChattyCalculatorTarget.class, SilentCalculatorTarget.class);
        compiler.compile(new ByteArrayInputStream(jar));
        Address dapp = installTestDApp();

        String ret = (String) callStatic(dapp, "amIGreater", 3, 4);
        assertEquals("No, 3, you are NOT greater than 4", ret);
        ret = (String) callStatic(dapp, "amIGreater", 5, 4);
        assertEquals("Yes, 5, you are greater than 4", ret);
    }

    //One complicated class with multiple types of arguments, multiple types of return values, complicated function body.
    @Test
    public void testComplicatedDApp() {

        byte[] jar = JarBuilder.buildJarForMainAndClasses(TestDAppTarget.class);
        compiler.compile(new ByteArrayInputStream(jar));

        TestHelpers.saveMainClassInABICompiler(compiler);



        Address dapp = installTestDApp();

        String ret = (String) callStatic(dapp, "returnHelloWorld");
        assertEquals("Hello world", ret);

        ret = (String) callStatic(dapp, "returnGoodbyeWorld");
        assertEquals("Goodbye world", ret);

        ret = (String) callStatic(dapp, "returnEcho", "Code meets world");
        assertEquals("Code meets world", ret);

        ret = (String) callStatic(dapp, "returnAppended", "alpha", "bet");
        assertEquals("alphabet", ret);

        ret = (String) callStatic(dapp, "returnAppendedMultiTypes", "alpha", "bet", false, 123);
        assertEquals("alphabetfalse123", ret);

        int[] intArray = (int[]) callStatic(dapp, "returnArrayOfInt", 1, 2, 3);
        assertArrayEquals(new int[]{1, 2, 3}, intArray);

        String[] strArray = (String[]) callStatic(dapp, "returnArrayOfString", "hello", "world", "!");
        assertArrayEquals(new String[]{"hello", "world", "!"}, strArray);
    }

    @Test
    public void testFallbackSuccess() {
        byte[] jar =
            JarBuilder.buildJarForMainAndClasses(DAppNoMainWithFallbackTarget.class);
        compiler.compile(new ByteArrayInputStream(jar));
        Address dapp = installTestDApp();

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
            JarBuilder.buildJarForMainAndClasses(DAppNoMainNoFallbackTarget.class);
        compiler.compile(new ByteArrayInputStream(jar));
        Address dapp = installTestDApp();

        assertNull(callStatic(dapp, "noSuchMethod"));

    }

}
