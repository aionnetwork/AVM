package org.aion.avm.core;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIToken;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;

import org.aion.types.TransactionResult;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistanceNameMappingTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static TestingState kernel;
    private static AvmConfiguration configurationWithDebugEnabled;
    private static AvmConfiguration configurationWithDebugDisabled;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        AvmConfiguration configurationWithDebug = new AvmConfiguration();
        configurationWithDebug.preserveDebuggability = true;
        configurationWithDebugEnabled = configurationWithDebug;

        AvmConfiguration configurationNoDebug = new AvmConfiguration();
        configurationNoDebug.preserveDebuggability = false;
        configurationWithDebugDisabled = configurationNoDebug;
    }

    @Test
    public void testPersistanceDebugEnabled() {
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), configurationWithDebugEnabled);
        runTestLogic(avm);
        avm.shutdown();
    }

    @Test
    public void testPersistanceDebugDisabled() {
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), configurationWithDebugDisabled);
        runTestLogic(avm);
        avm.shutdown();
    }

    private void runTestLogic(AvmImpl avm) {
        kernel.generateBlock();
        // Deploy the contract.
        AionAddress contract = deployContract(avm, kernel);
        kernel.generateBlock();

        // Set all the persistant fields.
        callContract(avm, kernel, contract, "setFields");
        kernel.generateBlock();

        // Verify all the persistant fields have been retained.
        callContract(avm, kernel, contract, "verifyFields");
    }

    private AionAddress deployContract(AvmImpl avm, IExternalState externalState) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistanceNameMappingTestTarget.class, ABIDecoder.class, ABIException.class, ABIToken.class, AionBuffer.class, AionSet.class, AionMap.class);
        byte[] data = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction createTransaction = AvmTransactionUtil.create(deployer, externalState.getNonce(deployer), BigInteger.ZERO, data, 5_000_000L, 1L);
        TransactionResult result = avm.run(externalState, new Transaction[]{ createTransaction })[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());

        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    private void callContract(AvmImpl avm, IExternalState externalState, AionAddress contract, String method) {
        byte[] data = ABIEncoder.encodeOneString(method);
        Transaction callTransaction = AvmTransactionUtil.call(deployer, contract, externalState.getNonce(deployer), BigInteger.ZERO, data, 2_000_000L, 1L);
        TransactionResult result = avm.run(externalState, new Transaction[]{ callTransaction })[0].getResult();

        // The tests will REVERT if what we want to test does not occur!
        assertTrue(result.transactionStatus.isSuccess());
    }
}
