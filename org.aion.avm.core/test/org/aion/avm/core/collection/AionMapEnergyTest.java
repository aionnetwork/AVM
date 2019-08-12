package org.aion.avm.core.collection;

import org.aion.avm.core.*;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AionMapEnergyTest {

    private static IExternalState externalState;
    private static AvmImpl avm;
    private static AionAddress from = TestingState.PREMINED_ADDRESS;
    private static int iteration = 1;
    private static boolean printEnergyUsed = false;

    // indexes to print the result
    private static List<Integer> printList = new ArrayList<>(Arrays.asList(1, 10, 50, 150, 250, 500, 768, 1000));

    private static AionAddress aionMapContract;

    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        externalState = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        aionMapContract = deployContract(AionMapSimpleContract.class, AionMap.class, ABIDecoder.class, ABIEncoder.class, ABIException.class);
    }

    @After
    public void tearDown() {
        avm.shutdown();
    }

    @Test
    public void putIntoMapCollision() {
        String methodName = "put";
        long energyUsed;

        for (int i = 1; i <= iteration; i++) {
            energyUsed = callStatic(aionMapContract, methodName, false, 1 + i * iteration).energyUsed;
            if (printEnergyUsed && printList.contains(i)) {
                System.out.println(i + "th insert energy: " + energyUsed);
            }
        }
    }

    @Test
    public void putIntoMapSequential() {
        String methodName = "put";
        long energyUsed;
        for (int i = 1; i <= iteration; i++) {
            energyUsed = callStatic(aionMapContract, methodName, false, i).energyUsed;
            if (printEnergyUsed && printList.contains(i)) {
                System.out.println(i + "th insert energy: " + energyUsed);
            }
        }
    }

    @Test
    public void getValueFromMap() {
        String methodName = "put";
        long energyUsed;

        //setup
        for (int i = 1; i <= iteration; i++) {
            callStatic(aionMapContract, methodName, false, i);
        }

        methodName = "get";

        for (int key : printList) {
            if (key <= iteration) {
                energyUsed = callStatic(aionMapContract, methodName, true, key).energyUsed;
                if (printEnergyUsed) {
                    System.out.println("get key " + key + " energy: " + energyUsed);
                }
            }
        }
    }

    @Test
    public void getValueFromMapCollision() {
        String methodName = "put";
        long energyUsed;

        //setup
        for (int i = 1; i <= iteration; i++) {
            callStatic(aionMapContract, methodName, false, 1 + i * iteration);
        }

        methodName = "get";

        for (int key : printList) {
            if (key <= iteration) {
                energyUsed = callStatic(aionMapContract, methodName, true, 1 + key * iteration).energyUsed;
                if (printEnergyUsed) {
                    System.out.println("get key " + 1 + key * iteration + " energy: " + energyUsed);
                }
            }
        }
    }

    @Test
    public void removeValueFromMap() {
        String methodName = "put";
        long energyUsed;

        //setup
        for (int i = 1; i <= iteration; i++) {
            callStatic(aionMapContract, methodName, false, i);
        }

        methodName = "remove";

        for (int key : printList) {
            if (key <= iteration) {
                energyUsed = callStatic(aionMapContract, methodName, true, key).energyUsed;
                if (printEnergyUsed) {
                    System.out.println("remove key " + key + " energy: " + energyUsed);
                }
            }
        }
    }

    private AionAddress deployContract(Class<?> mainClass, Class<?>... otherClasses) {

        byte[] jar = JarBuilder.buildJarForMainAndClasses(mainClass, otherClasses);
        Transaction createTransaction = AvmTransactionUtil.create(from, externalState.getNonce(from), BigInteger.ZERO, new CodeAndArguments(jar, null).encodeToBytes(), 5_000_000, 1L);
        TransactionResult createResult = avm.run(externalState, new Transaction[]{createTransaction}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();

        Assert.assertTrue(createResult.transactionStatus.isSuccess());
        if (printEnergyUsed) {
            System.out.println("deployment cost for " + mainClass.getName() + ": " + createResult.energyUsed);
        }
        return new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());
    }

    private TransactionResult callStatic(AionAddress contract, String methodName, boolean validateReturnResult, int key) {
        byte[] data = new ABIStreamingEncoder().encodeOneString(methodName).encodeOneInteger(key).toBytes();
        Transaction callTransaction = AvmTransactionUtil.call(from, contract, externalState.getNonce(from), BigInteger.ZERO, data, 2_000_000, 1L);
        TransactionResult callResult = avm.run(externalState, new Transaction[]{callTransaction}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(callResult.transactionStatus.isSuccess());
        if (validateReturnResult) {
            Assert.assertEquals("STRING", new ABIDecoder(callResult.copyOfTransactionOutput().orElseThrow()).decodeOneString());
        }
        return callResult;
    }
}
