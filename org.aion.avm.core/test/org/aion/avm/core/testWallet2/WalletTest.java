package org.aion.avm.core.testWallet2;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class WalletTest {

    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 5_000_000;

    private byte[] pepeMinter = Helpers.randomBytes(Address.LENGTH);
    private byte[] deployer = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner1 = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner2 = Helpers.randomBytes(Address.LENGTH);
    private byte[] receiver = Helpers.randomBytes(Address.LENGTH);

    @Test
    public void testWallet() throws InvalidTxDataException {

        //================
        // DEPLOY
        //================

        System.out.println(">> Deploy \"PEPE\" ERC20 token Dapp...");
        byte[] jar = Helpers.readFileToBytes("../examples/build/testExchangeJar/com.example.testERC20.jar");
        byte[] arguments = ABIEncoder.encodeMethodArguments("", "Pepe".toCharArray(), "PEPE".toCharArray(), 8);
        //CoinContract pepe = new CoinContract(null, pepeMinter, testERC20Jar, arguments);
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, pepeMinter, null, 0, Helpers.encodeCodeAndData(jar, arguments), energyLimit);
        TransactionContext txContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult txResult = new AvmImpl().run(txContext);
        Address tokenDapp = new Address(txResult.getReturnData());
        System.out.println(">> \"PEPE\" ERC20 token Dapp is deployed. (Address " + Helpers.toHexString(txResult.getReturnData()) + ")");

        System.out.println("\n>> Deploy the Multi-sig Wallet Dapp...");
        //byte[] jar = JarBuilder.buildJarForMainAndClasses(Main.class, Wallet.class, Bytes32.class, AionSet.class, AionMap.class);
        jar = Helpers.readFileToBytes("../examples/build/com.example.testWallet.jar");
        int confirmationsRequired = 2;
        arguments = ABIEncoder.encodeMethodArguments("", new Address(owner1), new Address(owner2), confirmationsRequired);
        Transaction tx = new Transaction(Transaction.Type.CREATE, deployer, null, 0L, Helpers.encodeCodeAndData(jar, arguments), energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        Address walletDapp = new Address(txResult.getReturnData());
        System.out.println(">> Wallet Dapp is deployed. (Address " + Helpers.toHexString(txResult.getReturnData()) + ")");
        System.out.println(">> Owners List:");
        System.out.println(">>   Deployer - (Address " + Helpers.toHexString(deployer) + ")");
        System.out.println(">>   Owner 1  - (Address " + Helpers.toHexString(owner1) + ")");
        System.out.println(">>   Owner 2  - (Address " + Helpers.toHexString(owner2) + ")");
        System.out.println(">> Minimum number of owners to approve a transaction: " + confirmationsRequired);

        //================
        // FUNDING and CHECK BALANCE
        //================
        arguments = ABIEncoder.encodeMethodArguments("mint", walletDapp, 5000L);
        tx = new Transaction(Transaction.Type.CALL, pepeMinter, tokenDapp.unwrap(), 0, arguments, energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println("\n>> PEPE Mint to deliver 5000 tokens to the wallet: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", walletDapp);
        tx = new Transaction(Transaction.Type.CALL, pepeMinter, tokenDapp.unwrap(), 0, arguments, energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> balance of wallet: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", new Address(receiver));
        tx = new Transaction(Transaction.Type.CALL, pepeMinter, tokenDapp.unwrap(), 0, arguments, energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> balance of receiver: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        //================
        // PROPOSE
        //================
        byte[] data = ABIEncoder.encodeMethodArguments("transfer", new Address(receiver), 3000L);
        arguments = ABIEncoder.encodeMethodArguments("propose", tokenDapp, 0L, data, energyLimit);
        tx = new Transaction(Transaction.Type.CALL, deployer, walletDapp.unwrap(), 0L, arguments, 2_000_000L);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println("\n>> Deployer to propose a transaction of 3000 PEPE tokens to Receiver. (Tx ID " + Helpers.toHexString((byte[]) ABIDecoder.decodeOneObject(txResult.getReturnData())) + ")");
        byte[] pendingTx = (byte[]) ABIDecoder.decodeOneObject(txResult.getReturnData());

        //================
        // CONFIRM #1
        //================
        arguments = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = new Transaction(Transaction.Type.CALL, owner1, walletDapp.unwrap(), 0L, arguments, energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> Transaction confirmed by Owner 1: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        //================
        // CONFIRM #2
        //================
        arguments = ABIEncoder.encodeMethodArguments("confirm", pendingTx);
        tx = new Transaction(Transaction.Type.CALL, owner2, walletDapp.unwrap(), 0L, arguments, energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> Transaction confirmed by Owner 2: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        System.out.println("\n>> Number of confirmations reach to " + confirmationsRequired + ". Transaction is processed.");

        //================
        // CHECK BALANCE
        //================
        arguments = ABIEncoder.encodeMethodArguments("balanceOf", walletDapp);
        tx = new Transaction(Transaction.Type.CALL, pepeMinter, tokenDapp.unwrap(), 0, arguments, energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println("\n>> balance of wallet: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));

        arguments = ABIEncoder.encodeMethodArguments("balanceOf", new Address(receiver));
        tx = new Transaction(Transaction.Type.CALL, pepeMinter, tokenDapp.unwrap(), 0, arguments, energyLimit);
        txContext = new TransactionContextImpl(tx, block);
        txResult = new AvmImpl().run(txContext);
        System.out.println(">> balance of receiver: " + ABIDecoder.decodeOneObject(txResult.getReturnData()));
    }
}
