package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.rt.BlockchainRuntime;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

/**
 * @author Roman Katerinenko
 */
public class AvmImplTest {
    @Test
    public void checkMainClassHasProperName() throws IOException {
        final var module = "com.example.avmstartuptest";
        final Path path = Paths.get(format("%s/%s.jar", "../examples/build", module));
        final byte[] jar = Files.readAllBytes(path);
        final AvmImpl.DappModule dappModule = AvmImpl.readDapp(jar);
        final var mainClassName = "com.example.avmstartuptest.MainClass";
        assertEquals(mainClassName, dappModule.getMainClass());
        Map<String, byte[]> classes = dappModule.getClasses();
        assertEquals(1, classes.size());
        final var expectedSizeOfFile = 424;
        assertEquals(expectedSizeOfFile, classes.get(mainClassName).length);
    }


    private byte[] sender = Helpers.randomBytes(32);
    private byte[] address = Helpers.randomBytes(32);
    private long energyLimit = 1000000;


    @Test
    public void testDeploy() {

        byte[] jar = Helpers.readFileToBytes("../examples/build/com.example.helloworld.jar");
        BlockchainRuntime rt = new BlockchainRuntime() {
            @Override
            public ByteArray getSender() {
                return new ByteArray(sender);
            }

            @Override
            public ByteArray getAddress() {
                return new ByteArray(address);
            }

            @Override
            public long getEnergyLimit() {
                return energyLimit;
            }

            @Override
            public ByteArray getData() {
                return null;
            }

            @Override
            public ByteArray getStorage(ByteArray key) {
                return null;
            }

            @Override
            public void putStorage(ByteArray key, ByteArray value) {

            }
        };
        AvmImpl avm = new AvmImpl();
        AvmResult result = avm.deploy(jar, rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testDeployAndRun() {
        testDeploy();

        BlockchainRuntime rt = new BlockchainRuntime() {
            @Override
            public ByteArray getSender() {
                return new ByteArray(sender);
            }

            @Override
            public ByteArray getAddress() {
                return new ByteArray(address);
            }

            @Override
            public long getEnergyLimit() {
                return energyLimit;
            }

            @Override
            public ByteArray getData() {
                return null;
            }

            @Override
            public ByteArray getStorage(ByteArray key) {
                return null;
            }

            @Override
            public void putStorage(ByteArray key, ByteArray value) {

            }
        };
        AvmImpl avm = new AvmImpl();
        AvmResult result = avm.run(rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }
}