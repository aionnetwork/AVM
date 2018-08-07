package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.AvmException;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.JvmError;
import org.aion.avm.internal.OutOfEnergyError;
import org.aion.kernel.Block;
import org.aion.kernel.InternalTransaction;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;
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
    private static Block block;

    @BeforeClass
    public static void setupClass() {
        block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    }

    @Test
    public void checkMainClassHasProperName() throws IOException {
        final var module = "com.example.avmstartuptest";
        final Path path = Paths.get(format("%s/%s.jar", "../examples/build", module));
        final byte[] jar = Files.readAllBytes(path);
        final RawDappModule dappModule = RawDappModule.readFromJar(jar);
        final var mainClassName = "com.example.avmstartuptest.MainClass";
        assertEquals(mainClassName, dappModule.mainClass);
        Map<String, byte[]> classes = dappModule.classes;
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
        IHelper helper = Helpers.instantiateHelper(NodeEnvironment.singleton.createInvocationClassLoader(contractClasses), 5L, 1);

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
            catchCount += 1;
            // And have the same exception.
            assertEquals(error, e);
        }
        assertEquals(2, catchCount);
    }

    // for asserts
    private IHelper currentContractHelper = null;


    private class CustomKernel extends KernelInterfaceImpl {
        @Override
        public TransactionResult call(Avm avm, InternalTransaction internalTx, Block parentBlock) {
            currentContractHelper = IHelper.currentContractHelper.get();

            TransactionResult result = super.call(avm, internalTx, parentBlock);
            result.setEnergyUsed(internalTx.getEnergyLimit() / 2);

            return result;
        }
    }

    @Test
    public void testHelperStateRestore() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(AvmImplTestResource.class);
        byte[] arguments = new byte[0];
        byte[] txData = Helpers.encodeCodeAndData(jar, arguments);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new CustomKernel());

        // deploy
        long transaction1EnergyLimit = 1_000_000l;
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, transaction1EnergyLimit);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());

        Address contractAddr = new Address(result1.getReturnData());

        // Account for the cost:  deployment, clinit, init call.
        // BytecodeFeeScheduler:  PROCESS + (PROCESSDATA * bytecodeSize * (1 + numberOfClasses) / 10)
        long deploymentProcessCost = 32000 + (10 * jar.length * (1 + 1) / 10);
        // BytecodeFeeScheduler:  CODEDEPOSIT * data.length;
        long deploymentStorageCost = 200 * txData.length;
        long clinitCost = 188l;
        assertEquals(deploymentProcessCost + deploymentStorageCost + clinitCost, result1.getEnergyUsed());

        // call (1 -> 2 -> 2)
        long transaction2EnergyLimit = 1_000_000l;
        Transaction tx2 = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, contractAddr.unwrap(), transaction2EnergyLimit);
        TransactionResult result2 = avm.run(new TransactionContextImpl(tx2, block));
        assertEquals(TransactionResult.Code.SUCCESS, result2.getStatusCode());
        assertArrayEquals("CALL".getBytes(), result2.getReturnData());
        // Account for the cost:  (blocks in call method) + runtime.call
        long costOfBlocks = 111l + 57l + 461l;
        // Note that this runtime call is 250000l whereas the actual cost of the receiver execution is:  111l + 57l + 116l.
        long costOfRuntimeCall = 250000l;
        assertEquals(costOfBlocks + costOfRuntimeCall, result2.getEnergyUsed()); // NOTE: the numbers are not calculated, but for fee schedule change detection.

        assertTrue(currentContractHelper == IHelper.currentContractHelper.get()); // same instance
    }
}