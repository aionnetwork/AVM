package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.api.Address;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AvmImplDeployAndRunTest {
    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 5000000;
    private long energyPrice = 1;

    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    public TransactionResult deployHelloWorld() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        byte[] txData = Helpers.encodeCodeAndData(jar, null);

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return new AvmImpl(new KernelInterfaceImpl()).run(context);
    }

    public TransactionResult deployTheDeployAndRunTest() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.deployAndRunTest.jar");
        byte[] txData = Helpers.encodeCodeAndData(jar, null);

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return new AvmImpl(new KernelInterfaceImpl()).run(context);
    }

    @Test
    public void testDeploy() {
        TransactionResult result = deployHelloWorld();

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
    }

    @Test
    public void testDeployWithMethodCall() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        byte[] arguments = ABIEncoder.encodeMethodArguments("", 100);
        byte[] txData = Helpers.encodeCodeAndData(jar, arguments);

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
    }

    @Test
    public void testDeployAndRun() throws InvalidTxDataException {
        TransactionResult deployResult = deployHelloWorld();

        // call the "run" method
        byte[] txData = ABIEncoder.encodeMethodArguments("run");
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals("Hello, world!", new String((byte[]) TestingHelper.decodeResult(result)));
    }

    @Test
    public void testDeployAndRunWithArgs() throws InvalidTxDataException {
        TransactionResult deployResult = deployHelloWorld();

        // test another method call, "add" with arguments
        byte[] txData = ABIEncoder.encodeMethodArguments("add", 123, 1);
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(124, TestingHelper.decodeResult(result));
    }

    @Test
    public void testDeployAndRunTest() throws InvalidTxDataException {
        TransactionResult deployResult = deployTheDeployAndRunTest();
        assertEquals(TransactionResult.Code.SUCCESS, deployResult.getStatusCode());

        // test encode method arguments with "encodeArgs"
        byte[] txData = ABIEncoder.encodeMethodArguments("encodeArgs");
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        byte[] expected = ABIEncoder.encodeMethodArguments("addArray", new int[]{123, 1}, 5);
        boolean correct = Arrays.equals((byte[])(TestingHelper.decodeResult(result)), expected);
        assertEquals(true, correct);

        // test another method call, "addArray" with 1D array arguments
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, expected, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(129, TestingHelper.decodeResult(result));

        // test another method call, "addArray2" with 2D array arguments
        int[][] a = new int[2][];
        a[0] = new int[]{123, 4};
        a[1] = new int[]{1, 2};
        txData = ABIEncoder.encodeMethodArguments("addArray2", TestingHelper.construct2DWrappedArray(a));
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(124, TestingHelper.decodeResult(result));

        // test another method call, "concatenate" with 2D array arguments and 1D array return data
        char[][] chars = new char[2][];
        chars[0] = "cat".toCharArray();
        chars[1] = "dog".toCharArray();
        txData = ABIEncoder.encodeMethodArguments("concatenate", TestingHelper.construct2DWrappedArray(chars));
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals("catdog", new String((char[]) TestingHelper.decodeResult(result)));

        // test another method call, "swap" with 2D array arguments and 2D array return data
        txData = ABIEncoder.encodeMethodArguments("swap", TestingHelper.construct2DWrappedArray(chars));
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals("dog", new String(((char[][]) TestingHelper.decodeResult(result))[0]));
        assertEquals("cat", new String(((char[][]) TestingHelper.decodeResult(result))[1]));
    }
}
