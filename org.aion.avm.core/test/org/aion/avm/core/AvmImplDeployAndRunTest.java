package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.kernel.Block;
import org.aion.kernel.KernelApiImpl;
import org.aion.kernel.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AvmImplDeployAndRunTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 1000000;

    Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    public AvmResult deployHelloWorld() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, jar, energyLimit);
        KernelApiImpl cb = new KernelApiImpl();

        AvmImpl avm = new AvmImpl(sharedClassLoader);
        return avm.create(tx, block, cb);
    }

    public AvmResult deployTheDeployAndRunTest() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.deployAndRunTest.jar");
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, jar, energyLimit);
        KernelApiImpl cb = new KernelApiImpl();
        BlockchainRuntime runtime = AvmImpl.createBlockchainRuntime(tx, block, cb);

        AvmImpl avm = new AvmImpl(sharedClassLoader);
        return avm.create(tx, block, cb);
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
        BlockchainRuntime rt = new SimpleRuntime(from, to, energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader, codeStorage);
        AvmResult result = avm.deploy(jar, null, rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployAndRun() {
        AvmResult deployResult = deployHelloWorld();

        // call the "run" method
        byte[] txData = new byte[]{0x72, 0x75, 0x6E}; // "run"
        BlockchainRuntime rt = new SimpleRuntime(from, deployResult.address.unwrap(), energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader, codeStorage);
        AvmResult result = avm.run(rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployAndRunWithArgs() {
        AvmResult deployResult = deployHelloWorld();

        // test another method call, "add" with arguments
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "add<II>" + raw data 123, 1
        BlockchainRuntime rt = new SimpleRuntime(from, deployResult.address.unwrap(), energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader, codeStorage);
        AvmResult result = avm.run(rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
        assertEquals(124, result.returnData);
    }

    @Test
    public void testDeployAndRunWithArrayArgs() {
        AvmResult deployResult = deployTheDeployAndRunTest();

        // test another method call, "add" with arguments
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x41, 0x72, 0x72, 0x61, 0x79, 0x3C, 0x5B, 0x49, 0x32, 0x5D, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "addArray<[I2]>" + raw data 123, 1
        BlockchainRuntime rt = new SimpleRuntime(from, deployResult.address.unwrap(), energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader, codeStorage);
        AvmResult result = avm.run(rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
        assertEquals(124, result.returnData);
    }
*/
}
