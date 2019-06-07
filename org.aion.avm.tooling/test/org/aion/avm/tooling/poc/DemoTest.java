package org.aion.avm.tooling.poc;

import avm.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.StandardCapabilities;
import org.aion.avm.tooling.testExchange.CoinController;
import org.aion.avm.tooling.testExchange.ERC20Token;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;


public class DemoTest {
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
        byte[] arguments = ABIUtil.encodeDeploymentArguments("Pepe", "PEPE", 8);
        //CoinContract pepe = new CoinContract(null, pepeMinter, testERC20Jar, arguments);
        TestingTransaction createTransaction = TestingTransaction.create(pepeMinter, kernel.getNonce(pepeMinter), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new TestingTransaction[] {createTransaction})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        Address tokenDapp = new Address(txResult.getReturnData());
        System.out.println(">> \"PEPE\" ERC20 token Dapp is deployed. (Address " + Helpers.bytesToHexString(txResult.getReturnData()) + ")");

        System.out.println("\n>> Deploy the Multi-sig Wallet Dapp...");
        jar = JarBuilder.buildJarForMainAndClassesAndUserlib(Main.class, Wallet.class, Bytes32.class);
        int confirmationsRequired = 2;
        arguments = ABIUtil.encodeDeploymentArguments(new Address(owner1.toBytes()), new Address(owner2.toBytes()), confirmationsRequired);
        TestingTransaction tx = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
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
        arguments = ABIUtil.encodeMethodArguments("mint", walletDapp, 5000L);
        tx = TestingTransaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> PEPE Mint to deliver 5000 tokens to the wallet: " + new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", walletDapp);
        tx = TestingTransaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of wallet: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", new Address(receiver.toBytes()));
        tx = TestingTransaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());

        //================
        // PROPOSE
        //================
        byte[] data = ABIUtil.encodeMethodArguments("transfer", new Address(receiver.toBytes()), 3000L);
        arguments = ABIUtil.encodeMethodArguments("propose", tokenDapp, 0L, data, energyLimit);
        tx = TestingTransaction.call(deployer, org.aion.types.Address.wrap(walletDapp.toByteArray()), kernel.getNonce(deployer), BigInteger.ZERO, arguments, 2_000_000L, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        byte[] pendingTx = new ABIDecoder(txResult.getReturnData()).decodeOneByteArray();
        System.out.println("\n>> Deployer to propose a transaction of 3000 PEPE tokens to Receiver. (Tx ID " + Helpers.bytesToHexString(pendingTx) + ")");

        //================
        // CONFIRM #1
        //================
        arguments = ABIUtil.encodeMethodArguments("confirm", pendingTx);
        tx = TestingTransaction.call(owner1, org.aion.types.Address.wrap(walletDapp.toByteArray()), kernel.getNonce(owner1), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 1: " + new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        //================
        // CONFIRM #2
        //================
        arguments = ABIUtil.encodeMethodArguments("confirm", pendingTx);
        tx = TestingTransaction.call(owner2, org.aion.types.Address.wrap(walletDapp.toByteArray()), kernel.getNonce(owner2), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 2: " + new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        System.out.println("\n>> Number of confirmations reach to " + confirmationsRequired + ". Transaction is processed.");

        //================
        // CHECK BALANCE
        //================
        arguments = ABIUtil.encodeMethodArguments("balanceOf", walletDapp);
        tx = TestingTransaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> balance of wallet: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", new Address(receiver.toBytes()));
        tx = TestingTransaction.call(pepeMinter, org.aion.types.Address.wrap(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());
        avm.shutdown();
    }
}
