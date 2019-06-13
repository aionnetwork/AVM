package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.kernel.AvmTransactionResult;
import org.aion.types.AionAddress;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApiSubclassingTest {
    private TestingKernel kernel;
    private AvmImpl avm;
    private AionAddress deployer = TestingKernel.PREMINED_ADDRESS;

    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void teardown() {
        this.avm.shutdown();
    }

    @Test
    public void testDeployAndCallContractWithAbiSubclasses() {
        byte[] jar = new CodeAndArguments(JarBuilder.buildJarForMainAndClassesAndUserlib(ApiSubclassingTarget.class), null).encodeToBytes();
        TestingTransaction transaction = TestingTransaction.create(this.deployer, this.kernel.getNonce(deployer), BigInteger.ZERO, jar, 5_000_000, 1);
        AvmTransactionResult result = this.avm.run(this.kernel, new TestingTransaction[] {transaction})[0].get();
        assertEquals(Code.FAILED_REJECTED, result.getResultCode());
    }

}
