package org.aion.avm.tooling.persistence;

import avm.Address;
import org.aion.avm.StorageFees;
import org.aion.avm.core.BillingRules;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Tests some of the corner cases and other complex examples of graph reachability.
 * NOTE:  These tests very precisely measure the billing costs so changes to the fee schedule are likely to require updating these.
 * It may be worth relying on some more coarse-grained information, should it become available.
 */
public class GraphReachabilityIntegrationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address deployer = avmRule.getPreminedAccount();

    /**
     * Tests that a hidden object, changed via a path that is destroyed, is still observed as changed by other paths.
     * This version of the test calls in a sequence of transactions, meaning normal serialization.
     */
    @Test
    public void test249_direct() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStatic(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long modify_basicCost = adjustBasicCost(21708L);
        long modify_miscCharges = 95L + 300L + 100L + 37234L + 65L + 29L + 85L;
        int graphSizeBefore = 4950;
        int graphSizeAfter = 4946;
        int readCost = StorageFees.READ_PRICE_PER_BYTE * graphSizeBefore;
        int writeCost = StorageFees.WRITE_PRICE_PER_BYTE * graphSizeAfter;
        long modify_storageCharges = readCost + writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long userlibCost = -28136;
        callStatic(block, contractAddr, modify_basicCost + modify_miscCharges + modify_storageCharges + userlibCost, "modify249");
        
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
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStatic(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long run_basicCost = adjustBasicCost(22796L);
        long run_miscCharges = 0L
                + 95L + 300L + 100L + 37234L + 88L + 187L + 100L + 17372L + 600L + 5000L + 100L
                + 95L + 100L + 37234L + 65L + 29L + 85L
                + 100L + 60L + 100L + 23L + 29L + 23L
                ;
        int graphSizeBefore = 4950;
        int graphSizeAfter = 4946;
        int readCost = StorageFees.READ_PRICE_PER_BYTE * graphSizeBefore;
        int writeCost = StorageFees.WRITE_PRICE_PER_BYTE * graphSizeAfter;
        // 2 reads/writes of the same cost.
        long run_storageCharges = 2 * readCost + 2 * writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long userlibCost = -74100;

        callStatic(block, contractAddr, run_basicCost + run_miscCharges + run_storageCharges + userlibCost, "run249_reentrant_notLoaded");
        
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
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStatic(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long run_basicCost = adjustBasicCost(22604L);
        long run_miscCharges = 0L
            + 95L + 300L + 100L + 37234L + 65L + 29L + 88L + 187L + 100L + 17372L + 600L + 5000L + 100L
            + 95L + 100L + 37234L + 65L + 29L + 85L
            + 100L + 60L + 100L + 23L + 29L + 23L
            ;
        int graphSizeBefore = 4950;
        int graphSizeAfter = 4946;
        int readCost = StorageFees.READ_PRICE_PER_BYTE * graphSizeBefore;
        int writeCost = StorageFees.WRITE_PRICE_PER_BYTE * graphSizeAfter;
        // 2 reads/writes of the same cost.
        long run_storageCharges = 2 * readCost + 2 * writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long userlibCost = -73374;

        callStatic(block, contractAddr, run_basicCost + run_miscCharges + run_storageCharges + userlibCost, "run249_reentrant_loaded");
        
        // Verify after.
        callStatic(block, contractAddr, getCost_check249(false), "check249", 5);
    }

    /**
     * Tests that we can create a new instance, reference it from an existing object, but sever the path to it.
     * This should write-back the new instance so we should be able to find it, later.
     */
    @Test
    public void testNewObjectWritebackViaUnreachablePath() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Run test.
        long run_basicCost = adjustBasicCost(22668L);
        long run_miscCharges = 0L
                + 95L + 300L + 100L + 37234L + 187L + 100L + 17372L + 600L + 5000L + 100L
                + 95L + 100L + 37234L + 194L + 63L
                + 100L + 60L + 100L + 23L
                ;
        int graphSizeBefore = 4950;
        int graphSizeAfter = 4946;
        int readCost = StorageFees.READ_PRICE_PER_BYTE * graphSizeBefore;
        int writeCost = StorageFees.WRITE_PRICE_PER_BYTE * graphSizeAfter;
        // 2 reads/writes of the same cost.
        long run_storageCharges = 2 * readCost + 2 * writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long run_userlibCost = -71385;

        callStatic(block, contractAddr, run_basicCost + run_miscCharges + run_storageCharges + run_userlibCost, "runNewInstance_reentrant");
        
        // Verify result.
        long check_basicCost = adjustBasicCost(22156L);
        long check_miscCharges = 0L + 95L + 300L + 100L + 37234L + 63L + 600L;
        long check_storageCharges = (StorageFees.READ_PRICE_PER_BYTE * graphSizeAfter) + writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long check_userlibCost = -28974;

        int value = (Integer) callStatic(block, contractAddr, check_basicCost + check_miscCharges + check_storageCharges + check_userlibCost, "checkNewInstance");
        Assert.assertEquals(5, value);
    }

    /**
     * Same as above but adds another level to the call stack to make sure we hit this case in both the graph processors.
     */
    @Test
    public void testNewObjectWritebackViaUnreachablePath2() throws Exception {
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Run test.
        long run_basicCost = adjustBasicCost(22732L);
        long run_miscCharges = 0L
                + 95L + 300L + 100L + 37234L + 187L + 100L + 17372L + 600L + 5000L + 100L
                + 95L + 100L + 37234L + 187L + 100L + 17372L + 600L + 5000L + 100L
                + 95L + 100L + 37234L + 194L + 63L
                + 100L + 60L + 100L + 23L
                + 100L + 60L + 100L + 23L
                ;
        int graphSizeBefore = 4950;
        int graphSizeAfter = 4946;
        int readCost = StorageFees.READ_PRICE_PER_BYTE * graphSizeBefore;
        int writeCost = StorageFees.WRITE_PRICE_PER_BYTE * graphSizeAfter;
        // 3 reads/writes of the same cost.
        long run_storageCharges = 3 * readCost + 3 * writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long run_userlibCost = -114854;

        callStatic(block, contractAddr, run_basicCost + run_miscCharges + run_storageCharges + run_userlibCost, "runNewInstance_reentrant2");
        
        // Verify result.
        long check_basicCost = adjustBasicCost(22156L);
        long check_miscCharges = 95L + 300L + 100L + 37234L + 63L + 600L;
        // Reads/write of the same cost.
        long check_storageCharges = (StorageFees.READ_PRICE_PER_BYTE * graphSizeAfter) + writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long check_userlibCost = -28974;

        int value = (Integer) callStatic(block, contractAddr, check_basicCost + check_miscCharges + check_storageCharges + check_userlibCost, "checkNewInstance");
        Assert.assertEquals(5, value);
    }


    private Address doInitialDeploymentAndSetup(Block block) {
        // The assertions in this method depends on the gas charged, which in turn depends on the exact size of the jar file.
        // The AvmRule invokes the ABICompiler on all input jars.
        // As a result, we have to run the ABICompiler on the input jar to get the correct expected gas values.
        JarOptimizer optimizer = new JarOptimizer(false);
        ABICompiler compiler = ABICompiler.compileJarBytes(JarBuilder.buildJarForMainAndClasses(GraphReachabilityIntegrationTestTarget.class));
        byte[] optimizedJar = optimizer.optimize(compiler.getJarFileBytes());
        byte[] txData = new CodeAndArguments(optimizedJar, new byte[0]).encodeToBytes();

        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;

        AvmTransactionResult createResult = (AvmTransactionResult) avmRule.deploy(deployer, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        Address contractAddr = new Address(createResult.getReturnData());
        
        // Check that the deployment cost is what we expected.
        // The first three numbers here are: basic cost of tx, processing cost and storage cost
        long basicCost = BillingRules.getBasicTransactionCost(txData);
        long codeInstantiationOfDeploymentFee = BillingRules.getDeploymentFee(11, optimizedJar.length);
        long clinit = 83L;
        long assertionStatus = 100L;
        long clinit2 = 3L + 31L;
        long miscCharges = basicCost + codeInstantiationOfDeploymentFee + clinit + assertionStatus + clinit2;
        // One write of 13563L.
        long storageCharges = 13563L;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long userlibCost = -7000L;

        long totalExpectedCost = miscCharges + storageCharges + userlibCost;

        Assert.assertEquals(totalExpectedCost, createResult.getEnergyUsed());
        Assert.assertEquals(energyLimit - totalExpectedCost, createResult.getEnergyRemaining());
        
        // Setup test.
        callStatic(block, contractAddr, getCost_setup249(), "setup249");
        return contractAddr;
    }

    private Object callStatic(Block block, Address contractAddr, long expectedCost, String methodName, Object... args) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, args);
        AvmTransactionResult result = (AvmTransactionResult) avmRule.call(deployer, contractAddr, BigInteger.ZERO, argData, energyLimit, 1l).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        Assert.assertEquals(expectedCost, result.getEnergyUsed());
        Assert.assertEquals(energyLimit - expectedCost, result.getEnergyRemaining());
        return ABIUtil.decodeOneObject(result.getReturnData());
    }

    private static long getCost_check249(boolean before) {
        long basicCost = adjustBasicCost(21784L);
        long miscCharges = 95L + 300L + 100L + 37234L + 65L + 29L + 50L;
        // We end up with a slightly different cost before/after changes.
        if (before) {
            miscCharges += 30L + 71L + 23L;
        } else {
            miscCharges += 23L;
        }
        int graphSizeBefore = 4950;
        int graphSizeAfter = 4946;
        long storageCharges = before
                ? (StorageFees.READ_PRICE_PER_BYTE * graphSizeBefore) + (StorageFees.WRITE_PRICE_PER_BYTE * graphSizeBefore)
                : (StorageFees.READ_PRICE_PER_BYTE * graphSizeAfter) + (StorageFees.WRITE_PRICE_PER_BYTE * graphSizeAfter);

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long userlibCost = -31821;
        return basicCost + miscCharges + storageCharges + userlibCost;
    }

    private static long getCost_setup249() {

        long basicCost = adjustBasicCost(21644L);
        long miscCharges = 95L + 300L + 100L + 37234L + 716L + 63L + 63L + 63L + 63L + 63L;
        int graphSizeBefore = 4521;
        int graphSizeAfter = 4950;
        int readCost = StorageFees.READ_PRICE_PER_BYTE * graphSizeBefore;
        int writeCost = StorageFees.WRITE_PRICE_PER_BYTE * graphSizeAfter;
        long storageCharges = readCost + writeCost;

        // This number is an adjustment factor for the cost changes associated with the various ABI improvements
        // TODO (AKI-120): Get rid of this number, by adjusting the precise measures in the factors above
        long userlibCost = -33136;

        return basicCost + miscCharges + storageCharges + userlibCost;
    }

    private static long adjustBasicCost(long cost) {
        return cost;
    }
}
