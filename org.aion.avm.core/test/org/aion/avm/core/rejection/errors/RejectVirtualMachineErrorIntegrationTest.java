package org.aion.avm.core.rejection.errors;

import java.math.BigInteger;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.*;


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
    private static final org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    @Test
    public void rejectCatchError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectCatchError.class);

        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }

    @Test
    public void rejectInstantiateError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectInstantiateError.class);

        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }

    @Test
    public void rejectSubclassError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectSubclassError.class);

        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }


    private TransactionResult deployJar(byte[] jar) {
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        return avmRule.deploy(deployer, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();
    }
}
