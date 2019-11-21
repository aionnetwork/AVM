package org.aion.avm.core.shadowing;

import org.aion.avm.core.*;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.Transaction;
import org.aion.types.AionAddress;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.*;

import java.math.BigInteger;
import java.util.Collections;


/**
 * Tests that we fail in a meaningful way when a DApp throws an exception due to a missing method.
 */
public class DefaultMethodIntegrationTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static TestingState kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testMissingDefaultInClinit() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(TestDefaultMethodInClinitResource.class, Collections.emptyMap(), AionMap.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 2_000_000l;
        long energyPrice = 1l;
        Transaction create = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        
        // The NoSuchMethodError triggers a "FAILED_EXCEPTION" state.
        TransactionResult result = avm.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);
    }

    @Test
    public void testMissingDefaultInMain() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(TestDefaultMethodInMainResource.class, Collections.emptyMap(), AionMap.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 2_000_000l;
        long energyPrice = 1l;
        Transaction create = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(createResult.transactionStatus.isSuccess());
        AionAddress contractAddr = new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());

        // Setup the call (parameters are currently ignored).
        byte[] argData = new byte[0];
        Transaction call = AvmTransactionUtil.call(deployer, contractAddr, kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        
        // The NoSuchMethodError triggers a "FAILED_EXCEPTION" state.
        TransactionResult result = avm.run(kernel, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);
    }
}
