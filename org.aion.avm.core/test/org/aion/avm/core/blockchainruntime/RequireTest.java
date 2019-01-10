package org.aion.avm.core.blockchainruntime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.EmptyInstrumentation;
import org.aion.avm.core.RedirectContract;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link org.aion.avm.api.BlockchainRuntime#require(boolean)} method.
 */
public class RequireTest {
    private static Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 5;
    private static Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private static KernelInterface kernel;
    private static AvmImpl avm;
    private static Address contract;

    @BeforeClass
    public static void setup() {
        kernel = new KernelInterfaceImpl();
        avm = CommonAvmFactory.buildAvmInstance(kernel);
        deployContract();
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testRequireOnTrueCondition() {
        TransactionResult result = callContractRequireMethod(true);
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testRequireOnFalseCondition() {
        TransactionResult result = callContractRequireMethod(false);
        assertEquals(Code.FAILED_REVERT, result.getResultCode());

        // A REVERT should NOT use up all remaining energy. We should be refunded.
        assertTrue(result.getEnergyRemaining() > 0);
    }

    @Test
    public void testRequireOnTrueConditionOnInternalCondition() {
        // We use the RedirectContract to trigger an internal transaction into the RequireTarget contract.
        Address redirectContract = deployRedirectContract();
        TransactionResult result = callRedirectContract(redirectContract, true);

        // If redirect condition is SUCCESS then its internal call was also SUCCESS.
        assertTrue(result.getResultCode().isSuccess());
    }

    @Test
    public void testRequireOnFalseConditionOnInternalCondition() {
        // We use the RedirectContract to trigger an internal transaction into the RequireTarget contract.
        Address redirectContract = deployRedirectContract();
        TransactionResult result = callRedirectContract(redirectContract, false);

        // If internal call was not SUCCESS then redirect gets a REVERT as well.
        assertEquals(Code.FAILED_REVERT, result.getResultCode());
    }

    @Test
    public void testUnableToCatchRevertExceptionFromRequire() {
        assertEquals(Code.FAILED_REVERT, callContractRequireAndAttemptToCatchExceptionMethod().getResultCode());
    }

    @Test
    public void testRequireInClinitOnTrueCondition() {
        assertTrue(deployContractAndTriggerClinitRequire(true).getResultCode().isSuccess());
    }

    @Test
    public void testRequireInClinitOnFalseCondition() {
        assertEquals(Code.FAILED_REVERT, deployContractAndTriggerClinitRequire(false).getResultCode());
    }

    private static TransactionResult deployContractAndTriggerClinitRequire(boolean condition) {
        byte[] clinitData = ABIEncoder.encodeOneObject(condition);
        byte[] data = new CodeAndArguments(getRawJarBytesForRequireContract(), clinitData).encodeToBytes();

        Transaction transaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    private static void deployContract() {
        byte[] jar = new CodeAndArguments(getRawJarBytesForRequireContract(), new byte[0]).encodeToBytes();

        Transaction transaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, jar, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        contract = AvmAddress.wrap(result.getReturnData());
    }

    private TransactionResult callContractRequireMethod(boolean condition) {
        byte[] callData = getAbiEncodingOfRequireContractCall(condition);
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    private TransactionResult callContractRequireAndAttemptToCatchExceptionMethod() {
        byte[] callData = ABIEncoder.encodeMethodArguments("requireAndTryToCatch");
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    private static Address deployRedirectContract() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RedirectContract.class);
        jar = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, jar, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return AvmAddress.wrap(result.getReturnData());
    }

    private TransactionResult callRedirectContract(Address redirect, boolean condition) {
        byte[] callData = encodeRedirectCallArgs(condition);
        Transaction transaction = Transaction.call(from, redirect, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

    private byte[] encodeRedirectCallArgs(boolean condition) {
        org.aion.avm.api.Address contractToCall = getRequireContractAsAbiAddress();
        byte[] args = getAbiEncodingOfRequireContractCall(condition);
        return ABIEncoder.encodeMethodArguments("callOtherContractAndRequireItIsSuccess", contractToCall, 0L, args);
    }

    private org.aion.avm.api.Address getRequireContractAsAbiAddress() {
        IInstrumentation instrumentation = new EmptyInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        org.aion.avm.api.Address converted = new org.aion.avm.api.Address(contract.toBytes());
        InstrumentationHelpers.detachThread(instrumentation);
        return converted;
    }

    private byte[] getAbiEncodingOfRequireContractCall(boolean condition) {
        return ABIEncoder.encodeMethodArguments("require", condition);
    }

    private static byte[] getRawJarBytesForRequireContract() {
        return JarBuilder.buildJarForMainAndClasses(RequireTarget.class);
    }

}
