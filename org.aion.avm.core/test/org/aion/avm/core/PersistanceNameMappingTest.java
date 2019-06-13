package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.types.AionAddress;
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
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;

import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistanceNameMappingTest {
    private static AionAddress deployer = TestingKernel.PREMINED_ADDRESS;
    private static TestingKernel kernel;
    private static AvmConfiguration configurationWithDebugEnabled;
    private static AvmConfiguration configurationWithDebugDisabled;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(block);
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

    private AionAddress deployContract(AvmImpl avm, KernelInterface kernel) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistanceNameMappingTestTarget.class, ABIDecoder.class, ABIException.class, ABIToken.class, AionBuffer.class, AionSet.class, AionMap.class);
        byte[] data = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        AvmTransaction createTransaction = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, data, 5_000_000L, 1L);
        AvmTransactionResult result = avm.run(kernel, new AvmTransaction[]{ createTransaction })[0].get();
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());

        return new AionAddress(result.getReturnData());
    }

    private void callContract(AvmImpl avm, KernelInterface kernel, AionAddress contract, String method) {
        byte[] data = ABIEncoder.encodeOneString(method);
        AvmTransaction callTransaction = AvmTransactionUtil.call(deployer, contract, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000L, 1L);
        AvmTransactionResult result = avm.run(kernel, new AvmTransaction[]{ callTransaction })[0].get();

        // The tests will REVERT if what we want to test does not occur!
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}
