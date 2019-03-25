package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.InstrumentationBasedStorageFees;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.kernel.AvmTransactionResult;
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
    private static final int HASH_JAVA_LANG = 94291514;
    private static final int HASH_JAVA_MATH = -602588053;
    private static final int HASH_API = 496;

    Address deployer = avmRule.getPreminedAccount();

    @Test
    public void testPersistJavaLang() {
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);
        
        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
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
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
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
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
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
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
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
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
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
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);

        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "Api");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_API, firstHash);
        
        // Verify that things are consistent across reentrant modifications.
        verifyReentrantChange(contractAddr, "Api");
        
        // Call to verify, again, to detect the bug where reentrant serializing was incorrectly injecting constant stubs.
        verifyReentrantChange(contractAddr, "Api");
    }

    @Test
    public void testEnergyLoadingJavaLang() {
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);
        
        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "JavaLang");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_LANG, firstHash);
        
        long energyLimit = 0;
        int hash = 0;
        while (0 == hash) {
            hash = getHashSuccessWithLimit(contractAddr, "JavaLang", energyLimit);
            // Allow at most one more read to succeed on the next attempt.
            energyLimit += InstrumentationBasedStorageFees.FIXED_READ_COST;
        }
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testEnergyLoadingJavaMath() {
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);
        
        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "JavaMath");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_MATH, firstHash);
        
        long energyLimit = 0;
        int hash = 0;
        while (0 == hash) {
            hash = getHashSuccessWithLimit(contractAddr, "JavaMath", energyLimit);
            // Allow at most one more read to succeed on the next attempt.
            energyLimit += InstrumentationBasedStorageFees.FIXED_READ_COST;
        }
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testEnergyLoadingApi() {
        byte[] txData = avmRule.getDappBytesWithUserlib(ShadowCoverageTarget.class, new byte[0]);
        
        // deploy
        TransactionResult result1 = avmRule.deploy(deployer, BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = new Address(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(contractAddr, "Api");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_API, firstHash);
        
        long energyLimit = 0;
        int hash = 0;
        while (0 == hash) {
            hash = getHashSuccessWithLimit(contractAddr, "Api", energyLimit);
            // Allow at most one more read to succeed on the next attempt.
            energyLimit += InstrumentationBasedStorageFees.FIXED_READ_COST;
        }
        Assert.assertEquals(firstHash, hash);
    }


    private int populate(Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("populate_" + segmentName);
        TransactionResult result  = avmRule.call(deployer, contractAddr, BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ((Integer) ABIDecoder.decodeOneObject(result.getReturnData())).intValue();
    }

    private int getHash(Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("getHash_" + segmentName);
        TransactionResult result  = avmRule.call(deployer, contractAddr, BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ((Integer)ABIDecoder.decodeOneObject(result.getReturnData())).intValue();
    }

    private int getHashSuccessWithLimit(Address contractAddr, String segmentName, long energyLimit) {
        byte[] argData = ABIEncoder.encodeMethodArguments("getHash_" + segmentName);
        TransactionResult result  = avmRule.call(deployer, contractAddr, BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE).getTransactionResult();
        return (AvmTransactionResult.Code.SUCCESS == result.getResultCode())
                ? ((Integer)ABIDecoder.decodeOneObject(result.getReturnData())).intValue()
                : 0;
    }

    private void verifyReentrantChange(Address contractAddr, String segmentName) {
        long energyLimit = 2_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("verifyReentrantChange_" + segmentName);
        TransactionResult result  = avmRule.call(deployer, contractAddr, BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        Assert.assertTrue((Boolean)ABIDecoder.decodeOneObject(result.getReturnData()));
    }
}
