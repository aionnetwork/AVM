package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;
import org.aion.kernel.TransformedDappStorage;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AvmImplDeployAndRunTest {
    private static AvmSharedClassLoader sharedClassLoader;
    private static TransformedDappStorage codeStorage;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        codeStorage = new TransformedDappStorage();
    }

    private byte[] sender = Helpers.randomBytes(Address.LENGTH);
    private byte[] address = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 1000000;

    public AvmResult deployHelloWorld() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        BlockchainRuntime rt = new SimpleRuntime(sender, address, energyLimit, null);
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        AvmResult result = avm.deploy(jar, rt, codeStorage);

        return result;
    }

    @Test
    public void testDeploy() {
        AvmResult result = deployHelloWorld();

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployWithMethodCall() {
        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01};
        BlockchainRuntime rt = new SimpleRuntime(sender, address, energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        AvmResult result = avm.deploy(jar, rt, codeStorage);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployAndRun() {
        AvmResult deployResult = deployHelloWorld();

        // call the "run" method
        byte[] txData = new byte[]{0x72, 0x75, 0x6E}; // "run"
        BlockchainRuntime rt = new SimpleRuntime(sender, deployResult.address.unwrap(), energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        AvmResult result = avm.run(rt, codeStorage);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployAndRunWithArgs() {
        AvmResult deployResult = deployHelloWorld();

        // test another method call, "add" with arguments
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "add<II>" + raw data 123, 1
        BlockchainRuntime rt = new SimpleRuntime(sender, deployResult.address.unwrap(), energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        AvmResult result = avm.run(rt, codeStorage);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
        assertEquals(124, result.returnData);
    }
}
