package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RevertInClinitTest {
    private long energyLimit = 5_000_000L;
    private AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private TestingState kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingState(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void teardown() {
        this.avm.shutdown();
    }

    @Test
    public void testRevertInClinitUsesEnergy() {
        byte[] jar = new CodeAndArguments(JarBuilder.buildJarForMainAndClassesAndUserlib(RevertInClinitTarget.class), null).encodeToBytes();
        Transaction transaction = AvmTransactionUtil.create(this.deployer, this.kernel.getNonce(deployer), BigInteger.ZERO, jar, this.energyLimit, 1);
        TransactionResult result = this.avm.run(this.kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isReverted());

        // Since we actually ran code, we expect there to be a positive amount of energy used.
        Assert.assertTrue(result.energyUsed > 0);
    }

    @Test
    public void testRevertInClinitDoesNotUseAllEnergy() {
        byte[] jar = new CodeAndArguments(JarBuilder.buildJarForMainAndClassesAndUserlib(RevertInClinitTarget.class), null).encodeToBytes();
        Transaction transaction = AvmTransactionUtil.create(this.deployer, this.kernel.getNonce(deployer), BigInteger.ZERO, jar, this.energyLimit, 1);
        TransactionResult result = this.avm.run(this.kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isReverted());

        // Verify that we did not use up all of our energy.
        Assert.assertTrue(result.energyUsed < this.energyLimit);
    }
}
