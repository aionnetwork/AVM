package org.aion.avm.core.rejection.errors;

import java.math.BigInteger;

import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Implemented as part of issue-303 to demonstrate that user code is not able to catch, throw, sub-class, or even instantiate any
 * java.lang.VirtualMachineError type.
 * Since the rejection will be made at the class level, we need several test classes.
 * 
 * We need to run these as integration tests since, although many are just testing the rejection phase, some have runtime considerations
 * since Throwable can be caught, but none of the VirtualMachineError instances should actually appear there.
 */
public class RejectVirtualMachineErrorIntegrationTest {
    // We will reuse these, for now, since we want to test that doing so is safe.  We may change this, in the future, is we depend on something perturbed by this.
    private static final byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static KernelInterfaceImpl kernel;
    private static Avm avm;

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
    public void rejectCatchError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectCatchError.class);
        
        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(TransactionResult.Code.FAILED_REJECTED, createResult.getStatusCode());
    }

    @Test
    public void rejectInstantiateError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectInstantiateError.class);
        
        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(TransactionResult.Code.FAILED_REJECTED, createResult.getStatusCode());
    }

    @Test
    public void rejectSubclassError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectSubclassError.class);
        
        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(TransactionResult.Code.FAILED_REJECTED, createResult.getStatusCode());
    }


    private TransactionResult deployJar(byte[] jar) {
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        TransactionResult createResult = avm.run(new TransactionContext[] {new TransactionContextImpl(create, block)})[0].get();
        return createResult;
    }
}
