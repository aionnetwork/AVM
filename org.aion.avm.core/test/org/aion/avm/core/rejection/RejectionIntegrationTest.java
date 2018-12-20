package org.aion.avm.core.rejection;

import java.math.BigInteger;

import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Implemented as part of issue-305 to demonstrate how rejections are actually observed, within the transformation logic.
 */
public class RejectionIntegrationTest {
    // We will reuse these, for now, since we want to test that doing so is safe.  We may change this, in the future, is we depend on something perturbed by this.
    private static final org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static KernelInterfaceImpl kernel;
    private static VirtualMachine avm;

    @BeforeClass
    public static void setupClass() {
        kernel = new KernelInterfaceImpl();
        avm = CommonAvmFactory.buildAvmInstance(kernel);
    }

    @AfterClass
    public static void tearDownClass() {
        avm.shutdown();
    }

    @Test
    public void rejectNonShadowJclSubclassError() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectNonShadowJclSubclassError.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }
}
