package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


public class ShadowSerializationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private static final long DEPLOY_ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    // Note that these numbers change pretty frequently, based on constants in the test, etc.
    private static final int HASH_JAVA_LANG = 94290322;
    private static final int HASH_JAVA_MATH = -602588053;
    private static final int HASH_API = 496;

    org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;

    @Test
    public void testPersistJavaLang() {
        byte[] txData = avmRule.getDappBytes(ShadowCoverageTarget.class, new byte[0]);
        
        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "JavaLang");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_LANG, firstHash);
        
        // Get the state of this data.
        int hash = getHash(contractAddr, "JavaLang");
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testReentrantJavaLang() {
        byte[] txData = avmRule.getDappBytes(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "JavaLang");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_LANG, firstHash);
        
        // Verify that things are consistent across reentrant modifications.
        verifyReentrantChange(contractAddr, "JavaLang");
        
        // Call to verify, again, to detect the bug where reentrant serializing was incorrectly injecting constant stubs.
        verifyReentrantChange(contractAddr, "JavaLang");
    }

    @Test
    public void testPersistJavaMath() {
        byte[] txData = avmRule.getDappBytes(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "JavaMath");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_MATH, firstHash);
        
        // Get the state of this data.
        int hash = getHash(contractAddr, "JavaMath");
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testReentrantJavaMath() {
        byte[] txData = avmRule.getDappBytes(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "JavaMath");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_MATH, firstHash);
        
        // Verify that things are consistent across reentrant modifications.
        verifyReentrantChange(contractAddr, "JavaMath");
        
        // Call to verify, again, to detect the bug where reentrant serializing was incorrectly injecting constant stubs.
        verifyReentrantChange(contractAddr, "JavaMath");
    }

    @Test
    public void testPersistApi() {
        byte[] txData = avmRule.getDappBytes(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "Api");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_API, firstHash);
        
        // Get the state of this data.
        int hash = getHash(contractAddr, "Api");
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testReentrantApi() {
        byte[] txData = avmRule.getDappBytes(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "Api");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_API, firstHash);
        
        // Verify that things are consistent across reentrant modifications.
        verifyReentrantChange(contractAddr, "Api");
        
        // Call to verify, again, to detect the bug where reentrant serializing was incorrectly injecting constant stubs.
        verifyReentrantChange(contractAddr, "Api");
    }


    private int populate(Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("populate_" + segmentName);
        TransactionResult result  = avmRule.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }

    private int getHash(Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("getHash_" + segmentName);
        TransactionResult result  = avmRule.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }

    private void verifyReentrantChange(Address contractAddr, String segmentName) {
        long energyLimit = 2_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("verifyReentrantChange_" + segmentName);
        TransactionResult result  = avmRule.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        Assert.assertTrue((Boolean)TestingHelper.decodeResult(result));
    }
}
