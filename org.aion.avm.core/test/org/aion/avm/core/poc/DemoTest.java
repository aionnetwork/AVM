package org.aion.avm.core.poc;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.testExchange.CoinController;
import org.aion.avm.core.testExchange.ERC20;
import org.aion.avm.core.testExchange.ERC20Token;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class DemoTest {

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 5_000_0000;
    private long energyPrice = 1;

    private org.aion.vm.api.interfaces.Address pepeMinter = Helpers.randomAddress();
    private org.aion.vm.api.interfaces.Address deployer = Helpers.randomAddress();
    private org.aion.vm.api.interfaces.Address owner1 = Helpers.randomAddress();
    private org.aion.vm.api.interfaces.Address owner2 = Helpers.randomAddress();
    private org.aion.vm.api.interfaces.Address receiver = Helpers.randomAddress();

    @Test
    public void testWallet() {
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);
        kernel.adjustBalance(pepeMinter, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(deployer, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(owner1, BigInteger.valueOf(1_000_000_000L));
        kernel.adjustBalance(owner2, BigInteger.valueOf(1_000_000_000L));

        //================
        // DEPLOY
        //================

        System.out.println(">> Deploy \"PEPE\" ERC20 token Dapp...");
        byte[] jar = JarBuilder.buildJarForMainAndClasses(CoinController.class, ERC20.class, ERC20Token.class, AionList.class, AionSet.class, AionMap.class);
        byte[] arguments = ABIEncoder.encodeMethodArguments("", "Pepe".toCharArray(), "PEPE".toCharArray(), 8);
        //CoinContract pepe = new CoinContract(null, pepeMinter, testERC20Jar, arguments);
        Transaction createTransaction = Transaction.create(pepeMinter, kernel.getNonce(pepeMinter).longValue(), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext txContext = new TransactionContextImpl(createTransaction, block);
        AvmTransactionResult txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        Address tokenDapp = TestingHelper.buildAddress(txResult.getReturnData());
        System.out.println(">> \"PEPE\" ERC20 token Dapp is deployed. (Address " + Helpers.bytesToHexString(txResult.getReturnData()) + ")");

        System.out.println("\n>> Deploy the Multi-sig Wallet Dapp...");
        jar = JarBuilder.buildJarForMainAndClasses(Main.class, Wallet.class, Bytes32.class, AionList.class, AionSet.class, AionMap.class, AionBuffer.class);
        int confirmationsRequired = 2;
        arguments = ABIEncoder.encodeMethodArguments("", TestingHelper.buildAddress(owner1.toBytes()), TestingHelper.buildAddress(owner2.toBytes()), confirmationsRequired);
        Transaction tx = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        Address walletDapp = TestingHelper.buildAddress(txResult.getReturnData());
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
        tx = Transaction.call(pepeMinter, AvmAddress.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter).longValue(), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> PEPE Mint to deliver 5000 tokens to the wallet: " + TestingHelper.decodeResult(txResult));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", walletDapp);
        tx = Transaction.call(pepeMinter, AvmAddress.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter).longValue(), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of wallet: " + TestingHelper.decodeResult(txResult));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", TestingHelper.buildAddress(receiver.toBytes()));
        tx = Transaction.call(pepeMinter, AvmAddress.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter).longValue(), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + TestingHelper.decodeResult(txResult));

        //================
        // PROPOSE
        //================
        byte[] data = ABIEncoder.encodeMethodArguments("transfer", TestingHelper.buildAddress(receiver.toBytes()), 3000L);
        arguments = ABIEncoder.encodeMethodArguments("propose", tokenDapp, 0L, data, energyLimit);
        tx = Transaction.call(deployer, AvmAddress.wrap(walletDapp.unwrap()), kernel.getNonce(deployer).longValue(), BigInteger.ZERO, arguments, 2_000_000L, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> Deployer to propose a transaction of 3000 PEPE tokens to Receiver. (Tx ID " + Helpers.bytesToHexString((byte[]) TestingHelper.decodeResult(txResult)) + ")");
        byte[] pendingTx = (byte[]) TestingHelper.decodeResult(txResult);

        //================
        // CONFIRM #1
        //================
        arguments = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = Transaction.call(owner1, AvmAddress.wrap(walletDapp.unwrap()), kernel.getNonce(owner1).longValue(), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 1: " + TestingHelper.decodeResult(txResult));

        //================
        // CONFIRM #2
        //================
        arguments = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = Transaction.call(owner2, AvmAddress.wrap(walletDapp.unwrap()), kernel.getNonce(owner2).longValue(), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> Transaction confirmed by Owner 2: " + TestingHelper.decodeResult(txResult));

        System.out.println("\n>> Number of confirmations reach to " + confirmationsRequired + ". Transaction is processed.");

        //================
        // CHECK BALANCE
        //================
        arguments = ABIEncoder.encodeMethodArguments("balanceOf", walletDapp);
        tx = Transaction.call(pepeMinter, AvmAddress.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter).longValue(), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println("\n>> balance of wallet: " + TestingHelper.decodeResult(txResult));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", TestingHelper.buildAddress(receiver.toBytes()));
        tx = Transaction.call(pepeMinter, AvmAddress.wrap(tokenDapp.unwrap()), kernel.getNonce(pepeMinter).longValue(), BigInteger.ZERO, arguments, energyLimit, energyPrice);
        txContext = new TransactionContextImpl(tx, block);
        txResult = avm.run(new TransactionContext[] {txContext})[0].get();
        assertTrue(txResult.getResultCode().isSuccess());
        System.out.println(">> balance of receiver: " + TestingHelper.decodeResult(txResult));
        avm.shutdown();
    }
}
