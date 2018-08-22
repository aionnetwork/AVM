package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class ShadowSerializationTest {
    private static Block block;
    private static final long DEPLOY_ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    @BeforeClass
    public static void setupClass() {
        block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    }


    @Test
    public void testPersistJavaLang() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "JavaLang");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(94290289, firstHash);
        
        // Get the state of this data.
        int hash = getHash(avm, contractAddr, "JavaLang");
        Assert.assertEquals(firstHash, hash);
    }

    @Test
    public void testPersistJavaMath() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        // Populate initial data.
        int firstHash = populate(avm, contractAddr, "JavaMath");
        // For now, just do the basic verification based on knowing the number.
        Assert.assertEquals(-602588053, firstHash);
        
        // Get the state of this data.
        int hash = getHash(avm, contractAddr, "JavaMath");
        Assert.assertEquals(firstHash, hash);
    }

    /**
     * This test is just temporary to demonstrate that we can at least instantiate the NIO buffer classes and perform basic actions.
     */
    @Test
    public void testBasicJavaNio() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ShadowCoverageTarget.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, new byte[0]);
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
        
        // deploy
        Transaction tx1 = new Transaction(Transaction.Type.CREATE, Helpers.address(1), Helpers.address(2), 0, txData, DEPLOY_ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result1 = avm.run(new TransactionContextImpl(tx1, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        Address contractAddr = TestingHelper.buildAddress(result1.getReturnData());
        
        byte[] argData = ABIEncoder.encodeMethodArguments("runBasicNio");
        Transaction call = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, argData, 1_000_000L, ENERGY_PRICE);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        Assert.assertEquals(-1318544925, ((Integer)TestingHelper.decodeResult(result)).intValue());
    }


    private int populate(Avm avm, Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("populate_" + segmentName);
        Transaction call = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, argData, energyLimit, ENERGY_PRICE);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }

    private int getHash(Avm avm, Address contractAddr, String segmentName) {
        long energyLimit = 1_000_000L;
        byte[] argData = ABIEncoder.encodeMethodArguments("getHash_" + segmentName);
        Transaction call = new Transaction(Transaction.Type.CALL, Helpers.address(1), contractAddr.unwrap(), 0, argData, energyLimit, ENERGY_PRICE);
        TransactionResult result = avm.run(new TransactionContextImpl(call, block));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return ((Integer)TestingHelper.decodeResult(result)).intValue();
    }
}
