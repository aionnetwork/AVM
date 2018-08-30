package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.util.CodeAndArguments;
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
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 5000000;
    private long energyPrice = 1;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    public TransactionResult deployHelloWorld() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        byte[] txData = new CodeAndArguments(jar, null).encodeToBytes();

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, null, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return avm.run(context);
    }

    public TransactionResult deployTheDeployAndRunTest() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.deployAndRunTest.jar");
        byte[] txData = new CodeAndArguments(jar, null).encodeToBytes();

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, null, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return avm.run(context);
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
        byte[] txData = new CodeAndArguments(jar, arguments).encodeToBytes();

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, null, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
    }

    @Test
    public void testDeployAndRun() {
        TransactionResult deployResult = deployHelloWorld();

        // call the "run" method
        byte[] txData = ABIEncoder.encodeMethodArguments("run");
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals("Hello, world!", new String((byte[]) TestingHelper.decodeResult(result)));
    }

    @Test
    public void testDeployAndRunWithArgs() {
        TransactionResult deployResult = deployHelloWorld();

        // test another method call, "add" with arguments
        byte[] txData = ABIEncoder.encodeMethodArguments("add", 123, 1);
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(124, TestingHelper.decodeResult(result));
    }

    @Test
    public void testDeployAndRunTest() {
        TransactionResult deployResult = deployTheDeployAndRunTest();
        assertEquals(TransactionResult.Code.SUCCESS, deployResult.getStatusCode());

        // test encode method arguments with "encodeArgs"
        byte[] txData = ABIEncoder.encodeMethodArguments("encodeArgs");
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        byte[] expected = ABIEncoder.encodeMethodArguments("addArray", new int[]{123, 1}, 5);
        boolean correct = Arrays.equals((byte[])(TestingHelper.decodeResult(result)), expected);
        assertEquals(true, correct);

        // test another method call, "addArray" with 1D array arguments
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, expected, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(129, TestingHelper.decodeResult(result));

        // test another method call, "addArray2" with 2D array arguments
        int[][] a = new int[2][];
        a[0] = new int[]{123, 4};
        a[1] = new int[]{1, 2};
        txData = ABIEncoder.encodeMethodArguments("addArray2", TestingHelper.construct2DWrappedArray(a));
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(124, TestingHelper.decodeResult(result));

        // test another method call, "concatenate" with 2D array arguments and 1D array return data
        char[][] chars = new char[2][];
        chars[0] = "cat".toCharArray();
        chars[1] = "dog".toCharArray();
        txData = ABIEncoder.encodeMethodArguments("concatenate", TestingHelper.construct2DWrappedArray(chars));
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals("catdog", new String((char[]) TestingHelper.decodeResult(result)));

        // test another method call, "swap" with 2D array arguments and 2D array return data
        txData = ABIEncoder.encodeMethodArguments("swap", TestingHelper.construct2DWrappedArray(chars));
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals("dog", new String(((char[][]) TestingHelper.decodeResult(result))[0]));
        assertEquals("cat", new String(((char[][]) TestingHelper.decodeResult(result))[1]));

        // test a method call to "setBar", which does not have a return type (void)
        txData = ABIEncoder.encodeMethodArguments("setBar", 20);
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(context);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
    }
}
