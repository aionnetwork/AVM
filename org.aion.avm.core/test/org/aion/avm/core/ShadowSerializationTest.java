package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ShadowSerializationTest {
    private static Block block;
    private static final long DEPLOY_ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    // Note that these numbers change pretty frequently, based on constants in the test, etc.
    private static final int HASH_JAVA_LANG = 94290322;
    private static final int HASH_JAVA_MATH = -602588053;
    private static final int HASH_API = 496;

    @BeforeClass
    public static void setupClass() {
        block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    }


    org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;

    private KernelInterface kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }


    @Test
    public void testPersistJavaLang() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // deploy
        Transaction tx1 = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result1 = avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "JavaLang");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_LANG, firstHash);
        
        // Get the state of this data.
        int hash = getHash(avm, contractAddr, "JavaLang");
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testReentrantJavaLang() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // deploy
        Transaction tx1 = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result1 = avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "JavaLang");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_LANG, firstHash);
        
        // Verify that things are consistent across reentrant modifications.
        verifyReentrantChange(avm, contractAddr, "JavaLang");
        
        // Call to verify, again, to detect the bug where reentrant serializing was incorrectly injecting constant stubs.
        verifyReentrantChange(avm, contractAddr, "JavaLang");
    }

    @Test
    public void testPersistJavaMath() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // deploy
        Transaction tx1 = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result1 = avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "JavaMath");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_MATH, firstHash);
        
        // Get the state of this data.
        int hash = getHash(avm, contractAddr, "JavaMath");
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testReentrantJavaMath() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // deploy
        Transaction tx1 = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result1 = avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "JavaMath");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_JAVA_MATH, firstHash);
        
        // Verify that things are consistent across reentrant modifications.
        verifyReentrantChange(avm, contractAddr, "JavaMath");
        
        // Call to verify, again, to detect the bug where reentrant serializing was incorrectly injecting constant stubs.
        verifyReentrantChange(avm, contractAddr, "JavaMath");
    }

    @Test
    public void testPersistApi() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // deploy
        Transaction tx1 = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result1 = avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "Api");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_API, firstHash);
        
        // Get the state of this data.
        int hash = getHash(avm, contractAddr, "Api");
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testReentrantApi() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // deploy
        Transaction tx1 = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result1 = avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result1.getResultCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "Api");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(HASH_API, firstHash);
        
        // Verify that things are consistent across reentrant modifications.
        verifyReentrantChange(avm, contractAddr, "Api");
        
        // Call to verify, again, to detect the bug where reentrant serializing was incorrectly injecting constant stubs.
        verifyReentrantChange(avm, contractAddr, "Api");
    }


    private int populate(Avm avm, Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("populate_" + segmentName);
        Transaction call = Transaction.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), kernel.getNonce(deployer).longValue(), BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE);
        AvmTransactionResult result = avm.run(new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }

    private int getHash(Avm avm, Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("getHash_" + segmentName);
        Transaction call = Transaction.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), kernel.getNonce(deployer).longValue(), BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE);
        AvmTransactionResult result = avm.run(new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }

    private void verifyReentrantChange(Avm avm, Address contractAddr, String segmentName) {
        long energyLimit = 2_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("verifyReentrantChange_" + segmentName);
        Transaction call = Transaction.call(deployer, AvmAddress.wrap(contractAddr.unwrap()), kernel.getNonce(deployer).longValue(), BigInteger.ZERO,  argData, energyLimit, ENERGY_PRICE);
        AvmTransactionResult result = avm.run(new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        Assert.assertTrue((Boolean)TestingHelper.decodeResult(result));
    }
}
