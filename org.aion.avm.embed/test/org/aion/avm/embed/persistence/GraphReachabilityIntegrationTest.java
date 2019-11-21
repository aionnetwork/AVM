package org.aion.avm.embed.persistence;

import avm.Address;
import org.aion.avm.RuntimeMethodFeeSchedule;
import org.aion.avm.StorageFees;
import org.aion.avm.core.BillingRules;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.avm.tooling.deploy.eliminator.UnreachableMethodRemover;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;


/**
 * Tests some of the corner cases and other complex examples of graph reachability.
 * NOTE:  These tests very precisely measure the billing costs so changes to the fee schedule are likely to require updating these.
 * It may be worth relying on some more coarse-grained information, should it become available.
 */
public class GraphReachabilityIntegrationTest {
    private static final int GRAPH_SIZE_INITIAL = 1045;
    private static final int GRAPH_SIZE_BEFORE = 1464;
    private static final int GRAPH_SIZE_AFTER  = 1460;
    private static final int JAR_SIZE_JDK10 = 4069;
    private static final int JAR_SIZE_JDK11 = 4067;

    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address deployer = avmRule.getPreminedAccount();

    /**
     * Tests that a hidden object, changed via a path that is destroyed, is still observed as changed by other paths.
     * This version of the test calls in a sequence of transactions, meaning normal serialization.
     */
    @Test
    public void test249_direct() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStaticVoid(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("modify249").toBytes());
        Assert.assertEquals(21708L, transactionCost);
        
        long storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long executionCost = getCost_modify249();
        long storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        
        long storageCosts = storageReadCost + storageWriteCost;
        
        callStaticVoid(block, contractAddr, transactionCost + storageCosts + executionCost, "modify249");
        
