package org.aion.avm.embed.poc;

import org.aion.avm.core.*;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import avm.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.StandardCapabilities;
import org.aion.avm.embed.testExchange.CoinController;
import org.aion.avm.embed.testExchange.ERC20Token;
import org.aion.avm.tooling.ABIUtil;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;


public class DemoTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;

    private long energyLimit = 5_000_0000;
    private long energyPrice = 1;

    private AionAddress pepeMinter = Helpers.randomAddress();
    private AionAddress deployer = Helpers.randomAddress();
    private AionAddress owner1 = Helpers.randomAddress();
    private AionAddress owner2 = Helpers.randomAddress();
    private AionAddress receiver = Helpers.randomAddress();

    @Test
    public void testWallet() {
        IExternalState kernel = new TestingState();
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
        kernel.adjustBalance(pepeMinter, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(deployer, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(owner1, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(owner2, BigInteger.valueOf(1_000_000_000L));

        //================
        // DEPLOY
        //================

        report(">> Deploy \"PEPE\" ERC20 token Dapp...");
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(CoinController.class, ERC20Token.class);
        byte[] arguments = ABIUtil.encodeDeploymentArguments("Pepe", "PEPE", 8);
        //CoinContract pepe = new CoinContract(null, pepeMinter, testERC20Jar, arguments);
        Transaction createTransaction = AvmTransactionUtil.create(pepeMinter, kernel.getNonce(pepeMinter), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {createTransaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        Address tokenDapp = new Address(txResult.copyOfTransactionOutput().orElseThrow());
        report(">> \"PEPE\" ERC20 token Dapp is deployed. (Address " + Helpers.bytesToHexString(txResult.copyOfTransactionOutput().orElseThrow()) + ")");

        report("\n>> Deploy the Multi-sig Wallet Dapp...");
        jar = JarBuilder.buildJarForMainAndClassesAndUserlib(Main.class, Wallet.class, Bytes32.class);
        int confirmationsRequired = 2;
        arguments = ABIUtil.encodeDeploymentArguments(new Address(owner1.toByteArray()), new Address(owner2.toByteArray()), confirmationsRequired);
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        Address walletDapp = new Address(txResult.copyOfTransactionOutput().orElseThrow());
        report(">> Wallet Dapp is deployed. (Address " + Helpers.bytesToHexString(txResult.copyOfTransactionOutput().orElseThrow()) + ")");
        report(">> Owners List:");
        report(">>   Deployer - (Address " + deployer + ")");
        report(">>   Owner 1  - (Address " + owner1 + ")");
        report(">>   Owner 2  - (Address " + owner2 + ")");
        report(">> Minimum number of owners to approve a transaction: " + confirmationsRequired);

        //================
        // FUNDING and CHECK BALANCE
        //================
        arguments = ABIUtil.encodeMethodArguments("mint", walletDapp, 5000L);
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        report("\n>> PEPE Mint to deliver 5000 tokens to the wallet: " + new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", walletDapp);
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        report(">> balance of wallet: " + new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneLong());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", new Address(receiver.toByteArray()));
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        report(">> balance of receiver: " + new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneLong());

        //================
        // PROPOSE
        //================
        byte[] data = ABIUtil.encodeMethodArguments("transfer", new Address(receiver.toByteArray()), 3000L);
        arguments = ABIUtil.encodeMethodArguments("propose", tokenDapp, 0L, data, energyLimit);
        tx = AvmTransactionUtil.call(deployer, new AionAddress(walletDapp.toByteArray()), kernel.getNonce(deployer), BigInteger.ZERO, arguments, 2_000_000L, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        byte[] pendingTx = new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneByteArray();
        report("\n>> Deployer to propose a transaction of 3000 PEPE tokens to Receiver. (Tx ID " + Helpers.bytesToHexString(pendingTx) + ")");

        //================
        // CONFIRM #1
        //================
        arguments = ABIUtil.encodeMethodArguments("confirm", pendingTx);
        tx = AvmTransactionUtil.call(owner1, new AionAddress(walletDapp.toByteArray()), kernel.getNonce(owner1), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        report(">> Transaction confirmed by Owner 1: " + new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());

        //================
        // CONFIRM #2
        //================
        arguments = ABIUtil.encodeMethodArguments("confirm", pendingTx);
        tx = AvmTransactionUtil.call(owner2, new AionAddress(walletDapp.toByteArray()), kernel.getNonce(owner2), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        report(">> Transaction confirmed by Owner 2: " + new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());

        report("\n>> Number of confirmations reach to " + confirmationsRequired + ". Transaction is processed.");

        //================
        // CHECK BALANCE
        //================
        arguments = ABIUtil.encodeMethodArguments("balanceOf", walletDapp);
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        report("\n>> balance of wallet: " + new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneLong());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", new Address(receiver.toByteArray()));
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        report(">> balance of receiver: " + new ABIDecoder(txResult.copyOfTransactionOutput().orElseThrow()).decodeOneLong());
        avm.shutdown();
    }

    private static void report(String report) {
        if (REPORT) {
            System.out.println(report);
        }
    }
}
