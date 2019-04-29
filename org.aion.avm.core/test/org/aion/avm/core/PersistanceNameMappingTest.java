package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIToken;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionInterface;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistanceNameMappingTest {
    private static Address deployer = TestingKernel.PREMINED_ADDRESS;
    private static Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private static KernelInterface kernel = new TestingKernel(block);
    private static AvmConfiguration configurationWithDebugEnabled;
    private static AvmConfiguration configurationWithDebugDisabled;

    @BeforeClass
    public static void setup() {
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
        // Deploy the contract.
        Address contract = deployContract(avm, kernel);

        // Set all the persistant fields.
        callContract(avm, kernel, contract, "setFields");

        // Verify all the persistant fields have been retained.
        callContract(avm, kernel, contract, "verifyFields");
    }

    private Address deployContract(AvmImpl avm, KernelInterface kernel) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(PersistanceNameMappingTestTarget.class, ABIDecoder.class, ABIException.class, ABIToken.class, AionBuffer.class, AionSet.class, AionMap.class);
        byte[] data = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction createTransaction = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, data, 5_000_000L, 1L);
        TransactionResult result = avm.run(kernel, new TransactionInterface[]{ createTransaction })[0].get();
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());

        return new Address(result.getReturnData());
    }

    private void callContract(AvmImpl avm, KernelInterface kernel, Address contract, String method) {
        byte[] data = ABIEncoder.encodeOneString(method);
        Transaction callTransaction = Transaction.call(deployer, contract, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000L, 1L);
        TransactionResult result = avm.run(kernel, new TransactionInterface[]{ callTransaction })[0].get();

        // The tests will REVERT if what we want to test does not occur!
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}
