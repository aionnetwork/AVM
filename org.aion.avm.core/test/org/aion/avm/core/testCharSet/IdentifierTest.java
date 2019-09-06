package org.aion.avm.core.testCharSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import org.aion.avm.core.*;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.types.TransactionResult;
import org.junit.*;

public class IdentifierTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;

    private static long energyPrice = 1L;

    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static AionAddress dappAddress;

    private static TestingState kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        AvmConfiguration config = new AvmConfiguration();
        // This test uses Blockchain.println so determine whether or not we want to see the output.
        config.enableBlockchainPrintln = REPORT;
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testCharSet() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(Identifier.class);
        long energyLimit = 10_000_000L;
        kernel.generateBlock();
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();

        dappAddress = new AionAddress(txResult.copyOfTransactionOutput().orElseThrow());
        assertNotNull(dappAddress);

        byte[] argData = encodeNoArgsMethodCall("sayHelloEN");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("Hello!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        argData = encodeNoArgsMethodCall("sayHelloTC");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("哈囉!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        argData = encodeNoArgsMethodCall("sayHelloExtendChar");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();

        char[] charArray = new char[]{'n', 'i', '\\', '3', '6', '1', 'o', '!'};
        assertArrayEquals(String.valueOf(charArray).getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        argData = encodeNoArgsMethodCall("sayHelloExtendChar2");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("����!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        argData = encodeNoArgsMethodCall("sayHelloExtendChar3");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("sayHelloÿ!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        argData = encodeNoArgsMethodCall("ÿ");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("ÿÿÿÿ!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        argData = encodeNoArgsMethodCall("哈囉");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("哈囉!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());
    }

    @Test
    public void testClassNaming() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(哈哈ÿ.class);
        long energyLimit = 10_000_000L;
        kernel.generateBlock();
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();

        dappAddress = new AionAddress(txResult.copyOfTransactionOutput().orElseThrow());
        assertNotNull(dappAddress);

        byte[] argData = encodeNoArgsMethodCall("callInnerClass1");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("哈囉!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        argData = encodeNoArgsMethodCall("callInnerClass2");

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("ÿ!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());
    }

    @Test
    public void testInvalidUtf8Code() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(哈哈ÿ.class);
        long energyLimit = 10_000_000L;
        kernel.generateBlock();
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        dappAddress = new AionAddress(txResult.copyOfTransactionOutput().orElseThrow());
        assertNotNull(dappAddress);

        byte[] invalidCode = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff};

        String methodName = new String(invalidCode);

        byte[] argData = encodeNoArgsMethodCall(methodName);

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("Invalid method name!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());

        invalidCode = new byte[]{(byte) 0xf1, (byte) 0xf0, (byte) 0xfa, (byte) 0xfb,
            (byte) 0xfc, (byte) 0xfd};

        methodName = new String(invalidCode);

        argData = encodeNoArgsMethodCall(methodName);

        kernel.generateBlock();
        tx = AvmTransactionUtil
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertArrayEquals("Invalid method name!".getBytes(), txResult.copyOfTransactionOutput().orElseThrow());
    }


    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}
