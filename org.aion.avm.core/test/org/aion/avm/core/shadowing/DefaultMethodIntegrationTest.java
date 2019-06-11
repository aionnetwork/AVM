package org.aion.avm.core.shadowing;

import avm.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionMap;
import org.aion.kernel.*;
import org.junit.*;

import java.math.BigInteger;


/**
 * Tests that we fail in a meaningful way when a DApp throws an exception due to a missing method.
 */
public class DefaultMethodIntegrationTest {
    private static org.aion.vm.api.types.Address deployer = TestingKernel.PREMINED_ADDRESS;
    private static TestingKernel kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testMissingDefaultInClinit() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(TestDefaultMethodInClinitResource.class, AionMap.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 2_000_000l;
        long energyPrice = 1l;
        TestingTransaction create = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        
        // The NoSuchMethodError triggers a "FAILED_EXCEPTION" state.
        AvmTransactionResult result = (AvmTransactionResult) avm.run(kernel, new TestingTransaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }

    @Test
    public void testMissingDefaultInMain() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(TestDefaultMethodInMainResource.class, AionMap.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 2_000_000l;
        long energyPrice = 1l;
        TestingTransaction create = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AvmTransactionResult createResult = (AvmTransactionResult) avm.run(kernel, new TestingTransaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // Setup the call (parameters are currently ignored).
        byte[] argData = new byte[0];
        TestingTransaction call = TestingTransaction.call(deployer, org.aion.vm.api.types.Address.wrap(contractAddr.toByteArray()), kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        
        // The NoSuchMethodError triggers a "FAILED_EXCEPTION" state.
        AvmTransactionResult result = (AvmTransactionResult) avm.run(kernel, new TestingTransaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
    }
}
