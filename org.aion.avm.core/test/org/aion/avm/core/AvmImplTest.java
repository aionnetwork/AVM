package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.JvmError;
import org.aion.avm.rt.BlockchainRuntime;
import org.junit.Assert;
import org.junit.Ignore;
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
        BlockchainRuntime rt = new SimpleRuntime(sender, address, energyLimit);
        AvmImpl avm = new AvmImpl();
        AvmResult result = avm.deploy(jar, rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Ignore
    @Test
    public void testDeployAndRun() {
        testDeploy();

        BlockchainRuntime rt = new SimpleRuntime(sender, address, energyLimit);
        AvmImpl avm = new AvmImpl();
        AvmResult result = avm.run(rt);

        assertEquals(AvmResult.Code.SUCCESS, result.code);
    }

    @Test
    public void testJvmError() {
        // Note that we eventually need to test how this interacts with AvmImpl's contract entry-point but this at least proves
        // that the hierarchy is correctly put together.
        String result = null;
        try {
            throw new JvmError(new UnknownError("testing"));
        } catch (AvmException e) {
            result = e.getMessage();
        }
        assertEquals("java.lang.UnknownError: testing", result);
    }


    private static class SimpleRuntime implements BlockchainRuntime {
        private final byte[] sender;
        private final byte[] address;
        private final long energyLimit;

        public SimpleRuntime(byte[] sender, byte[] address, long energyLimit) {
            this.sender = sender;
            this.address = address;
            this.energyLimit = energyLimit;
        }
        @Override
        public ByteArray getSender() {
            return new ByteArray(this.sender);
        }
        @Override
        public ByteArray getAddress() {
            return new ByteArray(this.address);
        }
        @Override
        public long getEnergyLimit() {
            return this.energyLimit;
        }
        @Override
        public ByteArray getData() {
            Assert.fail("This implementation doesn't handle this");
            return null;
        }
        @Override
        public ByteArray getStorage(ByteArray key) {
            Assert.fail("This implementation doesn't handle this");
            return null;
        }
        @Override
        public void putStorage(ByteArray key, ByteArray value) {
            Assert.fail("This implementation doesn't handle this");
        }
    }
}