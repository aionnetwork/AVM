package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AvmImplDeployTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    private byte[] sender = Helpers.randomBytes(Address.LENGTH);
    private byte[] address = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 1000000;

    @Test
    public void testDeploy() {

        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        BlockchainRuntime rt = new SimpleRuntime(sender, address, energyLimit, null);
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        AvmResult result = avm.deploy(jar, rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployWithMethodCall() {

        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01};
        BlockchainRuntime rt = new SimpleRuntime(sender, address, energyLimit, txData);
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        AvmResult result = avm.deploy(jar, rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }
}
