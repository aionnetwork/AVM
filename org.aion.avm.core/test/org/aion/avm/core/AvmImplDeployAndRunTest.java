package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.Transaction;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import legacy_examples.deployAndRunTest.DeployAndRunTarget;
import legacy_examples.helloworld.HelloWorld;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


public class AvmImplDeployAndRunTest {
    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private long energyLimit = 5000000;
    private long energyPrice = 1;

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private KernelInterfaceImpl kernel;
    private VirtualMachine avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    public TransactionResult deployHelloWorld() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(HelloWorld.class);
        byte[] txData = new CodeAndArguments(jar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    @Test
    public void testDeployWithClinitCall() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(HelloWorld.class);
        byte[] arguments = ABIEncoder.encodeMethodArguments("", 100);
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testDeployAndMethodCalls() {
        TransactionResult deployResult = deployHelloWorld();
        assertEquals(AvmTransactionResult.Code.SUCCESS, deployResult.getResultCode());

        // call the "run" method
        byte[] txData = ABIEncoder.encodeMethodArguments("run");
        Transaction tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals("Hello, world!", new String((byte[]) TestingHelper.decodeResult(result)));

        // test another method call, "add" with arguments
        txData = ABIEncoder.encodeMethodArguments("add", 123, 1);
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals(124, TestingHelper.decodeResult(result));
    }

    public TransactionResult deployTheDeployAndRunTest() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(DeployAndRunTarget.class);
        byte[] txData = new CodeAndArguments(jar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    @Test
    public void testDeployAndRunTest() {
        TransactionResult deployResult = deployTheDeployAndRunTest();
        assertEquals(AvmTransactionResult.Code.SUCCESS, deployResult.getResultCode());

        // test encode method arguments with "encodeArgs"
        byte[] txData = ABIEncoder.encodeMethodArguments("encodeArgs");
        Transaction tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        byte[] expected = ABIEncoder.encodeMethodArguments("addArray", new int[]{123, 1}, 5);
        boolean correct = Arrays.equals((byte[])(TestingHelper.decodeResult(result)), expected);
        assertEquals(true, correct);

        // test another method call, "addArray" with 1D array arguments
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, expected, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals(129, TestingHelper.decodeResult(result));

        // test another method call, "addArray2" with 2D array arguments
        int[][] a = new int[2][];
        a[0] = new int[]{123, 4};
        a[1] = new int[]{1, 2};
        txData = ABIEncoder.encodeMethodArguments("addArray2", (Object) a);
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals(124, TestingHelper.decodeResult(result));

        // test another method call, "concatenate" with 2D array arguments and 1D array return data
        char[][] chars = new char[2][];
        chars[0] = "cat".toCharArray();
        chars[1] = "dog".toCharArray();
        txData = ABIEncoder.encodeMethodArguments("concatenate", (Object) chars);
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals("catdog", new String((char[]) TestingHelper.decodeResult(result)));

        // test another method call, "concatString" with String array arguments and String return data
        txData = ABIEncoder.encodeMethodArguments("concatString", "cat", "dog"); // Note - need to cast String[] into Object, to pass it as one argument to the varargs method
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals("catdog", TestingHelper.decodeResult(result));

        // test another method call, "concatStringArray" with String array arguments and String return data
        txData = ABIEncoder.encodeMethodArguments("concatStringArray", (Object) new String[]{"cat", "dog"}); // Note - need to cast String[] into Object, to pass it as one argument to the varargs method
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals("catdog", ((String[])TestingHelper.decodeResult(result))[0]);
        assertEquals("perfect", ((String[])TestingHelper.decodeResult(result))[1]);

        // test another method call, "swap" with 2D array arguments and 2D array return data
        txData = ABIEncoder.encodeMethodArguments("swap", (Object) chars);
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals("dog", new String(((char[][]) TestingHelper.decodeResult(result))[0]));
        assertEquals("cat", new String(((char[][]) TestingHelper.decodeResult(result))[1]));

        // test a method call to "setBar", which does not have a return type (void)
        txData = ABIEncoder.encodeMethodArguments("setBar", 20);
        tx = Transaction.call(from, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testBalanceTransfer() {
        assertEquals(KernelInterfaceImpl.PREMINED_AMOUNT, kernel.getBalance(from));

        // account1 get 10000
        org.aion.vm.api.interfaces.Address account1 = Helpers.randomAddress();
        Transaction tx = Transaction.balanceTransfer(from, account1, kernel.getNonce(from).longValue(), BigInteger.valueOf(100000L), energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals(BigInteger.valueOf(100000L), kernel.getBalance(account1));

        // account1 transfers 1000 to account2
        org.aion.vm.api.interfaces.Address account2 = Helpers.randomAddress();
        tx = Transaction.balanceTransfer(account1, account2, kernel.getNonce(account1).longValue(), BigInteger.valueOf(1000L), energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals(BigInteger.valueOf(100000L - 1000L - energyPrice * 21000L), kernel.getBalance(account1));
        assertEquals(BigInteger.valueOf(1000L), kernel.getBalance(account2));
    }

    @Test
    public void testCreateAndCallWithBalanceTransfer() {
        // deploy the Dapp with 100000 value transfer; create with balance transfer
        byte[] jar = JarBuilder.buildJarForMainAndClasses(DeployAndRunTarget.class);
        byte[] txData = new CodeAndArguments(jar, null).encodeToBytes();

        Transaction tx = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.valueOf(100000L), txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult deployResult = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, deployResult.getResultCode());
        assertEquals(BigInteger.valueOf(100000L), kernel.getBalance(AvmAddress.wrap(deployResult.getReturnData())));

        // account1 get 300000; pure balance transfer
        org.aion.vm.api.interfaces.Address account1 = Helpers.randomAddress();
        tx = Transaction.balanceTransfer(from, account1, kernel.getNonce(from).longValue(), BigInteger.valueOf(300000L), energyPrice);
        context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals(BigInteger.valueOf(300000L), kernel.getBalance(account1));

        // account1 to call the Dapp and transfer 50000 to it; call with balance transfer
        txData = ABIEncoder.encodeMethodArguments("encodeArgs");
        tx = Transaction.call(account1, AvmAddress.wrap(deployResult.getReturnData()), kernel.getNonce(account1).longValue(), BigInteger.valueOf(50000L), txData, 200000L, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();

        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        assertEquals(BigInteger.valueOf(150000L), kernel.getBalance(AvmAddress.wrap(deployResult.getReturnData())));
        assertEquals(BigInteger.valueOf(300000L - 50000L - ((AvmTransactionResult) result).getEnergyUsed()), kernel.getBalance(account1));
    }
}
