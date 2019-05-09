package org.aion.avm.core.testCharSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IdentifierTest {

    private long energyPrice = 1L;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(),
        System.currentTimeMillis(), new byte[0]);

    private org.aion.types.Address deployer = TestingKernel.PREMINED_ADDRESS;
    private org.aion.types.Address dappAddress;

    private TestingKernel kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        this.kernel = new TestingKernel(block);
        this.avm = CommonAvmFactory
            .buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testCharSet() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(Identifier.class);
        long energyLimit = 10_000_000L;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();

        dappAddress = org.aion.types.Address.wrap(txResult.getReturnData());
        assertNotNull(dappAddress);

        byte[] argData = ABIUtil.encodeMethodArguments("sayHelloEN");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("Hello!".getBytes(), txResult.getReturnData());

        argData = ABIUtil.encodeMethodArguments("sayHelloTC");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("哈囉!".getBytes(), txResult.getReturnData());

        argData = ABIUtil.encodeMethodArguments("sayHelloExtendChar");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();

        char[] charArray = new char[]{'n', 'i', '\\', '3', '6', '1', 'o', '!'};
        assertArrayEquals(String.valueOf(charArray).getBytes(), txResult.getReturnData());

        argData = ABIUtil.encodeMethodArguments("sayHelloExtendChar2");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("����!".getBytes(), txResult.getReturnData());

        argData = ABIUtil.encodeMethodArguments("sayHelloExtendChar3");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("sayHelloÿ!".getBytes(), txResult.getReturnData());

        argData = ABIUtil.encodeMethodArguments("ÿ");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("ÿÿÿÿ!".getBytes(), txResult.getReturnData());

        argData = ABIUtil.encodeMethodArguments("哈囉");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("哈囉!".getBytes(), txResult.getReturnData());
    }

    @Test
    public void testClassNaming() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(哈哈ÿ.class);
        long energyLimit = 10_000_000L;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();

        dappAddress = org.aion.types.Address.wrap(txResult.getReturnData());
        assertNotNull(dappAddress);

        byte[] argData = ABIUtil.encodeMethodArguments("callInnerClass1");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("哈囉!".getBytes(), txResult.getReturnData());

        argData = ABIUtil.encodeMethodArguments("callInnerClass2");

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("ÿ!".getBytes(), txResult.getReturnData());
    }

    @Test
    public void testInvalidUtf8Code() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(哈哈ÿ.class);
        long energyLimit = 10_000_000L;
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO,
            new CodeAndArguments(jar, null).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        dappAddress = org.aion.types.Address.wrap(txResult.getReturnData());
        assertNotNull(dappAddress);

        byte[] invalidCode = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff};

        String methodName = new String(invalidCode);

        byte[] argData = ABIUtil.encodeMethodArguments(methodName);

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("Invalid method name!".getBytes(), txResult.getReturnData());

        invalidCode = new byte[]{(byte) 0xf1, (byte) 0xf0, (byte) 0xfa, (byte) 0xfb,
            (byte) 0xfc, (byte) 0xfd};

        methodName = new String(invalidCode);

        argData = ABIUtil.encodeMethodArguments(methodName);

        tx = Transaction
            .call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, argData,
                energyLimit, energyPrice);
        txResult = avm.run(this.kernel, new Transaction[]{tx})[0].get();
        assertArrayEquals("Invalid method name!".getBytes(), txResult.getReturnData());
    }
}
