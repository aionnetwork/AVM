package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.instrument.BytecodeFeeScheduler;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class ConstantBillingTest {
    private static Address deployer = TestingKernel.PREMINED_ADDRESS;
    private static long energyLimit = 1_000_000;

    @Test
    public void test() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ConstantBillingTarget.class);

        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        KernelInterface kernel = new TestingKernel(block);
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, new byte[0]).encodeToBytes(), energyLimit, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] { tx })[0].get();
        assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());

        BytecodeFeeScheduler feeScheduler = new BytecodeFeeScheduler();
        feeScheduler.initialize();

        //TODO Once AKI-136 is finished this getstatic should be replaced by ldc -- we are currently
        // charging for the constant instrumentation code we inject into the user code!
        long ldcFee = feeScheduler.getFee(Opcodes.GETSTATIC);
        long putstaticFee = feeScheduler.getFee(Opcodes.PUTSTATIC);
        long returnFee = feeScheduler.getFee(Opcodes.RETURN);

        long clinitCost = ldcFee + putstaticFee + returnFee;

        long basicTransactionCost = BillingRules.getBasicTransactionCost(tx.getData());
        long deploymentFee = BillingRules.getDeploymentFee(1, jar.length);
        long storageFee = 141;

        long cost = basicTransactionCost + deploymentFee + clinitCost + storageFee;

        assertEquals(cost, energyLimit - result.getEnergyRemaining());
    }
}