        // Verify after.
        callStaticVoid(block, contractAddr, getCost_check249(false), "check249", 5);
    }

    /**
     * Tests that a hidden object, changed via a path that is destroyed, is still observed as changed by other paths.
     * This version of the test calls in one transaction, using reentrant calls to modify the result.
     * This version only loads the changed object after the change.
     */
    @Test
    public void test249_notLoaded() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStaticVoid(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("run249_reentrant_notLoaded").toBytes());
        Assert.assertEquals(22796L, transactionCost);
        
        long storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long main = cost_mainPreamble();
        long decodeMethodName = cost_decodeMethodNamePre() + 78L + 100L + 126L + cost_decodeMethodNamePost("run249_reentrant_notLoaded");
        long main2 = (63 + 340 + 63 + 340 + 63 + 430) + 94L;
        long run249_reentrant_notLoaded = 88L + 134L + 29L + 223L + 645L + 36L + 100L + 109L + 26L + 100L + 5000L;
        
        long nested_storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long nested_modify249 = getCost_modify249();
        long nested_storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        long nestedCost = nested_storageReadCost + nested_modify249 + nested_storageWriteCost;
        Assert.assertEquals(11547L, nestedCost);
        
        long newResult = 300L;
        long run249_reentrant_notLoaded2 = 60L + 100L + 23L + 29L + 23L;
        long main3 = cost_mainPostamble();
        long storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        
        long storageCosts = storageReadCost + storageWriteCost;
        long executionCost = main + decodeMethodName + main2 + run249_reentrant_notLoaded + nestedCost + newResult + run249_reentrant_notLoaded2 + main3;
        
        callStaticVoid(block, contractAddr, transactionCost + storageCosts + executionCost, "run249_reentrant_notLoaded");
        
        // Verify after.
        callStaticVoid(block, contractAddr, getCost_check249(false), "check249", 5);
    }

    /**
     * Tests that a hidden object, changed via a path that is destroyed, is still observed as changed by other paths.
     * This version of the test calls in one transaction, using reentrant calls to modify the result.
     * This version loads the object before it is changed, then verifies the value is different after the change.
     */
    @Test
    public void test249_loaded() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Verify before.
        callStaticVoid(block, contractAddr, getCost_check249(true), "check249", 4);
        
        // Run test.
        long transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("run249_reentrant_loaded").toBytes());
        Assert.assertEquals(22604L, transactionCost);
        
        long storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long main = cost_mainPreamble();
        long decodeMethodName = cost_decodeMethodNamePre() + 69L + 100L + 123L + cost_decodeMethodNamePost("run249_reentrant_loaded");
        long main2 = (63 + 340 + 63 + 340 + 63 + 415 + 63 + 415) + 94L;
        long run249_reentrant_loaded = 65L + 29L + 88L + 134L + 29L + 223L + 645L + 36L + 100L + 109L + 26L + 100L + 5000L;
        
        long nested_storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long nested_modify249 = getCost_modify249();
        long nested_storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        long nestedCost = nested_storageReadCost + nested_modify249 + nested_storageWriteCost;
        Assert.assertEquals(11547L, nestedCost);
        
        long newResult = 300L;
        long run249_reentrant_loaded2 = 60L + 100L + 23L + 29L + 23L;
        long main3 = cost_mainPostamble();
        long storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        
        long storageCosts = storageReadCost + storageWriteCost;
        long executionCost = main + decodeMethodName + main2 + run249_reentrant_loaded + nestedCost + newResult + run249_reentrant_loaded2 + main3;
        
        callStaticVoid(block, contractAddr, transactionCost + storageCosts + executionCost, "run249_reentrant_loaded");
        
        // Verify after.
        callStaticVoid(block, contractAddr, getCost_check249(false), "check249", 5);
    }

    /**
     * Tests that we can create a new instance, reference it from an existing object, but sever the path to it.
     * This should write-back the new instance so we should be able to find it, later.
     */
    @Test
    public void testNewObjectWritebackViaUnreachablePath() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Run test.
        long transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("runNewInstance_reentrant").toBytes());
        Assert.assertEquals(22668L, transactionCost);
        
        long combinedCost = getCost_runNewInstance_reentrant(transactionCost, "runNewInstance_reentrant", "modifyNewInstance");
        
        callStaticVoid(block, contractAddr, combinedCost, "runNewInstance_reentrant");
        
        verifyResult(block, contractAddr);
    }

    /**
     * Same as above but adds another level to the call stack to make sure we hit this case in both the graph processors.
     */
    @Test
    public void testNewObjectWritebackViaUnreachablePath2() throws Exception {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        Address contractAddr = doInitialDeploymentAndSetup(block);
        
        // Run test.
        long transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("runNewInstance_reentrant2").toBytes());
        Assert.assertEquals(22732L, transactionCost);
        
        long storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long main = cost_mainPreamble();
        long decodeMethodName = cost_decodeMethodNamePre() + 75L + 100L + 125L + cost_decodeMethodNamePost("runNewInstance_reentrant2");
        long main2 = (63 + 340 + 63 + 340 + 63 + 425 + 63 + 415 + 63 + 420 + 63 + 425) + 94L;
        long runNewInstance_reentrant = 134L + 29L + 223L + 720L + 81L + 100L + 124L + 26L + 100L + 5000L;
        
        long nestedCost = getCost_runNewInstance_reentrant(0, "runNewInstance_reentrant", "modifyNewInstance");
        Assert.assertEquals(29832L, nestedCost);
        
        long newResult = 300L;
        long runNewInstance_reentrant2 = 60L + 100L + 23L;
        long main3 = cost_mainPostamble();
        long storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        
        long storageCosts = storageReadCost + storageWriteCost;
        long executionCost = main + decodeMethodName + main2 + runNewInstance_reentrant + nestedCost + newResult + runNewInstance_reentrant2 + main3;
        
        callStaticVoid(block, contractAddr, transactionCost + storageCosts + executionCost, "runNewInstance_reentrant2");
        
        verifyResult(block, contractAddr);
    }


    private Address doInitialDeploymentAndSetup(TestingBlock block) throws Exception {
        // The assertions in this method depends on the gas charged, which in turn depends on the exact size of the jar file.
        // The AvmRule invokes the ABICompiler on all input jars.
        // As a result, we have to run the ABICompiler on the input jar to get the correct expected gas values.
        JarOptimizer optimizer = new JarOptimizer(false);
        ABICompiler compiler = ABICompiler.compileJarBytes(JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(GraphReachabilityIntegrationTestTarget.class, Collections.emptyMap()));
        byte[] optimizedJar = optimizer.optimize(compiler.getJarFileBytes());
        optimizedJar = UnreachableMethodRemover.optimize(optimizedJar);
        // The size of the JAR shifts between versions of the JDK since they changed the line wrapping of the MANIFEST.MF in JDK 11, for some reason.
        boolean isNewManifest = (JAR_SIZE_JDK11 == optimizedJar.length);
        if (!isNewManifest) {
            // This needs to be the old size.
            Assert.assertEquals(JAR_SIZE_JDK10, optimizedJar.length);
        }
        byte[] txData = new CodeAndArguments(optimizedJar, new byte[0]).encodeToBytes();

        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;

        TransactionResult createResult = avmRule.deploy(deployer, BigInteger.ZERO, txData, energyLimit, energyPrice).getTransactionResult();
        Assert.assertTrue(createResult.transactionStatus.isSuccess());
        Address contractAddr = new Address(createResult.copyOfTransactionOutput().orElseThrow());
        
        // Check that the deployment cost is what we expected.
        // The first three numbers here are: basic cost of tx, processing cost and storage cost
        long basicCost = BillingRules.getBasicTransactionCost(txData);
        long expectedTransactionCost = isNewManifest ? 266920L : 267048;
        Assert.assertEquals(expectedTransactionCost, basicCost);
        int numberOfClasses = 4;
        long codeInstantiationOfDeploymentFee = BillingRules.getDeploymentFee(numberOfClasses, optimizedJar.length);
        long expectedDeploymentFee = isNewManifest ? 208067L : 208069;
        Assert.assertEquals(expectedDeploymentFee, codeInstantiationOfDeploymentFee);
        long clinit = 60L;
        long assertionStatus = 100L;
        long clinit2 = 3L + 31L;
        long miscCharges = basicCost + codeInstantiationOfDeploymentFee + clinit + assertionStatus + clinit2;
        long storageCharges = GRAPH_SIZE_INITIAL * StorageFees.WRITE_PRICE_PER_BYTE;

        long totalExpectedCost = miscCharges + storageCharges;

        Assert.assertEquals(totalExpectedCost, createResult.energyUsed);

        // Setup test.
        callStaticVoid(block, contractAddr, getCost_setup249(), "setup249");
        return contractAddr;
    }

    private int callStaticInt(TestingBlock block, Address contractAddr, long expectedCost, String methodName, Object... args) {
        byte[] result = callStaticSuccess(block, contractAddr, expectedCost, methodName, args);
        return new ABIDecoder(result).decodeOneInteger();
    }

    private void callStaticVoid(TestingBlock block, Address contractAddr, long expectedCost, String methodName, Object... args) {
        byte[] result = callStaticSuccess(block, contractAddr, expectedCost, methodName, args);
        Assert.assertArrayEquals(new byte[0], result);
    }

    private byte[] callStaticSuccess(TestingBlock block, Address contractAddr, long expectedCost, String methodName, Object... args) {
        long energyLimit = 1_000_000l;
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, args);
        TransactionResult result = avmRule.call(deployer, contractAddr, BigInteger.ZERO, argData, energyLimit, 1l).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(expectedCost, result.energyUsed);
        return result.copyOfTransactionOutput().orElseThrow();
    }

    private static long getCost_check249(boolean before) {
        int graphSize = before ? GRAPH_SIZE_BEFORE : GRAPH_SIZE_AFTER;
        
        // (Just pass in any integer for this sizing)
        long transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("check249").encodeOneInteger(4).toBytes());
        long storageReadCost = graphSize * StorageFees.READ_PRICE_PER_BYTE;
        long main = cost_mainPreamble();
        long decodeMethodName = cost_decodeMethodNamePre() + 24L + 100L + 108L + cost_decodeMethodNamePost("check249");
        long main2 = 63L + 340L + 63L + 340L + 140L;
        long decodeOneInteger = 79L + 37L + 39L + 23L + 76L + 49L + 258L;
        long main3 = 65L + 29L + 50L;
        long main4 = before
                ? 30L + 71L
                : 0L;
        long main5 = 23L + cost_mainPostamble();
        long storageWriteCost = graphSize * StorageFees.WRITE_PRICE_PER_BYTE;
        
        long storageCosts = storageReadCost + storageWriteCost;
        long executionCost = main + decodeMethodName + main2 + decodeOneInteger + main3 + main4 + main5;
        
        return transactionCost + storageCosts + executionCost;
    }

    private static long getCost_setup249() {
        long transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("setup249").toBytes());
        Assert.assertEquals(21644L, transactionCost);
        long storageReadCost = GRAPH_SIZE_INITIAL * StorageFees.READ_PRICE_PER_BYTE;
        long main = cost_mainPreamble() + 37L + 39L + 41L + 114L;
        long checkNullEmptyData = 37L + 39L + 23L;
        long checkMinLengthForObject = 53L + 23L;
        long decodeMethodName = 76L + 53L + 82L + 148L + 256L + 24L + 100L + 108L + cost_decodeMethodNamePost("setup249");
        long main2 = 63L + 340L + 94L;
        long setup249 = 896L + (5 * 63L);
        long main3 = cost_mainPostamble();
        long storageWriteCost = GRAPH_SIZE_BEFORE * StorageFees.WRITE_PRICE_PER_BYTE;

        long storageCosts = storageReadCost + storageWriteCost;
        long executionCost = main + checkNullEmptyData + checkMinLengthForObject + decodeMethodName + main2 + setup249 + main3;
        
        return transactionCost + storageCosts + executionCost;
    }

    private long getCost_modify249() {
        long main = cost_mainPreamble();
        long decodeMethodName = cost_decodeMethodNamePre() + 27L + 100L + 109L + cost_decodeMethodNamePost("modify249");
        long main2 = (63 + 340 + 63 + 340 + 63 + 345 + 63 + 345 + 63 + 345 + 63 + 345 + 63 + 345 + 63 + 345) + 94L;
        long modify249 = 65L + 29L + 85L;
        long main3 = cost_mainPostamble();
        return main + decodeMethodName + main2 + modify249 + main3;
    }

    private long getCost_nestedReentrantMethod(String methodName) {
        long main = cost_mainPreamble();
        long decodeMethodName = cost_decodeMethodNamePre() + 51L + 100L + 117L + cost_decodeMethodNamePost(methodName);
        long main2 = (63 + 340 + 63 + 340 + 63 + 385 + 63 + 385 + 63 + 385 + 63 + 385 + 63 + 380 + 63 + 345 + 63 + 385) + 94L;
        long modifyNewInstance = 230L + 63L;
        long main3 = cost_mainPostamble();
        return main + decodeMethodName + main2 + modifyNewInstance + main3;
    }

    private long getCost_runNewInstance_reentrant(long transactionCost, String methodName, String reentrantMethodName) {
        long storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long main = cost_mainPreamble();
        long decodeMethodName = cost_decodeMethodNamePre() + 72L + 100L + 124L + cost_decodeMethodNamePost(methodName);
        long main2 = (63 + 340 + 63 + 340 + 63 + 420 + 63 + 415 + 63 + 420) + 94L;
        long runNewInstance_reentrant = 134L + 29L + 223L + 685L + 60L + 100L + 117L + 26L + 100L + 5000L;
        
        long nested_storageReadCost = GRAPH_SIZE_BEFORE * StorageFees.READ_PRICE_PER_BYTE;
        long nested_reeentrant_method = getCost_nestedReentrantMethod(reentrantMethodName);
        long nested_storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        long nestedCost = nested_storageReadCost + nested_reeentrant_method + nested_storageWriteCost;
        Assert.assertEquals(12376L, nestedCost);
        
        long newResult = 300L;
        long runNewInstance_reentrant2 = 60L + 100L + 23L;
        long main3 = cost_mainPostamble();
        long storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        
        long storageCosts = storageReadCost + storageWriteCost;
        long executionCost = main + decodeMethodName + main2 + runNewInstance_reentrant + nestedCost + newResult + runNewInstance_reentrant2 + main3;
        return transactionCost + storageCosts + executionCost;
    }

    private void verifyResult(TestingBlock block, Address contractAddr) {
        long check_transactionCost = BillingRules.getBasicTransactionCost(new ABIStreamingEncoder().encodeOneString("checkNewInstance").toBytes());
        Assert.assertEquals(22156L, check_transactionCost);
        long check_storageReadCost = GRAPH_SIZE_AFTER * StorageFees.READ_PRICE_PER_BYTE;
        long check_main = cost_mainPreamble();
        long check_decodeMethodName = cost_decodeMethodNamePre() + 48L + 100L + 116L + cost_decodeMethodNamePost("checkNewInstance");
        long check_main2 = (63 + 340 + 63 + 340 + 63 + 380 + 63 + 380 + 63 + 380 + 63 + 380 + 63 + 380) + 69L;
        long checkNewInstance = 63L;
        long check_main3 = 212L + 15L + 100L;
        long check_storageWriteCost = GRAPH_SIZE_AFTER * StorageFees.WRITE_PRICE_PER_BYTE;
        
        long check_storageCosts = check_storageReadCost + check_storageWriteCost;
        long check_executionCost = check_main + check_decodeMethodName + check_main2 + checkNewInstance + check_main3;
        
        int value = callStaticInt(block, contractAddr, check_transactionCost + check_storageCosts + check_executionCost, "checkNewInstance");
        Assert.assertEquals(5, value);
    }

    // Observed energy charges for common idioms: (cost_* methods).

    private static long cost_decodeMethodNamePre() {
        return 37L + 39L + 41L + 114L + 37L + 39L + 23L + 53L + 23L + 76L + 53L + 82L + 148L + 256L;
    }

    private static long cost_decodeMethodNamePost(String methodName) {
        return 300L + (methodName.getBytes().length * RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR_LEVEL_2) + 26L + 26L;
    }

    private static long cost_mainPostamble() {
        return 0L + 100L;
    }

    private static long cost_mainPreamble() {
        return 245L + 100L + 77L;
    }
}
