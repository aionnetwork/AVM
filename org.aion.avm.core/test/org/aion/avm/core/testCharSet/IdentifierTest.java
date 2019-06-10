package org.aion.avm.core.testCharSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.*;

public class IdentifierTest {

    private static long energyPrice = 1L;
    private static TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(),
        System.currentTimeMillis(), new byte[0]);

    private static org.aion.types.Address deployer = TestingKernel.PREMINED_ADDRESS;
    private static org.aion.types.Address dappAddress;

    private static TestingKernel kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        kernel = new TestingKernel(block);
        avm = CommonAvmFactory
            .buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testCharSet() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(Identifier.class);
        long energyLimit = 10_000_000L;
        TestingTransaction tx = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();

        dappAddress = org.aion.types.Address.wrap(txResult.getReturnData());
        assertNotNull(dappAddress);

        byte[] argData = encodeNoArgsMethodCall("sayHelloEN");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("Hello!".getBytes(), txResult.getReturnData());

        argData = encodeNoArgsMethodCall("sayHelloTC");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("哈囉!".getBytes(), txResult.getReturnData());

        argData = encodeNoArgsMethodCall("sayHelloExtendChar");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();

        char[] charArray = new char[]{'n', 'i', '\\', '3', '6', '1', 'o', '!'};
        assertArrayEquals(String.valueOf(charArray).getBytes(), txResult.getReturnData());

        argData = encodeNoArgsMethodCall("sayHelloExtendChar2");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("����!".getBytes(), txResult.getReturnData());

        argData = encodeNoArgsMethodCall("sayHelloExtendChar3");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("sayHelloÿ!".getBytes(), txResult.getReturnData());

        argData = encodeNoArgsMethodCall("ÿ");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("ÿÿÿÿ!".getBytes(), txResult.getReturnData());

        argData = encodeNoArgsMethodCall("哈囉");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("哈囉!".getBytes(), txResult.getReturnData());
    }

    @Test
    public void testClassNaming() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(哈哈ÿ.class);
        long energyLimit = 10_000_000L;
        TestingTransaction tx = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();

        dappAddress = org.aion.types.Address.wrap(txResult.getReturnData());
        assertNotNull(dappAddress);

        byte[] argData = encodeNoArgsMethodCall("callInnerClass1");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("哈囉!".getBytes(), txResult.getReturnData());

        argData = encodeNoArgsMethodCall("callInnerClass2");

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("ÿ!".getBytes(), txResult.getReturnData());
    }

    @Test
    public void testInvalidUtf8Code() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(哈哈ÿ.class);
        long energyLimit = 10_000_000L;
        TestingTransaction tx = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        dappAddress = org.aion.types.Address.wrap(txResult.getReturnData());
        assertNotNull(dappAddress);

        byte[] invalidCode = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff};

        String methodName = new String(invalidCode);

        byte[] argData = encodeNoArgsMethodCall(methodName);

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("Invalid method name!".getBytes(), txResult.getReturnData());

        invalidCode = new byte[]{(byte) 0xf1, (byte) 0xf0, (byte) 0xfa, (byte) 0xfb,
            (byte) 0xfc, (byte) 0xfd};

        methodName = new String(invalidCode);

        argData = encodeNoArgsMethodCall(methodName);

        tx = TestingTransaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[]{tx})[0].get();
        assertArrayEquals("Invalid method name!".getBytes(), txResult.getReturnData());
    }


    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}
