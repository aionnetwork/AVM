package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.JvmError;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.kernel.Block;
import org.aion.kernel.KernelApiImpl;
import org.aion.kernel.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Roman Katerinenko
 */
public class AvmImplTest {
    private static AvmSharedClassLoader sharedClassLoader;
    private static Block block;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateShadowJDK());
        block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
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
        Map<String, byte[]> contractClasses = Helpers.mapIncludingHelperBytecode(Collections.emptyMap());
        IHelper helper = Helpers.instantiateHelper(new AvmClassLoader(sharedClassLoader, contractClasses), 5L, 1);

        // Prove that we can charge 0 without issue.
        helper.externalChargeEnergy(0);
        assertEquals(5, helper.externalGetEnergyRemaining());

        // Run the test.
        int catchCount = 0;
        OutOfEnergyError error = null;
        try {
            helper.externalChargeEnergy(10);
        } catch (OutOfEnergyError e) {
            catchCount += 1;
            error = e;
        }
        // We didn't reset the state so this should still fail.
        try {
            helper.externalChargeEnergy(0);
        } catch (OutOfEnergyError e) {
            new AvmImpl(sharedClassLoader);
            catchCount += 1;
            // And have the same exception.
            assertEquals(error, e);
        }
        assertEquals(2, catchCount);
    }

    // for asserts
    private IHelper currentContractHelper = null;
    private long currentEnergyLeft = 0;

    @Test
    public void testHelperStateRestore() {

        byte[] jar = JarBuilder.buildJarForMainAndClasses(AvmImplTestResource.class);
        KernelApiImpl kernel = new KernelApiImpl() {
            public AvmResult call(byte[] from, byte[] to, long value, byte[] data, long energyLimit) {
                currentContractHelper = IHelper.currentContractHelper.get();
                currentEnergyLeft = currentContractHelper.externalGetEnergyRemaining();

                AvmResult result = super.call(from, to, value, data, energyLimit);
                result.energyLeft = energyLimit / 2;

                return result;
            }
        };
        kernel.sharedClassLoader = sharedClassLoader;
        kernel.block = block;

        // deploy
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, jar, 1000000);
        AvmImpl avm1 = new AvmImpl(sharedClassLoader);
        AvmResult result1 = avm1.run(tx1, block, kernel);
        assertEquals(AvmResult.Code.SUCCESS, result1.code);
        assertArrayEquals(Helpers.address(2), result1.returnData);

        // call (1 -> 2 -> 2)
        Transaction tx2 = new Transaction(Transaction.Type.CALL, Helpers.address(1), Helpers.address(2), 0, Helpers.address(2), 1000000);
        AvmImpl avm2 = new AvmImpl(sharedClassLoader);
        AvmResult result2 = avm2.run(tx2, block, kernel);
        assertEquals(AvmResult.Code.SUCCESS, result2.code);
        assertArrayEquals("CALL".getBytes(), result2.returnData);

        assertTrue(currentContractHelper == IHelper.currentContractHelper.get()); // same instance
        assertEquals(999487 - 500000 / 2 - 176, result2.energyLeft); // NOTE: the numbers are not calculated, but for fee schedule change detection.
    }
}