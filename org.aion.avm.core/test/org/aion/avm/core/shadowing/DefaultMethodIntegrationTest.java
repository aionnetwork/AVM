package org.aion.avm.core.shadowing;

import org.aion.avm.api.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.StandardCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionMap;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Tests that we fail in a meaningful way when a DApp throws an exception due to a missing method.
 */
public class DefaultMethodIntegrationTest {
    private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private KernelInterfaceImpl kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testMissingDefaultInClinit() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(TestDefaultMethodInClinitResource.class, AionMap.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 2_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        
        // The NoSuchMethodError triggers a "FAILED_EXCEPTION" state.
        AvmTransactionResult result = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {TransactionContextImpl.forExternalTransaction(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }

    @Test
    public void testMissingDefaultInMain() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        byte[] jar = JarBuilder.buildJarForMainAndClasses(TestDefaultMethodInMainResource.class, AionMap.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 2_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AvmTransactionResult createResult = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {TransactionContextImpl.forExternalTransaction(create, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // Setup the call (parameters are currently ignored).
        byte[] argData = new byte[0];
        Transaction call = Transaction.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        
        // The NoSuchMethodError triggers a "FAILED_EXCEPTION" state.
        AvmTransactionResult result = (AvmTransactionResult) avm.run(this.kernel, new TransactionContext[] {TransactionContextImpl.forExternalTransaction(call, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }
}
