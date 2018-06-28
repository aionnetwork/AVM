package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.api.Address;
import org.aion.kernel.Block;
import org.aion.kernel.KernelApiImpl;
import org.aion.kernel.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class AvmImplDeployAndRunTest {
    private static AvmSharedClassLoader sharedClassLoader;
    private static AvmImpl avm;
    private static KernelApiImpl cb;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        avm = new AvmImpl(sharedClassLoader);
        cb = new KernelApiImpl();
    }

    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 5000000;

    Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    public AvmResult deployHelloWorld() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, jar, energyLimit);

        return avm.run(tx, block, cb);
    }

    public AvmResult deployTheDeployAndRunTest() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.deployAndRunTest.jar");
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, jar, energyLimit);

        return avm.run(tx, block, cb);
    }

    @Test
    public void testDeploy() {
        AvmResult result = deployHelloWorld();

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

/* TODO: fix the following tests
    @Test
    public void testDeployWithMethodCall() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01};
        IBlockchainRuntime rt = new SimpleRuntime(from, to, energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader, codeStorage);
        AvmResult result = avm.deploy(jar, null, rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }*/

    @Test
    public void testDeployAndRun() {
        AvmResult deployResult = deployHelloWorld();

        // call the "run" method
        byte[] txData = new byte[]{0x72, 0x75, 0x6E}; // "run"
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.address.unwrap(), txData, energyLimit);
        AvmResult result = avm.run(tx, block, cb);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployAndRunWithArgs() {
        AvmResult deployResult = deployHelloWorld();

        // test another method call, "add" with arguments
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "add<II>" + raw data 123, 1
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.address.unwrap(), txData, energyLimit);
        AvmResult result = avm.run(tx, block, cb);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
        assertEquals(124, ByteBuffer.allocate(4).put(result.returnData).getInt(0));
    }

    @Test
    public void testDeployAndRunWithArrayArgs() {
        AvmResult deployResult = deployTheDeployAndRunTest();
        assertEquals(AvmResult.Code.SUCCESS, deployResult.code);

        // test another method call, "addArray" with 1D array arguments
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x41, 0x72, 0x72, 0x61, 0x79, 0x3C, 0x5B, 0x49, 0x32, 0x5D, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "addArray<[I2]>" + raw data 123, 1
        Transaction tx = new Transaction(Transaction.Type.CALL, from, deployResult.address.unwrap(), txData, energyLimit);
        AvmResult result = avm.run(tx, block, cb);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
        assertEquals(124, ByteBuffer.allocate(4).put(result.returnData).getInt(0));

        // test another method call, "addArray2" with 2D array arguments
        txData = new byte[]{0x61, 0x64, 0x64, 0x41, 0x72, 0x72, 0x61, 0x79, 0x32, 0x3C, 0x5B, 0x5B, 0x49, 0x31, 0x5D, 0x32, 0x5D, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "addArray<[I2]>" + raw data 123, 1
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.address.unwrap(), txData, energyLimit);
        result = avm.run(tx, block, cb);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
        assertEquals(124, ByteBuffer.allocate(4).put(result.returnData).getInt(0));

        // test another method call, "concatenate" with 2D array arguments and 1D array return data
        txData = new byte[]{0x63, 0x6F, 0x6E, 0x63, 0x61, 0x74, 0x65, 0x6E, 0x61, 0x74, 0x65, 0x3C, 0x5B, 0x5B, 0x43, 0x33, 0x5D, 0x32, 0x5D, 0x3E,
                0x63, 0x61, 0x74, 0x64, 0x6F, 0x67}; // "concatenate<[[C3]2]>" + raw data "cat" "dog"
        tx = new Transaction(Transaction.Type.CALL, from, deployResult.address.unwrap(), txData, energyLimit);
        result = avm.run(tx, block, cb);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
        assertEquals("catdog", new String(result.returnData));
    }
}
