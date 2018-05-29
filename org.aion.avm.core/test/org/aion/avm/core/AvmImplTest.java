package org.aion.avm.core;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.JvmError;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.avm.rt.BlockchainRuntime;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

/**
 * @author Roman Katerinenko
 */
public class AvmImplTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() throws Exception {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    @After
    public void teardown() throws Exception {
        Helper.clearTestingState();
    }

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

    @Test
    public void testDeployAndRun() {
        testDeploy();

        BlockchainRuntime rt = new SimpleRuntime(sender, address, energyLimit) {
            @Override
            public ByteArray getData() {
                return new ByteArray(new byte[0]);
            }
            @Override
            public ByteArray getStorage(ByteArray key) {
                return null;
            }
        };
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

    /**
     * Tests that, if we hit the energy limit, we continue to hit it on every attempt to charge for a new code block.
     */
    @Test
    public void testPersistentEnergyLimit() {
        // Set up the runtime.
        BlockchainRuntime rt = new SimpleRuntime(null, null, 5);
        new Helper(new AvmClassLoader(sharedClassLoader, Collections.emptyMap()), rt);

        // Prove that we can charge 0 without issue.
        Helper.chargeEnergy(0);
        assertEquals(5, Helper.energyLeft());

        // Run the test.
        int catchCount = 0;
        OutOfEnergyError error = null;
        try {
            Helper.chargeEnergy(10);
        } catch (OutOfEnergyError e) {
            catchCount += 1;
            error = e;
        }
        // We didn't reset the state so this should still fail.
        try {
            Helper.chargeEnergy(0);
        } catch (OutOfEnergyError e) {
            catchCount += 1;
            // And have the same exception.
            assertEquals(error, e);
        }
        assertEquals(2, catchCount);

        // Cleanup.
        Helper.clearTestingState();
    }
}
