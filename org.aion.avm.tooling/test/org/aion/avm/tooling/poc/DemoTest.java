package org.aion.avm.tooling.poc;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.StandardCapabilities;
import org.aion.avm.tooling.testExchange.CoinController;
import org.aion.avm.tooling.testExchange.ERC20Token;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;


public class DemoTest {

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 5_000_0000;
    private long energyPrice = 1;

    private org.aion.types.Address pepeMinter = Helpers.randomAddress();
    private org.aion.types.Address deployer = Helpers.randomAddress();
    private org.aion.types.Address owner1 = Helpers.randomAddress();
    private org.aion.types.Address owner2 = Helpers.randomAddress();
    private org.aion.types.Address receiver = Helpers.randomAddress();

    @Test
    public void testWallet() {
        KernelInterface kernel = new TestingKernel();
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
        kernel.adjustBalance(pepeMinter, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(deployer, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(owner1, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(owner2, BigInteger.valueOf(1_000_000_000L));

        //================
        // DEPLOY
        //================

        System.out.println(">> Deploy \"PEPE\" ERC20 token Dapp...");
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(CoinController.class, ERC20Token.class);
        byte[] arguments = ABIEncoder.encodeDeploymentArguments("Pepe", "PEPE", 8);
        //CoinContract pepe = new CoinContract(null, pepeMinter, testERC20Jar, arguments);
        Transaction createTransaction = Transaction.create(pepeMinter, kernel.getNonce(pepeMinter), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = TransactionContextImpl.forExternalTransaction(createTransaction, block);
        TransactionResult txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        Address tokenDapp = new Address(txResult.getReturnData());
        System.out.println(">> \"PEPE\" ERC20 token Dapp is deployed. (Address " + Helpers.bytesToHexString(txResult.getReturnData()) + ")");

        System.out.println("\n>> Deploy the Multi-sig Wallet Dapp...");
        jar = JarBuilder.buildJarForMainAndClassesAndUserlib(Main.class, Wallet.class, Bytes32.class);
        int confirmationsRequired = 2;
        arguments = ABIEncoder.encodeDeploymentArguments(new Address(owner1.toBytes()), new Address(owner2.toBytes()), confirmationsRequired);
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        Address walletDapp = new Address(txResult.getReturnData());
        System.out.println(">> Wallet Dapp is deployed. (Address " + Helpers.bytesToHexString(txResult.getReturnData()) + ")");
        System.out.println(">> Owners List:");
        System.out.println(">>   Deployer - (Address " + deployer + ")");
        System.out.println(">>   Owner 1  - (Address " + owner1 + ")");
        System.out.println(">>   Owner 2  - (Address " + owner2 + ")");
        System.out.println(">> Minimum number of owners to approve a transaction: " + confirmationsRequired);

        //================
        // FUNDING and CHECK BALANCE
        //================
        arguments = ABIEncoder.encodeMethodArguments("mint", walletDapp, 5000L);
        tx = Transaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> PEPE Mint to deliver 5000 tokens to the wallet: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", walletDapp);
        tx = Transaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of wallet: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", new Address(receiver.toBytes()));
        tx = Transaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        //================
        // PROPOSE
        //================
        byte[] data = ABIEncoder.encodeMethodArguments("transfer", new Address(receiver.toBytes()), 3000L);
        arguments = ABIEncoder.encodeMethodArguments("propose", tokenDapp, 0L, data, energyLimit);
        tx = Transaction.call(deployer, org.aion.types.Address.wrap(walletDapp.unwrap()), kernel.getNonce(deployer), BigInteger.ZERO, arguments, 2_000_000L, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> Deployer to propose a transaction of 3000 PEPE tokens to Receiver. (Tx ID " + Helpers.bytesToHexString((byte[]) ABIDecoder.decodeOneObject(txResult.getReturnData())) + ")");
        byte[] pendingTx = (byte[]) ABIDecoder.decodeOneObject(txResult.getReturnData());

        //================
        // CONFIRM #1
        //================
        arguments = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = Transaction.call(owner1, org.aion.types.Address.wrap(walletDapp.unwrap()), kernel.getNonce(owner1), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 1: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        //================
        // CONFIRM #2
        //================
        arguments = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = Transaction.call(owner2, org.aion.types.Address.wrap(walletDapp.unwrap()), kernel.getNonce(owner2), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 2: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        System.out.println("\n>> Number of confirmations reach to " + confirmationsRequired + ". Transaction is processed.");

        //================
        // CHECK BALANCE
        //================
        arguments = ABIEncoder.encodeMethodArguments("balanceOf", walletDapp);
        tx = Transaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> balance of wallet: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", new Address(receiver.toBytes()));
        tx = Transaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = TransactionContextImpl.forExternalTransaction(tx, block);
        txResult = avm.run(kernel, new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));
        avm.shutdown();
    }
}
