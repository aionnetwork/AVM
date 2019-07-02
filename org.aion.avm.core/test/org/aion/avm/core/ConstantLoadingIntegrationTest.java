package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.aion.types.TransactionResult;
import org.junit.*;


/**
 * Tests that we can do things like default methods with constants in the interface, etc, to prove
 * that constants are being correctly loaded, and can be referenced from, the constant class.
 */
public class ConstantLoadingIntegrationTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static TestingState kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testCreation() throws Exception {
        AionAddress contractAddr = deploy();
        
        // Test just the creation modes.
        int bareHash = 59;
        int bareLength = 6;
        int populateHash = 62;
        int populateLength = 3;
        byte[] bare = callStatic(contractAddr, 0);
        byte[] populated = callStatic(contractAddr, 1);
        
        Assert.assertEquals((byte)bareHash, bare[0]);
        Assert.assertEquals((byte)bareLength, bare[1]);
        Assert.assertEquals((byte)populateHash, populated[0]);
        Assert.assertEquals((byte)populateLength, populated[1]);
    }

    @Test
    public void testPersistence() throws Exception {
        AionAddress contractAddr = deploy();
        
        // Run the creation and then test the read calls.
        callStatic(contractAddr, 0);
        callStatic(contractAddr, 1);
        
        int bareHash = 59;
        int bareLength = 6;
        int populateHash = 62;
        int populateLength = 3;
        byte[] bare = callStatic(contractAddr, 2);
        byte[] populated = callStatic(contractAddr, 3);
        
        Assert.assertEquals((byte)bareHash, bare[0]);
        Assert.assertEquals((byte)bareLength, bare[1]);
        Assert.assertEquals((byte)populateHash, populated[0]);
        Assert.assertEquals((byte)populateLength, populated[1]);
    }


    private AionAddress deploy() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ConstantLoadingIntegrationTestTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        long energyLimit = 10_000_000l;
        long energyPrice = 1l;
        Transaction create = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult createResult = avm.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(createResult.transactionStatus.isSuccess());

        return new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());
    }

    private byte[] callStatic(AionAddress contractAddr, int code) {
        kernel.generateBlock();
        long energyLimit = 1_000_000l;
        byte[] argData = new byte[] { (byte)code };
        Transaction call = AvmTransactionUtil.call(deployer, contractAddr, kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        TransactionResult result = avm.run(kernel, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        return result.copyOfTransactionOutput().orElseThrow();
    }
}
