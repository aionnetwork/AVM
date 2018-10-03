package org.aion.avm.core.persistence;

import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests some of the corner cases and other complex examples of graph reachability.
 */
public class GraphReachabilityIntegrationTest {
    private byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    /**
     * Tests that a hidden object, changed via a path that is destroyed, is still observed as changed by other paths.
     * This version of the test calls in a sequence of transactions, meaning normal serialization.
     */
    @Test
    public void test249_direct() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStatic(block, contractAddr, "check249", 4);
        
        // Run test.
        callStatic(block, contractAddr, "modify249");
        
        // Verify after.
        callStatic(block, contractAddr, "check249", 5);
    }

    /**
     * Tests that a hidden object, changed via a path that is destroyed, is still observed as changed by other paths.
     * This version of the test calls in one transaction, using reentrant calls to modify the result.
     * This version only loads the changed object after the change.
     */
    @Test
    public void test249_notLoaded() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStatic(block, contractAddr, "check249", 4);
        
        // Run test.
        callStatic(block, contractAddr, "run249_reentrant_notLoaded");
        
        // Verify after.
        callStatic(block, contractAddr, "check249", 5);
    }

    /**
     * Tests that a hidden object, changed via a path that is destroyed, is still observed as changed by other paths.
     * This version of the test calls in one transaction, using reentrant calls to modify the result.
     * This version loads the object before it is changed, then verifies the value is different after the change.
     */
    @Test
    public void test249_loaded() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStatic(block, contractAddr, "check249", 4);
        
        // Run test.
        callStatic(block, contractAddr, "run249_reentrant_loaded");
        
        // Verify after.
        callStatic(block, contractAddr, "check249", 5);
    }


    private Address doInitialDeploymentAndSetup(Block block) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(GraphReachabilityIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        Transaction create = Transaction.create(deployer, kernel.getNonce(deployer), 0L, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(new TransactionContextImpl(create, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
        
        // Setup test.
        callStatic(block, contractAddr, "setup249");
        return contractAddr;
    }

    private Object callStatic(Block block, Address contractAddr, String methodName, Object... args) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, args);
        Transaction call = Transaction.call(deployer, contractAddr.unwrap(), kernel.getNonce(deployer), 0, argData, energyLimit, 1l);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return TestingHelper.decodeResult(result);
    }
}
