package org.aion.avm.core;

import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

/**
 * Difference of energy consumption explained more in issue-345
 */
public class EnergyUsageDebugModeTest {
    private static TestingState kernel;
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;



    @BeforeClass
    public static void setup (){
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
    }

    /**
     * since the debug mode's code blocks are smaller than the normal mode, the energy consumed after throwing an exception (in debug mode) is less
     */
    @Test
    public void testEnergyConsumptionDivisionFunction(){
        long debugEnergyUsageDivision = testEnergyUsedInDebugMode(10, 0);
        long normalEnergyUsageDivision = testEnergyUsedInNormalMode(10, 0);
        assertTrue(debugEnergyUsageDivision < normalEnergyUsageDivision);
    }

    /**
     * since the debug mode's code blocks are smaller, only what is executed will be billed.
     * Thus the faster the exception is thrown, the lower the energy consumption.
     */
    @Test
    public void testEnergyConsumptionInDebug(){
        long debugEnergyUsageFailEarly = testEnergyUsedInDebugMode(10, 0);
        long debugEnergyUsageFailLate = testEnergyUsedInDebugMode(0, 10);

        assertTrue(debugEnergyUsageFailEarly < debugEnergyUsageFailLate);
    }

    private long testEnergyUsedInDebugMode(int a, int b){
        AvmConfiguration config = new AvmConfiguration();
        config.preserveDebuggability = true;
        config.enableVerboseContractErrors = true;
        AvmImpl avmDebugMode = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);

        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(EnergyUsageDebugModeTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        long energyPrice = 1l;

        //deploy in debugMode Mode
        Transaction create = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, 10_000_000l, energyPrice);
        TransactionResult createResult = avmDebugMode.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(createResult.transactionStatus.isSuccess());
        AionAddress contractAddressDebug = new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());

        long energyLimit = 1_000_000l;
        byte[] argData = encodeTryToDivideInteger(a, b);
        Transaction call = AvmTransactionUtil.call(deployer, contractAddressDebug, kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        TransactionResult result = avmDebugMode.run(kernel, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(111, new BigInteger(result.copyOfTransactionOutput().orElseThrow()).intValue());

        avmDebugMode.shutdown();
        return result.energyUsed;
    }

    private long testEnergyUsedInNormalMode(int a, int b){

        AvmImpl avmNormalMode = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(EnergyUsageDebugModeTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;

        //deploy in normal Mode
        Transaction create = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, 10_000_000l, energyPrice);
        TransactionResult createResult = avmNormalMode.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(createResult.transactionStatus.isSuccess());
        AionAddress contractAddressNormal = new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());

        byte[] argData = encodeTryToDivideInteger(a, b);
        Transaction call = AvmTransactionUtil.call(deployer,contractAddressNormal, kernel.getNonce(deployer), BigInteger.ZERO, argData, energyLimit, 1l);
        TransactionResult result = avmNormalMode.run(kernel, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(111, new BigInteger(result.copyOfTransactionOutput().orElseThrow()).intValue());
        avmNormalMode.shutdown();
        return result.energyUsed;
    }

    private static byte[] encodeTryToDivideInteger(int a, int b) {
        return new ABIStreamingEncoder()
                .encodeOneString("tryToDivideInteger")
                .encodeOneInteger(a)
                .encodeOneInteger(b)
                .toBytes();
    }
}
