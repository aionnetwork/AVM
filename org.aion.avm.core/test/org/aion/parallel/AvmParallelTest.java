package org.aion.parallel;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import avm.Address;
import org.aion.avm.core.BillingRules;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.core.FutureResult;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class AvmParallelTest {

    private AionAddress preminedAddress = TestingState.PREMINED_ADDRESS;

    private TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    @Test
    public void basicConcurrencyTest(){
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AionAddress usr1 = new AionAddress(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        BigInteger expected1 = BigInteger.ZERO;

        Transaction t0 = AvmTransactionUtil.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t1 = AvmTransactionUtil.call(preminedAddress, usr1, BigInteger.ONE, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t2 = AvmTransactionUtil.call(preminedAddress, usr1, BigInteger.TWO, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t3 = AvmTransactionUtil.call(preminedAddress, usr1, BigInteger.valueOf(3), BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t4 = AvmTransactionUtil.call(preminedAddress, usr1, BigInteger.valueOf(4), BigInteger.valueOf(500_000), new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        FutureResult[] results = avm.run(kernel, batch);
        for (FutureResult f : results){
            f.getResult();
        }

        expected1 = expected1.add(BigInteger.valueOf(500_000 * 5));

        Assert.assertEquals(expected1, kernel.getBalance(usr1));

        AionAddress usr2 = new AionAddress(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        BigInteger expected2 = BigInteger.ZERO;

        t0 = AvmTransactionUtil.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = AvmTransactionUtil.call(usr1, usr2, BigInteger.ONE, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t2 = AvmTransactionUtil.call(usr1, usr2, BigInteger.TWO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t3 = AvmTransactionUtil.call(usr1, usr2, BigInteger.valueOf(3), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t4 = AvmTransactionUtil.call(usr1, usr2, BigInteger.valueOf(4), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
        results = avm.run(kernel, batch);
        for (FutureResult f : results){
            f.getResult();
        }

        expected1 = expected1.subtract(BigInteger.valueOf(100_000 * 5)).subtract(BigInteger.valueOf(BillingRules.getBasicTransactionCost(t1.copyOfTransactionData()) * 5));
        expected2 = BigInteger.valueOf(100_000 * 5);

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        t0 = AvmTransactionUtil.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = AvmTransactionUtil.call(usr2, usr1, BigInteger.ONE, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        int iteration = 10;
        batch = new Transaction[iteration * 2];
        for (int i = 0; i < iteration; i++){
            batch[i * 2]     = AvmTransactionUtil.call(usr1, usr2, BigInteger.valueOf(5 + i), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
            batch[i * 2 + 1] = AvmTransactionUtil.call(usr2, usr1, BigInteger.valueOf(i), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        }

        results = avm.run(kernel, batch);
        for (FutureResult f : results){
            f.getResult();
        }

        expected1 = expected1.subtract(BigInteger.valueOf(BillingRules.getBasicTransactionCost(t0.copyOfTransactionData()) * iteration));
        expected2 = expected2.subtract(BigInteger.valueOf(BillingRules.getBasicTransactionCost(t1.copyOfTransactionData()) * iteration));

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        avm.shutdown();
    }

    @Test
    public void cyclicWaitTest(){

        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AionAddress usr1 = new AionAddress(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        AionAddress usr2 = new AionAddress(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        AionAddress usr3 = new AionAddress(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        AionAddress usr4 = new AionAddress(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        Transaction t0 = AvmTransactionUtil.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t1 = AvmTransactionUtil.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(4_000_000), new byte[0], 100000L, 1);
        Transaction t2 = AvmTransactionUtil.call(usr2, usr3, BigInteger.ZERO, BigInteger.valueOf(3_000_000), new byte[0], 100000L, 1);
        Transaction t3 = AvmTransactionUtil.call(usr3, usr4, BigInteger.ZERO, BigInteger.valueOf(2_000_000), new byte[0], 100000L, 1);
        Transaction t4 = AvmTransactionUtil.call(usr4, usr1, BigInteger.ZERO, BigInteger.valueOf(1_000_000), new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        FutureResult[] results = avm.run(kernel, batch);
        for (FutureResult f : results){
            f.getResult();
        }

        avm.shutdown();
    }

    @Test
    public void internalTransactionTest(){

        byte[] code = JarBuilder.buildJarForMainAndClassesAndUserlib(TestContract.class);

        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AionAddress usr1 = new AionAddress(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        AionAddress usr2 = new AionAddress(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        AionAddress usr3 = new AionAddress(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        AionAddress usr4 = new AionAddress(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        Transaction t0 = AvmTransactionUtil.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t1 = AvmTransactionUtil.call(preminedAddress, usr2, BigInteger.ONE, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t2 = AvmTransactionUtil.call(preminedAddress, usr3, BigInteger.TWO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t3 = AvmTransactionUtil.call(preminedAddress, usr4, BigInteger.valueOf(3), BigInteger.valueOf(15_000_000), new byte[0], 100000L, 1);
        Transaction t4 = AvmTransactionUtil.create(usr4, BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 10_000_000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        FutureResult[] results = avm.run(kernel, batch);
        for (FutureResult f : results){
            f.getResult();
        }

        TransactionResult res = results[4].getResult();
        AionAddress contractAddr = new AionAddress(res.copyOfTransactionOutput().orElseThrow());

        byte[] args = encodeNoArgsMethodCall("doTransfer");
        byte[] args2 = encodeNoArgsMethodCall("addValue");

        t0 = AvmTransactionUtil.call(preminedAddress, contractAddr, BigInteger.valueOf(4), BigInteger.valueOf(5_000_000), args2, 100000L, 1);
        t1 = AvmTransactionUtil.call(usr1, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t2 = AvmTransactionUtil.call(usr2, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t3 = AvmTransactionUtil.call(usr3, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t4 = AvmTransactionUtil.call(usr4, contractAddr, BigInteger.ONE, BigInteger.ZERO, args, 200000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
        results = avm.run(kernel, batch);
        for (FutureResult f : results){
            f.getResult();
        }

        avm.shutdown();
    }

    /**
     * This test attempts to transfer to 100 users and then from those to 1 user.  This will cause heavy abort behaviour because this is a data hazard.
     */
    @Test
    public void heavyAbortTest(){
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // We will send 2x the value to these accounts, initially, and they will send 1x to the target.
        int iterations = 100;
        long valueToSend = 1_000_000L;
        AionAddress targetUser = Helpers.randomAddress();
        AionAddress[] tempUsers = new AionAddress[iterations];
        
        // First batch - disperse funds.
        Transaction[] firstBatch = new Transaction[iterations];
        for (int i = 0; i < iterations; ++i) {
            tempUsers[i] = Helpers.randomAddress();
            firstBatch[i] = AvmTransactionUtil.call(preminedAddress, tempUsers[i], BigInteger.valueOf(i), BigInteger.valueOf(2L * valueToSend), new byte[0], 100_000L, 1L);
        }
            FutureResult[] results = avm.run(kernel, firstBatch);
        for (FutureResult f : results){
            Assert.assertTrue(f.getResult().transactionStatus.isSuccess());
        }
        
        // Second batch - collect funds.
        Transaction[] secondBatch = new Transaction[iterations];
        for (int i = 0; i < iterations; ++i) {
            secondBatch[i] = AvmTransactionUtil.call(tempUsers[i], targetUser, BigInteger.ZERO, BigInteger.valueOf(valueToSend), new byte[0], 100_000L, 1L);
        }
        results = avm.run(kernel, secondBatch);
        for (FutureResult f : results){
            Assert.assertTrue(f.getResult().transactionStatus.isSuccess());
        }
        
        // Check that this had the expected result.
        Assert.assertEquals(BigInteger.valueOf((long)iterations * valueToSend), kernel.getBalance(targetUser));
        avm.shutdown();
    }

    @Test
    public void reentrantAbort() {
        byte[] code = JarBuilder.buildJarForMainAndClassesAndUserlib(TestContract.class);

        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        int length = 5;
        AionAddress[] user = new AionAddress[length];
        Transaction[] ctx = new Transaction[length];
        for (int i = 0; i < user.length; i++) {
            user[i] = Helpers.randomAddress();
            kernel.adjustBalance(user[i], BigInteger.TEN.pow(20));
            ctx[i] = AvmTransactionUtil.create(user[i], BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 5_000_000L, 1);
        }

        FutureResult[] results = avm.run(kernel, ctx);
        AionAddress[] contractAddresses = new AionAddress[results.length];
        for (int i = 0; i < results.length; i++) {
            contractAddresses[i] = new AionAddress(results[i].getResult().copyOfTransactionOutput().orElseThrow());
        }

        Transaction[] tx = new Transaction[results.length - 1];
       // A->A->B, B->B->C, C->C->D
        for (int i = 0; i < results.length - 1; i++) {
            byte[] internalCallData = new ABIStreamingEncoder()
                    .encodeOneString("doCallOther")
                    .encodeOneAddress(new Address(contractAddresses[i + 1].toByteArray()))
                    .encodeOneByteArray(new ABIStreamingEncoder().encodeOneString("addValue").toBytes())
                    .toBytes();

            byte[] data = new ABIStreamingEncoder().encodeOneString("doCallThis").encodeOneByteArray(internalCallData).toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, data, 5_000_000, 1);
        }

        results = avm.run(kernel, tx);
        for (FutureResult f : results) {
            f.getResult();
        }

        // validate the state of contracts
        for (int i = 0; i < length - 1; i++) {
            byte[] getCallCount = new ABIStreamingEncoder().encodeOneString("getCallCount").toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, getCallCount, 5_000_000, 1);
        }

        results = avm.run(kernel, tx);
        for (FutureResult f : results) {
            Assert.assertEquals(2, new ABIDecoder(f.getResult().copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }

        for (int i = 1; i < length - 1; i++) {
            byte[] getValue = new ABIStreamingEncoder().encodeOneString("getValue").toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, getValue, 5_000_000, 1);
        }

        results = avm.run(kernel, new Transaction[]{tx[1], tx[2], tx[3]});
        for (FutureResult f : results) {
            Assert.assertEquals(1, new ABIDecoder(f.getResult().copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }

        avm.shutdown();
    }

    @Test
    public void internalTransactionAbort() {

        byte[] code = JarBuilder.buildJarForMainAndClassesAndUserlib(TestContract.class);

        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        int length = 4;
        AionAddress[] user = new AionAddress[length];
        Transaction[] ctx = new Transaction[length];
        for (int i = 0; i < length; i++) {
            user[i] = Helpers.randomAddress();
            kernel.adjustBalance(user[i], BigInteger.TEN.pow(20));
            ctx[i] = AvmTransactionUtil.create(user[i], BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 5_000_000L, 1);
        }

        FutureResult[] results = avm.run(kernel, ctx);
        AionAddress[] contractAddresses = new AionAddress[results.length];
        for (int i = 0; i < results.length; i++) {
            contractAddresses[i] = new AionAddress(results[i].getResult().copyOfTransactionOutput().orElseThrow());
        }

        Transaction[] tx = new Transaction[results.length - 1];
        // u1->A->B, u2->B->C, u3->C->D
        for (int i = 0; i < results.length - 1; i++) {
            byte[] callData = new ABIStreamingEncoder()
                    .encodeOneString("doCallOther")
                    .encodeOneAddress(new Address(contractAddresses[i + 1].toByteArray()))
                    .encodeOneByteArray(new ABIStreamingEncoder().encodeOneString("addValue").toBytes())
                    .toBytes();

            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, callData, 5_000_000, 1);
        }

        results = avm.run(kernel, tx);
        for (FutureResult f : results) {
            f.getResult();
        }

        // validate the state of contracts
        for (int i = 0; i < length - 1; i++) {
            byte[] getCallCount = new ABIStreamingEncoder().encodeOneString("getCallCount").toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, getCallCount, 5_000_000, 1);
        }

        results = avm.run(kernel, tx);
        for (FutureResult f : results) {
            Assert.assertEquals(1, new ABIDecoder(f.getResult().copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }

        for (int i = 1; i < length - 1; i++) {
            byte[] getValue = new ABIStreamingEncoder().encodeOneString("getValue").toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, getValue, 5_000_000, 1);
        }
        Transaction[] batch = new Transaction[]{tx[1], tx[2]};
        results = avm.run(kernel, batch);
        for (FutureResult f : results) {
            Assert.assertEquals(1, new ABIDecoder(f.getResult().copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }

        avm.shutdown();
    }

    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}
