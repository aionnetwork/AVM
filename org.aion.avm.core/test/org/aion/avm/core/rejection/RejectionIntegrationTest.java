package org.aion.avm.core.rejection;

import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Implemented as part of issue-305 to demonstrate how rejections are actually observed, within the transformation logic.
 */
public class RejectionIntegrationTest {
    private static Address FROM = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static long ENERGY_LIMIT = 5_000_000L;
    private static long ENERGY_PRICE = 1L;
    private static Block BLOCK = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private static KernelInterface kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        kernel = new KernelInterfaceImpl();
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void rejectNonShadowJclSubclassError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectNonShadowJclSubclassError.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        Transaction transaction = Transaction.create(FROM, kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionContext context = TransactionContextImpl.forExternalTransaction(transaction, BLOCK);
        TransactionResult createResult = avm.run(RejectionIntegrationTest.kernel, new TransactionContext[] {context})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }
}
