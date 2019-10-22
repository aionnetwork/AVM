package org.aion.avm.core.stacktracking;

import java.math.BigInteger;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.ExecutionType;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConstructorStackTest {
    private static final AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static TestingBlock block;
    private static TestingState kernel;
    private static AvmImpl avm;
    private static AionAddress dappAddress;

    @BeforeClass
    public static void setupClass() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        AvmConfiguration config = new AvmConfiguration();
        config.enableVerboseContractErrors = true;
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);

        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ConstructorStackTarget.class);
        byte[] deployment = new CodeAndArguments(jar, null).encodeToBytes();
        Transaction create = AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, deployment, 5_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        dappAddress = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    @AfterClass
    public static void tearDownClass() {
        avm.shutdown();
    }

    @Test
    public void test() {
        //TODO
    }
}
