package org.aion.avm.core;

import org.aion.avm.core.util.Helpers;
import org.aion.avm.api.Address;
import org.aion.kernel.Block;
import org.aion.kernel.KernelApiImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AvmImplDeployAndRunTest {
    private static AvmImpl avm;
    private static KernelApiImpl cb;

    @BeforeClass
    public static void setupClass() {
        avm = new AvmImpl();
        cb = new KernelApiImpl();
    }

    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 5000000;

    Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    public TransactionResult deployHelloWorld() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, 0, jar, energyLimit);

        return avm.run(tx, block, cb);
    }

    public TransactionResult deployTheDeployAndRunTest() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.deployAndRunTest.jar");
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, 0, jar, energyLimit);

        return avm.run(tx, block, cb);
    }

    @Test
    public void testDeploy() {
        TransactionResult result = deployHelloWorld();

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
    }

/* deploy invocation is not supported for now
    @Test
    public void testDeployWithMethodCall() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01};
        IBlockchainRuntime rt = new SimpleRuntime(from, to, energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader, codeStorage);
        TransactionResult result = avm.deploy(jar, null, rt);

        assertEquals(TransactionResult.Code.SUCCESS, result.code);
    }*/

    @Test
    public void testDeployAndRun() {
        TransactionResult deployResult = deployHelloWorld();

        // call the "run" method
        byte[] txData = new byte[]{0x72, 0x75, 0x6E}; // "run"
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit);
        TransactionResult result = avm.run(tx, block, cb);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals("Hello, world!", new String(result.getReturnData()));
    }

    @Test
    public void testDeployAndRunWithArgs() {
        TransactionResult deployResult = deployHelloWorld();

        // test another method call, "add" with arguments
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "add<II>" + raw data 123, 1
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit);
        TransactionResult result = avm.run(tx, block, cb);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(124, ByteBuffer.allocate(4).put(result.getReturnData()).getInt(0));
    }

    @Test
    public void testDeployAndRunTest() {
        TransactionResult deployResult = deployTheDeployAndRunTest();
        assertEquals(TransactionResult.Code.SUCCESS, deployResult.getStatusCode());

        // test encode method arguments with "encodeArgs"
        byte[] txData = new byte[]{0x65, 0x6E, 0x63, 0x6F, 0x64, 0x65, 0x41, 0x72, 0x67, 0x73}; // encodeArgs
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit);
        TransactionResult result = avm.run(tx, block, cb);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        byte[] expected = new byte[]{0x61, 0x64, 0x64, 0x41, 0x72, 0x72, 0x61, 0x79, 0x3C, 0x5B, 0x49, 0x32, 0x5D, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05};
        // "addArray<[I2]I>" + raw data 123, 1, 5
        boolean correct = Arrays.equals(result.getReturnData(), expected);
        assertEquals(true, correct);

        // test another method call, "addArray" with 1D array arguments
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, expected, energyLimit);
        result = avm.run(tx, block, cb);

        assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        assertEquals(129, ByteBuffer.allocate(4).put(result.getReturnData()).getInt(0));

/* disable these tests before we have the 2D array object creation
        // test another method call, "addArray2" with 2D array arguments
        txData = new byte[]{0x61, 0x64, 0x64, 0x41, 0x72, 0x72, 0x61, 0x79, 0x32, 0x3C, 0x5B, 0x5B, 0x49, 0x31, 0x5D, 0x32, 0x5D, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "addArray<[I2]>" + raw data 123, 1
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit);
        result = avm.run(tx, block, cb);

        assertEquals(TransactionResult.Code.SUCCESS, result.code);
        assertEquals(124, ByteBuffer.allocate(4).put(result.getReturnData()).getInt(0));

        // test another method call, "concatenate" with 2D array arguments and 1D array return data
        txData = new byte[]{0x63, 0x6F, 0x6E, 0x63, 0x61, 0x74, 0x65, 0x6E, 0x61, 0x74, 0x65, 0x3C, 0x5B, 0x5B, 0x43, 0x33, 0x5D, 0x32, 0x5D, 0x3E,
                0x63, 0x61, 0x74, 0x64, 0x6F, 0x67}; // "concatenate<[[C3]2]>" + raw data "cat" "dog"
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit);
        result = avm.run(tx, block, cb);

        assertEquals(TransactionResult.Code.SUCCESS, result.code);
        assertEquals("<[C6]>catdog", new String(result.getReturnData()));

        // test another method call, "swap" with 2D array arguments and 2D array return data
        txData = new byte[]{0x73, 0x77, 0x61, 0x70, 0x3C, 0x5B, 0x5B, 0x43, 0x33, 0x5D, 0x32, 0x5D, 0x3E,
                0x63, 0x61, 0x74, 0x64, 0x6F, 0x67}; // "swap<[[C3]2]>" + raw data "cat" "dog"
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.getReturnData(), 0, txData, energyLimit);
        result = avm.run(tx, block, cb);

        assertEquals(TransactionResult.Code.SUCCESS, result.code);
        assertEquals("<[[C]2](3)(3)>dogcat", new String(result.getReturnData()));
*/    }
}
