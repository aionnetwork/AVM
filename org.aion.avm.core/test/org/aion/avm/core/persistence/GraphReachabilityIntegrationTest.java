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
 * NOTE:  These tests very precisely measure the billing costs so changes to the fee schedule are likely to require updating these.
 * It may be worth relying on some more coarse-grained information, should it become available.
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
        callStatic(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long modify_miscCharges = 21704L + 236L + 300L + 100L + 600L + 37234L + 75L + 55L + 98L;
        long modify_storageCharges = 0L
                // read static
                    + 1161L
                // read instances (3)
                    + (3 * 1040L)
                // write static
                    + 10161L
                // write instances (3)
                    + (3 * 10040L)
                ;
        callStatic(block, contractAddr, modify_miscCharges + modify_storageCharges, "modify249");
        
        // Verify after.
        callStatic(block, contractAddr, getCost_check249(false), "check249", 5);
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
        callStatic(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long run_miscCharges = 22792L
                + 236L + 300L + 100L + 600L + 37234L + 135L + 327L + 100L + 17372L + 600L + 100L + 600L + 600L + 100L
                + 236L + 300L + 100L + 600L + 37234L + 75L + 55L + 98L
                + 100L + 108L + 100L + 50L + 55L + 50L
                ;
        long run_storageCharges = 0L
                // read static
                    + 1161L
                // read instance
                    + 1017L
                // (heap) read static
                    + 1161L
                // (heap) read instances (3)
                    + (3 * 1040L)
                // (heap) write static
                    + 10161L
                // (heap) write instances (3)
                    + (3 * 10040L)
                // read instances (3)
                    + (3 * 1040L)
                // write static
                    + 10161L
                // write instances (3)
                    + (3 * 10040L)
                // write instance
                    + 10017L
                ;
        callStatic(block, contractAddr, run_miscCharges + run_storageCharges, "run249_reentrant_notLoaded");
        
        // Verify after.
        callStatic(block, contractAddr, getCost_check249(false), "check249", 5);
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
        callStatic(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long run_miscCharges = 22600L
            + 236L + 300L + 100L + 600L + 37234L + 75L + 55L + 135L + 327L + 100L + 17372L + 600L + 100L + 600L + 600L + 100L
            + 236L + 300L + 100L + 600L + 37234L + 75L + 55L + 98L
            + 100L + 108L + 100L + 50L + 55L + 50L
            ;
        long run_storageCharges = 0L
                // read static
                    + 1161L
                // read instances (3)
                    + (3 * 1040L)
                // read instance
                    + 1017L
                // (heap) read static
                    + 1161L
                // (heap) read instances (3)
                    + (3 * 1040L)
                // (heap) write static
                    + 10161L
                // (heap) write instances (3)
                    + (3 * 10040L)
                // write static
                    + 10161L
                // write instances (3)
                    + (3 * 10040L)
                // write instance
                    + 10017L
                ;
        callStatic(block, contractAddr, run_miscCharges + run_storageCharges, "run249_reentrant_loaded");
        
        // Verify after.
        callStatic(block, contractAddr, getCost_check249(false), "check249", 5);
    }

    /**
     * Tests that we can create a new instance, reference it from an existing object, but sever the path to it.
     * This should write-back the new instance so we should be able to find it, later.
     */
    @Test
    public void testNewObjectWritebackViaUnreachablePath() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Run test.
        long run_miscCharges = 22664L
                + 236L + 300L + 100L + 600L + 37234L + 327L + 100L + 17372L + 600L + 100L + 600L + 600L + 100L
                + 236L + 300L + 100L + 600L + 37234L + 252L + 131L
                + 100L + 108L + 100L + 50L
                ;
        long run_storageCharges = 0L
                // read static
                    + 1161L
                // read instance
                    + 1025L
                // (heap) read static
                    + 1161L
                // (heap) read instances (3)
                    + (3 * 1040L)
                // (heap) write static
                    + 10161L
                // (heap) write instances (4 = 3 + new 1)
                    + (3 * 10040L)
                    + 10040L
                // write static
                    + 10161L
                // write instance
                    + 10025L
                ;
        callStatic(block, contractAddr, run_miscCharges + run_storageCharges, "runNewInstance_reentrant");
        
        // Verify result.
        long check_miscCharges = 22152L + 236L + 300L + 100L + 600L + 37234L + 80L + 600L;
        long check_storageCharges = 0L
                // read static
                    + 1161L
                // read instances (4)
                    + (4 * 1040L)
                // write static
                    + 10161L
                // write instances (4)
                    + (4 * 10040L)
                ;
        int value = (Integer) callStatic(block, contractAddr, check_miscCharges + check_storageCharges, "checkNewInstance");
        Assert.assertEquals(5, value);
    }

    /**
     * Same as above but adds another level to the call stack to make sure we hit this case in both the graph processors.
     */
    @Test
    public void testNewObjectWritebackViaUnreachablePath2() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Run test.
        long run_miscCharges = 22728L
                + 236L + 300L + 100L + 600L + 37234L + 327L + 100L + 17372L + 600L + 100L + 600L + 600L + 100L
                + 236L + 300L + 100L + 600L + 37234L + 327L + 100L + 17372L + 600L + 100L + 600L + 600L + 100L
                + 236L + 300L + 100L + 600L + 37234L + 252L + 131L
                + 100L + 108L + 100L + 50L
                + 100L + 108L + 100L + 50L
                ;
        long run_storageCharges = 0L
                // read static
                    + 1161L
                // read instance
                    + 1032L
                // (heap) read static
                    + 1161L
                // (heap) read instance
                    + 1025L
                // (heap) read static
                    + 1161L
                // (heap) read instances (3)
                    + (3 * 1040L)
                // (heap) write static
                    + 10161L
                // (heap) write instances (4 = 3 + new 1)
                    + (3 * 10040L)
                    + 10040L
                // (heap) write static
                    + 10161L
                // (heap) write instance
                    + 10025L
                // write static
                    + 10161L
                // write instance
                    + 10032L
                ;
        callStatic(block, contractAddr, run_miscCharges + run_storageCharges, "runNewInstance_reentrant2");
        
        // Verify result.
        long check_miscCharges = 22152L + 236L + 300L + 100L + 600L + 37234L + 80L + 600L;
        long check_storageCharges = 0L
                // read static
                    + 1161L
                // read instances (4)
                    + (4 * 1040L)
                // write static
                    + 10161L
                // write instances (4)
                    + (4 * 10040L)
                ;
        int value = (Integer) callStatic(block, contractAddr, check_miscCharges + check_storageCharges, "checkNewInstance");
        Assert.assertEquals(5, value);
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
        
        // Check that the deployment cost is what we expected.
        long miscCharges = 143844L + 36046L + 406200L + 375L + 300L + 1500L + 6L + 53L;
        long storageCharges = 0L
                // static
                    + 10161L
                // instance
                    + 10032L
                // instance
                    + 10017L
                // instance
                    + 10025L
                ;
        Assert.assertEquals(miscCharges + storageCharges, createResult.getEnergyUsed());
        
        // Setup test.
        long expectedMiscCharges = 21640L + 236L + 300L + 100L + 600L + 37234L + 973L + 131L + 131L + 131L + 131L + 131L;
        long expectedStorageCharges = 0L
                // read static
                    + 1161L
                // write static
                    + 10161L
                // instance
                    + 10040L
                // instance
                    + 10040L
                // instance
                    + 10040L
                // instance
                    + 10040L
                // instance
                    + 10040L
                ;
        callStatic(block, contractAddr, expectedMiscCharges + expectedStorageCharges, "setup249");
        return contractAddr;
    }

    private Object callStatic(Block block, Address contractAddr, long expectedCost, String methodName, Object... args) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, args);
        Transaction call = Transaction.call(deployer, contractAddr.unwrap(), kernel.getNonce(deployer), 0, argData, energyLimit, 1l);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        Assert.assertEquals(expectedCost, result.getEnergyUsed());
        return TestingHelper.decodeResult(result);
    }

    private static long getCost_check249(boolean before) {
        long miscCharges = 21780L + 236L + 300L + 100L + 600L + 37234L + 100L + 100L + 75L + 55L + 67L;
        // We end up with a slightly different cost before/after changes.
        if (before) {
            miscCharges += 48L + 79L + 50L;
        } else {
            miscCharges += 50L;
        }
        long storageCharges = 0L
                // read static
                    + 1161L
                // read instances (5)
                    + (5 * 1040L)
                // write static
                    + 10161L
                // write instances (5)
                    + (5 * 10040L)
                ;
        return miscCharges + storageCharges;
    }
}
