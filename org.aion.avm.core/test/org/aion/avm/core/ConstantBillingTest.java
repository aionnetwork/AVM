package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.instrument.BytecodeFeeScheduler;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.TransactionResult;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class ConstantBillingTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static long energyLimit = 1_000_000;

    @Test
    public void test() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(ConstantBillingTarget.class);

        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        IExternalState externalState = new TestingState(block);
        Transaction tx = AvmTransactionUtil.create(deployer, externalState.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, new byte[0]).encodeToBytes(), energyLimit, 1);
        TransactionResult result = avm.run(externalState, new Transaction[] { tx }, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());

        BytecodeFeeScheduler feeScheduler = new BytecodeFeeScheduler();
        feeScheduler.initialize();

        // See AKI-136: static instrumentation code is no longer billed, so we charge for an ldc + putstatic in the clinit now (assigning constant to static field)
        // rather than a getstatic + putstatic (fetching the constant from our constant instrumentation class)
        long ldcFee = feeScheduler.getFee(Opcodes.LDC);
        long putstaticFee = feeScheduler.getFee(Opcodes.PUTSTATIC);
        long returnFee = feeScheduler.getFee(Opcodes.RETURN);

        long clinitCost = ldcFee + putstaticFee + returnFee;

        long basicTransactionCost = BillingRules.getBasicTransactionCost(tx.copyOfTransactionData());
        long deploymentFee = BillingRules.getDeploymentFee(1, jar.length);
        long storageFee = 141;

        long cost = basicTransactionCost + deploymentFee + clinitCost + storageFee;

        assertEquals(cost, result.energyUsed);

        avm.shutdown();
    }
}
