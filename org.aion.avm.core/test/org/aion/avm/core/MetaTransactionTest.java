package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.types.AionAddress;
import org.aion.types.InternalTransaction;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import avm.Address;


public class MetaTransactionTest {
    private static AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static TestingBlock BLOCK;
    private static TestingState KERNEL;
    private static EmptyCapabilities CAPABILITIES;
    private static AvmImpl AVM;

    @BeforeClass
    public static void setupClass() {
        BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        KERNEL = new TestingState(BLOCK);
        
        CAPABILITIES = new EmptyCapabilities();
        AvmConfiguration config = new AvmConfiguration();
        AVM = CommonAvmFactory.buildAvmInstanceForConfiguration(CAPABILITIES, config);
    }

    @AfterClass
    public static void tearDownClass() {
        AVM.shutdown();
    }

    @Test
    public void testInlineBalanceTransfer() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);

        // Create a transaction to make a basic balance transfer.
        AionAddress targetAddress = Helpers.randomAddress();
        BigInteger valueToSend = BigInteger.valueOf(1_000_000_000L);
        byte[] serializedTransaction = buildInnerMetaTransactionFromDeployer(targetAddress, valueToSend, 1L, contractAddress, new byte[0]);
        
        // Verify initial state.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getBalance(targetAddress));
        
        // Send the transaction as an inline meta-transaction.
        byte[] callData = encodeCallByteArray("callInline", serializedTransaction);
        Transaction tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // Balance transfer returns null.
        Assert.assertNull(new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray());
        
        // Verify final state.
        Assert.assertEquals(valueToSend, KERNEL.getBalance(targetAddress));
    }

    @Test
    public void testInlineContractCall() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);

        // Create a transaction to call back into the contract, itself, as just the identity invocation (returns what it is given).
        BigInteger valueToSend = BigInteger.valueOf(1_000_000_000L);
        byte[] inputData = new byte[] {1,2,3,4,5};
        byte[] invokeArguments = new ABIStreamingEncoder().encodeOneString("identity").encodeOneByteArray(inputData).toBytes();
        byte[] serializedTransaction = buildInnerMetaTransactionFromDeployer(contractAddress, valueToSend, 1L, contractAddress, invokeArguments);
        
        // Verify initial state.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getBalance(contractAddress));
        
        // Send the transaction as an inline meta-transaction.
        byte[] callData = encodeCallByteArray("callInline", serializedTransaction);
        Transaction tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // Identity returns the data it is given.
        Assert.assertArrayEquals(inputData, new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray());
        
        // Verify final state.
        Assert.assertEquals(valueToSend, KERNEL.getBalance(contractAddress));
    }

    @Test
    public void testStoreBalanceTransferDoubleSend() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);

        // Create a transaction to make a basic balance transfer.
        AionAddress targetAddress = Helpers.randomAddress();
        BigInteger valueToSend = BigInteger.valueOf(1_000_000_000L);
        byte[] serializedTransaction = buildInnerMetaTransactionFromDeployer(targetAddress, valueToSend, 2L, contractAddress, new byte[0]);
        
        // Verify initial state.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getBalance(targetAddress));
        
        // Store this on-chain.
        byte[] callData = encodeCallByteArray("store", serializedTransaction);
        Transaction tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        
        // Verify that it hasn't yet run.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getBalance(targetAddress));
        
        // Invoke the on-chain transaction.
        callData = encodeCall("call");
        tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        
        // Observe the balance changed.
        Assert.assertEquals(valueToSend, KERNEL.getBalance(targetAddress));
        
        // Invoke the on-chain transaction, again - this should cause a nonce failure which we aren't handline, so it is an exception.
        callData = encodeCall("call");
        tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isFailed());
        
        // Verify that the balance didn't change again.
        Assert.assertEquals(valueToSend, KERNEL.getBalance(targetAddress));
    }

    @Test
    public void testInlineContractDeploy() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);

        // Create a transaction to call back into the contract, itself, as just the identity invocation (returns what it is given).
        BigInteger valueToSend = BigInteger.valueOf(1_000_000_000L);
        // We are going to do the create inline, so we expect a lower energy limit (create has 5M and call has 2M).
        byte[] inlineCodeAndArgs = codeAndArgsForTargetDeployment(false, null, false);
        byte[] serializedTransaction = buildInnerMetaTransactionFromDeployer(null, valueToSend, 1L, contractAddress, inlineCodeAndArgs);
        
        // Verify initial state.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getBalance(contractAddress));
        
        // Send the transaction as an inline meta-transaction.
        byte[] callData = encodeCallByteArray("createInline", serializedTransaction);
        Transaction tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        
        // The result should be an encoded contract address so check that it is there and has the money.
        AionAddress newContract = new AionAddress(new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray());
        Assert.assertEquals(valueToSend, KERNEL.getBalance(newContract));
        Assert.assertArrayEquals(CodeAndArguments.decodeFromBytes(codeAndArgs).code, KERNEL.getCode(newContract));
        
        // Now, send a simple transaction to make sure we can run this and the returned address was meaningful.
        byte[] inputData = new byte[] {1,2,3,4,5};
        callData = encodeCallByteArray("identity", inputData);
        tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertArrayEquals(inputData, new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray());
    }

    @Test
    public void testInlineContractCallDepth() {
        BigInteger initialNonce = KERNEL.getNonce(DEPLOYER);
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);
        
        // We will try sending a simple transaction, nested 9 times, since that should be the limit.
        // We will eventually call "identity", nested in 8 levels of "callInline".
        // These will be called backward so we need to attach nonces in reverse order.
        long intermediateLevels = 8L;
        long firstInnerNonceBias = 1L;
        BigInteger valueToSend = BigInteger.valueOf(1_000_000_000L);
        byte[] inputData = new byte[] {1,2,3,4,5};
        byte[] invokeArguments = new ABIStreamingEncoder().encodeOneString("identity").encodeOneByteArray(inputData).toBytes();
        byte[] terminalTransaction = buildInnerMetaTransactionFromDeployer(contractAddress, valueToSend, intermediateLevels + 1, contractAddress, invokeArguments);
        byte[] serializedTransaction = recursiveEncode(contractAddress, valueToSend, firstInnerNonceBias, intermediateLevels, 0L, contractAddress, terminalTransaction);
        
        // Verify initial state.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getBalance(contractAddress));
        
        // Send the transaction as an inline meta-transaction.
        byte[] callData = encodeCallByteArray("callInline", serializedTransaction);
        Transaction tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // Identity returns the data it is given.
        Assert.assertArrayEquals(inputData, new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray());
        
        // Verify final state.
        BigInteger baselineBalance = valueToSend.multiply(BigInteger.valueOf(9L));
        Assert.assertEquals(baselineBalance, KERNEL.getBalance(contractAddress));
        // Also verify the nonce reflects these 11 calls:  1 deployment, 1 external, and 9 internal meta.
        Assert.assertEquals(initialNonce.add(BigInteger.valueOf(11L)), KERNEL.getNonce(DEPLOYER));
        Assert.assertEquals(9, result.internalTransactions.size());
        
        // Now, send one which should fail (10 iterations - 9 intermediate and 1 final).
        // NOTE:  only the final transaction will fail - the other 8 internal transactions will pass
        intermediateLevels = 9L;
        invokeArguments = new ABIStreamingEncoder().encodeOneString("identity").encodeOneByteArray(inputData).toBytes();
        terminalTransaction = buildInnerMetaTransactionFromDeployer(contractAddress, valueToSend, intermediateLevels + 1, contractAddress, invokeArguments);
        serializedTransaction = recursiveEncode(contractAddress, valueToSend, firstInnerNonceBias, intermediateLevels, 0L, contractAddress, terminalTransaction);
        
        // Send the transaction as an inline meta-transaction.
        callData = encodeCallByteArray("callInline", serializedTransaction);
        tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        
        // Verify final state is only updated to account for the 8 successful frames.
        // NOTE:  This failure behaviour requires some explanation:  an invocation stack depth overflow causes fatal error within the caller.
        // This means that 9 calls are ok but attempting 10 will only see 8 of them complete.
        Assert.assertEquals(valueToSend.multiply(BigInteger.valueOf(8L)).add(baselineBalance), KERNEL.getBalance(contractAddress));
        // Also verify the final nonce only reflects the successfully executed calls:
        // 1 deployment
        // 1 + 9 for original successful sequences.
        // 1 + 9 successes in this sequence (excluding the failure).
        Assert.assertEquals(initialNonce.add(BigInteger.valueOf(21L)), KERNEL.getNonce(DEPLOYER));
        Assert.assertEquals(9, result.internalTransactions.size());
    }

    @Test
    public void testOrigin() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);
        
        // Create the new user account which will sign but not pay for the transaction.
        AionAddress freeloaderAddress = Helpers.randomAddress();
        
        // Create the inner transaction which will verify the observed origin.
        BigInteger valueToSend = BigInteger.ZERO;
        byte[] invokeArguments = new ABIStreamingEncoder().encodeOneString("checkOrigin").toBytes();
        byte[] serializedTransaction = buildInnerMetaTransaction(freeloaderAddress, contractAddress, valueToSend, BigInteger.ZERO, contractAddress, invokeArguments);
        
        // Verify initial state.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getBalance(contractAddress));
        
        // Send the transaction as an inline meta-transaction.
        byte[] callData = encodeCallByteArray("callInline", serializedTransaction);
        Transaction tx = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // Identity returns the data it is given.
        Assert.assertArrayEquals(freeloaderAddress.toByteArray(), new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray());
        // Verify that we see this meta-transaction in the results.
        int normal = 0;
        int meta = 0;
        for (InternalTransaction internal : result.internalTransactions) {
            if (null != internal.copyOfInvokableHash()) {
                meta += 1;
            } else {
                normal += 1;
            }
        }
        Assert.assertEquals(0, normal);
        Assert.assertEquals(1, meta);
        
        // Verify final state.
        Assert.assertEquals(valueToSend, KERNEL.getBalance(contractAddress));
    }

    @Test
    public void testInlineTransferOnDeploy() {
        // Get the nonce for the deployment.
        BigInteger deploymentNonce = KERNEL.getNonce(DEPLOYER);
        // ... and the nonce for the transaction within it.
        BigInteger innerTransactionNonce = deploymentNonce.add(BigInteger.ONE);
        
        // Create the target address we will fund.
        AionAddress targetAddress = Helpers.randomAddress();
        // We also need to know the executor address which will be the address where the contract is deployed
        // (we just pass a fake transaction with the correct sender and nonce into the helper).
        AionAddress executorAddress = CAPABILITIES.generateContractAddress(DEPLOYER, deploymentNonce);
        // Create the value we wish to send which we can use to verify the balance, later.
        BigInteger valueToSend = BigInteger.valueOf(1_000_000_000L);
        
        // Prepare the inner transaction.
        byte[] innerTransaction = buildInnerMetaTransaction(DEPLOYER, targetAddress, valueToSend, innerTransactionNonce, executorAddress, new byte[0]);
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, innerTransaction, false);
        
        // Deploy the contract and verify that the address was as expected.
        AionAddress contractAddress = createDApp(codeAndArgs);
        Assert.assertEquals(executorAddress, contractAddress);
        
        // Verify that the balance transfer happened and that the deployer's nonce incremented.
        Assert.assertEquals(valueToSend, KERNEL.getBalance(targetAddress));
        Assert.assertEquals(innerTransactionNonce.add(BigInteger.ONE), KERNEL.getNonce(DEPLOYER));
    }

    @Test
    public void testInternalTransactionEnergyCost_Call() {
        // Deploy initial contract.
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(InternalTransactionEnergyTarget.class);
        byte[] codeAndArgs = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress contractAddress = createDApp(codeAndArgs);
        byte[] doNothingData = new ABIStreamingEncoder().encodeOneString("doNothing").toBytes();
        
        // Get a baseline, for later comparison.
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, doNothingData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // Based on graph activity and executed code, we expect the doNothing() call to cost this much
        // - this should be EXACTLY the number billed by BlockchainRuntimeImpl when returning from the internal call.
        long expectedDoNothingCost = 26_012L;
        // Add the basic transaction cost to this.
        Assert.assertEquals(expectedDoNothingCost + BillingRules.getBasicTransactionCost(doNothingData), result.energyUsed);
        
        // Tell it to call itself.
        byte[] callData = new ABIStreamingEncoder().encodeOneString("costOfCall").encodeOneAddress(new Address(contractAddress.toByteArray())).encodeOneByteArray(doNothingData).toBytes();
        transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // We expect this to be the baseline cost we measured, plus API cost (call() and getRemainingEnergy() - see RuntimeMethodFeeSchedule) and allocate Result.
        Assert.assertEquals(expectedDoNothingCost + 5000L + 100L + 100L, new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneLong());
    }

    @Test
    public void testInternalTransactionEnergyCost_Create() {
        // Deploy initial contract.
        // (we assemble this manually to keep it small since we don't have the optimizer in this project).
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(InternalTransactionEnergyTarget.class, ABIEncoder.class, ABIDecoder.class, ABIException.class);
        byte[] codeAndArgs = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress contractAddress = createDApp(codeAndArgs);
        
        // Based on graph activity and executed code, we expect the creation to cost this much
        // - this should be EXACTLY the number billed by BlockchainRuntimeImpl when returning from the internal call.
        long expectedLocalDeploymentCost = 230_029L;
        
        // Tell it to deploy itself.
        byte[] callData = new ABIStreamingEncoder().encodeOneString("costOfCreate").encodeOneByteArray(codeAndArgs).toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // We expect this to be the baseline cost we measured, plus API cost (create() and getRemainingEnergy() - see RuntimeMethodFeeSchedule) and allocate Result.
        Assert.assertEquals(expectedLocalDeploymentCost + 5000L + 100L + 100L, new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneLong());
    }

    @Test
    public void testInternalTransactionEnergyCost_Invoke() {
        // Deploy initial contract.
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(InternalTransactionEnergyTarget.class);
        byte[] codeAndArgs = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress contractAddress = createDApp(codeAndArgs);
        byte[] doNothingData = new ABIStreamingEncoder().encodeOneString("doNothing").toBytes();
        
        // Get a baseline, for later comparison.
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, doNothingData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // Based on graph activity and executed code, we expect the doNothing() call to cost this much
        // - this should be EXACTLY the number billed by BlockchainRuntimeImpl when returning from the internal call.
        long expectedDoNothingCost = 26_012L;
        // Add the basic transaction cost to this.
        Assert.assertEquals(expectedDoNothingCost + BillingRules.getBasicTransactionCost(doNothingData), result.energyUsed);
        
        // Tell it to call itself via an invokable.
        byte[] innerInvoke = buildInnerMetaTransaction(DEPLOYER, contractAddress, BigInteger.ZERO, KERNEL.getNonce(DEPLOYER).add(BigInteger.ONE), contractAddress, doNothingData);
        byte[] callData = new ABIStreamingEncoder().encodeOneString("costOfInvoke").encodeOneByteArray(innerInvoke).toBytes();
        transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // We expect this to be the baseline cost we measured, plus API cost (call() and getRemainingEnergy() - see RuntimeMethodFeeSchedule) and allocate Result.
        Assert.assertEquals(expectedDoNothingCost + 5000L + 100L + 100L, new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneLong());
    }

    @Test
    public void testInvokableEnergyLimitDuringCreate() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);
        
        // Verify the energy limit on a normal call.
        byte[] call_checkEnergyLimit = new ABIStreamingEncoder().encodeOneString("checkEnergyLimit").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, call_checkEnergyLimit, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(2_000_000L, new ABIDecoder(new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray()).decodeOneLong());
        
        // Verify the energy limit on a call-invoke-call.
        byte[] innerInvoke = buildInnerMetaTransaction(DEPLOYER, contractAddress, BigInteger.ZERO, KERNEL.getNonce(DEPLOYER).add(BigInteger.ONE), contractAddress, call_checkEnergyLimit);
        byte[] callData = encodeCallByteArray("callInline", innerInvoke);
        transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // (note that inner-most calls have a diminished limit so we expect this to be something less than 2 million)
        Assert.assertTrue(new ABIDecoder(new ABIDecoder(result.copyOfTransactionOutput().get()).decodeOneByteArray()).decodeOneLong() < 2_000_000L);
        
        // Verify the energy limit on a create-API-create.
        AionAddress testExecutorAddress = CAPABILITIES.generateContractAddress(DEPLOYER, KERNEL.getNonce(DEPLOYER));
        byte[] inlineCreateCodeAndArgs = codeAndArgsForTargetDeployment(true, codeAndArgs, true);
        createDApp(inlineCreateCodeAndArgs);
        
        // Verify the energy limit on a create-invoke-create.
        testExecutorAddress = CAPABILITIES.generateContractAddress(DEPLOYER, KERNEL.getNonce(DEPLOYER));
        innerInvoke = buildInnerMetaTransaction(DEPLOYER, null, BigInteger.ZERO, KERNEL.getNonce(DEPLOYER).add(BigInteger.ONE), testExecutorAddress, codeAndArgs);
        inlineCreateCodeAndArgs = codeAndArgsForTargetDeployment(true, innerInvoke, false);
        createDApp(inlineCreateCodeAndArgs);
    }

    @Test
    public void testInvokableNonceOnDeepFailure() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);
        
        // Create the invokable signers.
        AionAddress signerAddress1 = Helpers.randomAddress();
        AionAddress signerAddress2 = Helpers.randomAddress();
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getNonce(signerAddress1));
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getNonce(signerAddress2));
        
        // Create the leaf invokable (this will be re-run until it passes).
        byte[] call_checkEnergyLimit = new ABIStreamingEncoder().encodeOneString("checkEnergyLimit").toBytes();
        byte[] leafInvoke = buildInnerMetaTransaction(signerAddress2, contractAddress, BigInteger.ZERO, KERNEL.getNonce(signerAddress2), contractAddress, call_checkEnergyLimit);
        
        // Create the intermediate one (the first will fail so we will need another).
        byte[] middleInvoke = buildInnerMetaTransaction(signerAddress1, contractAddress, BigInteger.ZERO, KERNEL.getNonce(signerAddress1), contractAddress, encodeCallByteArray("invokeAndFail", leafInvoke));
        
        // Now, send this such that it fails.
        byte[] callData = encodeCallByteArray("callInline", middleInvoke);
        BigInteger deployerNonce = KERNEL.getNonce(DEPLOYER);
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        
        // Verify the change in the nonces is as expected.
        Assert.assertEquals(BigInteger.ONE, KERNEL.getNonce(signerAddress1));
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getNonce(signerAddress2));
        Assert.assertEquals(deployerNonce.add(BigInteger.ONE), KERNEL.getNonce(DEPLOYER));
        
        // Now, send the leaf invoke again, such that it succeeds.
        middleInvoke = buildInnerMetaTransaction(signerAddress1, contractAddress, BigInteger.ZERO, KERNEL.getNonce(signerAddress1), contractAddress, encodeCallByteArray("callInline", leafInvoke));
        callData = encodeCallByteArray("callInline", middleInvoke);
        deployerNonce = KERNEL.getNonce(DEPLOYER);
        transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        
        // Verify the change in the nonces is as expected.
        Assert.assertEquals(BigInteger.TWO, KERNEL.getNonce(signerAddress1));
        Assert.assertEquals(BigInteger.ONE, KERNEL.getNonce(signerAddress2));
        Assert.assertEquals(deployerNonce.add(BigInteger.ONE), KERNEL.getNonce(DEPLOYER));
    }

    @Test
    public void testInvokableNonceOnFailure() {
        // Deploy initial contract.
        byte[] codeAndArgs = codeAndArgsForTargetDeployment(true, null, false);
        AionAddress contractAddress = createDApp(codeAndArgs);
        
        // Create the invokable signer.
        AionAddress signerAddress = Helpers.randomAddress();
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getNonce(signerAddress));
        
        // Send an invokable which will fail externally.
        byte[] call_checkEnergyLimit = new ABIStreamingEncoder().encodeOneString("checkEnergyLimit").toBytes();
        byte[] innerInvoke = buildInnerMetaTransaction(signerAddress, contractAddress, BigInteger.ZERO, KERNEL.getNonce(signerAddress), contractAddress, call_checkEnergyLimit);
        byte[] callData = encodeCallByteArray("invokeAndFail", innerInvoke);
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isReverted());
        
        // Verify that the nonce didn't change.
        Assert.assertEquals(BigInteger.ZERO, KERNEL.getNonce(signerAddress));
        
        // Send the same transaction such that it will succeed.
        callData = encodeCallByteArray("callInline", innerInvoke);
        transaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, callData, 2_000_000l, 1L);
        result = AVM.run(KERNEL, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(BigInteger.ONE, KERNEL.getNonce(signerAddress));
    }


    private byte[] recursiveEncode(AionAddress targetAddress, BigInteger valueToSend, long baseNonce, long iterations, long currentIteration, AionAddress executor, byte[] data) {
        byte[] result = null;
        // This works like a for look so terminate when they match.
        if (iterations == currentIteration) {
            result = data;
        } else {
            byte[] downStream = recursiveEncode(targetAddress, valueToSend, baseNonce, iterations, currentIteration + 1, executor, data);
            long nonceBias = baseNonce + currentIteration;
            valueToSend = BigInteger.valueOf(1_000_000_000L);
            byte[] invokeArguments = new ABIStreamingEncoder().encodeOneString("callInline").encodeOneByteArray(downStream).toBytes();
            result = buildInnerMetaTransactionFromDeployer(targetAddress, valueToSend, nonceBias, targetAddress, invokeArguments);
        }
        return result;
    }

    // NOTE:  If targetAddress is null, this is a create.
    private byte[] buildInnerMetaTransaction(AionAddress senderAddress, AionAddress targetAddress, BigInteger valueToSend, BigInteger nonce, AionAddress executor, byte[] data) {
        TestingMetaEncoder.MetaTransaction transaction = new TestingMetaEncoder.MetaTransaction();
        transaction.senderAddress = senderAddress;
        transaction.targetAddress = targetAddress;
        transaction.value = valueToSend;
        transaction.nonce = nonce;
        transaction.executor = executor;
        transaction.data = data;
        transaction.signature = new byte[] { 0x1 };
        return TestingMetaEncoder.encode(transaction);
    }

    // NOTE:  If targetAddress is null, this is a create.
    private byte[] buildInnerMetaTransactionFromDeployer(AionAddress targetAddress, BigInteger valueToSend, long nonceBias, AionAddress executor, byte[] data) {
        TestingMetaEncoder.MetaTransaction transaction = new TestingMetaEncoder.MetaTransaction();
        transaction.senderAddress = DEPLOYER;
        transaction.targetAddress = targetAddress;
        transaction.value = valueToSend;
        transaction.nonce = KERNEL.getNonce(DEPLOYER).add(BigInteger.valueOf(nonceBias));
        transaction.executor = executor;
        transaction.data = data;
        transaction.signature = new byte[] { 0x1 };
        return TestingMetaEncoder.encode(transaction);
    }

    private AionAddress createDApp(byte[] createData) {
        TransactionResult result1 = createDAppCanFail(createData);
        Assert.assertTrue(result1.transactionStatus.isSuccess());
        return new AionAddress(result1.copyOfTransactionOutput().orElseThrow());
    }

    private TransactionResult createDAppCanFail(byte[] createData) {
        long energyLimit = 5_000_000l;
        long energyPrice = 1l;
        Transaction tx1 = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, createData, energyLimit, energyPrice);
        return AVM.run(KERNEL, new Transaction[] {tx1}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber() - 1)[0].getResult();
    }

    private static byte[] encodeCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }

    private static byte[] encodeCallByteArray(String methodName, byte[] arg) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneByteArray(arg)
                .toBytes();
    }

    private static byte[] codeAndArgsForTargetDeployment(boolean expectHighEnergyLimit, byte[] invokable, boolean interpretAsApiCreate) {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(MetaTransactionTarget.class, ABIEncoder.class, ABIDecoder.class, ABIException.class);
        byte[] args = new ABIStreamingEncoder()
                .encodeOneBoolean(expectHighEnergyLimit)
                .encodeOneByteArray(invokable)
                .encodeOneBoolean(interpretAsApiCreate)
                .toBytes();
        return new CodeAndArguments(jar, args).encodeToBytes();
    }
}
