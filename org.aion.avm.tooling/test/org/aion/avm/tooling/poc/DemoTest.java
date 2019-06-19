package org.aion.avm.tooling.poc;

import org.aion.avm.core.AvmTransactionUtil;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import avm.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.StandardCapabilities;
import org.aion.avm.tooling.testExchange.CoinController;
import org.aion.avm.tooling.testExchange.ERC20Token;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;


public class DemoTest {
    private long energyLimit = 5_000_0000;
    private long energyPrice = 1;

    private AionAddress pepeMinter = Helpers.randomAddress();
    private AionAddress deployer = Helpers.randomAddress();
    private AionAddress owner1 = Helpers.randomAddress();
    private AionAddress owner2 = Helpers.randomAddress();
    private AionAddress receiver = Helpers.randomAddress();

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
        Transaction createTransaction = AvmTransactionUtil.create(pepeMinter, kernel.getNonce(pepeMinter), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new Transaction[] {createTransaction})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        Address tokenDapp = new Address(txResult.getReturnData());
        System.out.println(">> \"PEPE\" ERC20 token Dapp is deployed. (Address " + Helpers.bytesToHexString(txResult.getReturnData()) + ")");

        System.out.println("\n>> Deploy the Multi-sig Wallet Dapp...");
        jar = JarBuilder.buildJarForMainAndClassesAndUserlib(Main.class, Wallet.class, Bytes32.class);
        int confirmationsRequired = 2;
        arguments = ABIUtil.encodeDeploymentArguments(new Address(owner1.toByteArray()), new Address(owner2.toByteArray()), confirmationsRequired);
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
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
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> PEPE Mint to deliver 5000 tokens to the wallet: " + new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", walletDapp);
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of wallet: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", new Address(receiver.toByteArray()));
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());

        //================
        // PROPOSE
        //================
        byte[] data = ABIUtil.encodeMethodArguments("transfer", new Address(receiver.toByteArray()), 3000L);
        arguments = ABIUtil.encodeMethodArguments("propose", tokenDapp, 0L, data, energyLimit);
        tx = AvmTransactionUtil.call(deployer, new AionAddress(walletDapp.toByteArray()), kernel.getNonce(deployer), BigInteger.ZERO, arguments, 2_000_000L, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        byte[] pendingTx = new ABIDecoder(txResult.getReturnData()).decodeOneByteArray();
        System.out.println("\n>> Deployer to propose a transaction of 3000 PEPE tokens to Receiver. (Tx ID " + Helpers.bytesToHexString(pendingTx) + ")");

        //================
        // CONFIRM #1
        //================
        arguments = ABIUtil.encodeMethodArguments("confirm", pendingTx);
        tx = AvmTransactionUtil.call(owner1, new AionAddress(walletDapp.toByteArray()), kernel.getNonce(owner1), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 1: " + new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        //================
        // CONFIRM #2
        //================
        arguments = ABIUtil.encodeMethodArguments("confirm", pendingTx);
        tx = AvmTransactionUtil.call(owner2, new AionAddress(walletDapp.toByteArray()), kernel.getNonce(owner2), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 2: " + new ABIDecoder(txResult.getReturnData()).decodeOneBoolean());

        System.out.println("\n>> Number of confirmations reach to " + confirmationsRequired + ". Transaction is processed.");

        //================
        // CHECK BALANCE
        //================
        arguments = ABIUtil.encodeMethodArguments("balanceOf", walletDapp);
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> balance of wallet: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());

        arguments = ABIUtil.encodeMethodArguments("balanceOf", new Address(receiver.toByteArray()));
        tx = AvmTransactionUtil.call(pepeMinter, new AionAddress(tokenDapp.toByteArray()), kernel.getNonce(pepeMinter), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txResult = avm.run(kernel, new Transaction[] {tx})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + new ABIDecoder(txResult.getReturnData()).decodeOneLong());
        avm.shutdown();
    }
}
