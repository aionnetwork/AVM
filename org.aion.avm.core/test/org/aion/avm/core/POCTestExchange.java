package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class POCTestExchange {
    private byte[] testERC20Jar;
    private byte[] testExchangeJar;

    @Before
    public void setup() {
        testERC20Jar = Helpers.readFileToBytes("../examples/build/testExchangeJar/com.example.testERC20.jar");
        testExchangeJar = Helpers.readFileToBytes("../examples/build/testExchangeJar/com.example.testExchange.jar");
    }

    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 5_000_000;

    private byte[] pepeMinter = Helpers.randomBytes(Address.LENGTH);
    private byte[] memeMinter = Helpers.randomBytes(Address.LENGTH);
    private byte[] exchangeOwner = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr1 = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr2 = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr3 = Helpers.randomBytes(Address.LENGTH);

    class CoinContract{
        private byte[] addr;
        private byte[] minter;

        CoinContract(byte[] contractAddr, byte[] minter, byte[] jar, byte[] arguments){
            this.addr = contractAddr;
            this.minter = minter;
            this.addr = initCoin(jar, arguments);
        }

        private byte[] initCoin(byte[] jar, byte[] arguments){
            Transaction createTransaction = new Transaction(Transaction.Type.CREATE, minter, addr, 0, Helpers.encodeCodeAndData(jar, arguments), energyLimit, 1l);
            TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
            TransactionResult createResult = new AvmImpl(new KernelInterfaceImpl()).run(createContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
            return createResult.getReturnData();
        }

        public TransactionResult callTotalSupply() throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("totalSupply");
            return call(minter, args);
        }

        private TransactionResult callBalanceOf(byte[] toQuery) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("balanceOf", new Address(toQuery));
            return call(minter, args);
        }

        private TransactionResult callOpenAccount(byte[] toOpen) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("openAccount", new Address(toOpen));
            return call(minter, args);
        }

        private TransactionResult callMint(byte[] receiver, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("mint", new Address(receiver), amount);
            return call(minter, args);
        }

        private TransactionResult callTransfer(byte[] sender, byte[] receiver, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("transfer", new Address(receiver), amount);
            return call(sender, args);
        }

        private TransactionResult callAllowance(byte[] owner, byte[] spender) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("allowance", new Address(owner), new Address(spender));
            return call(minter, args);
        }

        private TransactionResult callApprove(byte[] owner, byte[] spender, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("approve", new Address(spender), amount);
            return call(owner, args);
        }

        private TransactionResult callTransferFrom(byte[] executor, byte[] from, byte[] to, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("transferFrom", new Address(from), new Address(to), amount);
            return call(executor, args);
        }

        private TransactionResult call(byte[] sender, byte[] args) {
            Transaction callTransaction = new Transaction(Transaction.Type.CALL, sender, addr, 0, args, energyLimit, 1l);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = new AvmImpl(new KernelInterfaceImpl()).run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }
    }

    class ExchangeContract{
        private byte[] addr;
        private byte[] owner;

        ExchangeContract(byte[] contractAddr, byte[] owner, byte[] jar){
            this.addr = contractAddr;
            this.owner = owner;
            this.addr = initExchange(jar, null);
        }

        private byte[] initExchange(byte[] jar, byte[] arguments){
            Transaction createTransaction = new Transaction(Transaction.Type.CREATE, owner, addr, 0, Helpers.encodeCodeAndData(jar, arguments), energyLimit, 1l);
            TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
            TransactionResult createResult = new AvmImpl(new KernelInterfaceImpl()).run(createContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
            return createResult.getReturnData();
        }

        public TransactionResult callListCoin(String name, byte[] coinAddr) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("listCoin", name.toCharArray(), new Address(coinAddr));
            return call(owner,args);
        }

        public TransactionResult callRequestTransfer(String name, byte[] from,  byte[] to, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("requestTransfer", name.toCharArray(), new Address(to), amount);
            return call(from,args);
        }

        public TransactionResult callProcessExchangeTransaction(byte[] sender) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("processExchangeTransaction");
            return call(sender,args);
        }

        private TransactionResult call(byte[] sender, byte[] args) throws InvalidTxDataException {
            Transaction callTransaction = new Transaction(Transaction.Type.CALL, sender, addr, 0, args, energyLimit, 1l);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = new AvmImpl(new KernelInterfaceImpl()).run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }
    }

    @Test
    public void testERC20() throws InvalidTxDataException{
        TransactionResult res;
        System.out.println(">> Deploy \"PEPE\" token contract...");
        byte[] arguments = ABIEncoder.encodeMethodArguments("", "Pepe".toCharArray(), "PEPE".toCharArray(), 8);
        CoinContract pepe = new CoinContract(null, pepeMinter, testERC20Jar, arguments);

        res = pepe.callTotalSupply();
        Assert.assertEquals(0L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> total supply: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr1);
        Assert.assertEquals(0L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr2);
        Assert.assertEquals(0L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callMint(usr1, 5000L);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Mint to deliver 5000 tokens to User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr1);
        Assert.assertEquals(5000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callMint(usr2, 10000L);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Mint to deliver 10000 tokens to User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr2);
        Assert.assertEquals(10000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callTransfer(usr1, usr2, 2000L);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> User1 to transfer 2000 tokens to User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr1);
        Assert.assertEquals(3000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr2);
        Assert.assertEquals(12000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callAllowance(usr1, usr2);
        Assert.assertEquals(0L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Allowance User1 grants to User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callApprove(usr1, usr3, 1000L);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> User1 grants User3 the allowance of 1000 tokens: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callAllowance(usr1, usr3);
        Assert.assertEquals(1000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Allowance User1 grants to User3: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callTransferFrom(usr3, usr1, usr2, 500L);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> User3 to transfer 500 tokens to User2, from the allowance granted by User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callAllowance(usr1, usr3);
        Assert.assertEquals(500L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Allowance User1 grants to User3: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr1);
        Assert.assertEquals(2500L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr2);
        Assert.assertEquals(12500L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));
    }

    @Test
    public void testExchange() throws InvalidTxDataException{
        System.out.println(">> Deploy \"PEPE\" token contract...");
        byte[] arguments = ABIEncoder.encodeMethodArguments("", "Pepe".toCharArray(), "PEPE".toCharArray(), 8);
        CoinContract pepe = new CoinContract(null, pepeMinter, testERC20Jar, arguments);

        System.out.println(">> Deploy \"MEME\" token contract...");
        arguments = ABIEncoder.encodeMethodArguments("", "Meme".toCharArray(), "MEME".toCharArray(), 8);
        CoinContract meme = new CoinContract(null, memeMinter, testERC20Jar, arguments);

        System.out.println(">> Deploy the Exchange contract...");
        ExchangeContract ex = new ExchangeContract(null, exchangeOwner, testExchangeJar);

        TransactionResult res;

        res = ex.callListCoin("PEPE", pepe.addr);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> List \"PEPE\" token on Exchange: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = ex.callListCoin("MEME", meme.addr);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> List \"MEME\" token on Exchange: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callMint(usr1, 5000L);
        System.out.println(">> Mint to deliver 5000 tokens to User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));
        res = pepe.callMint(usr2, 5000L);
        System.out.println(">> Mint to deliver 5000 tokens to User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callApprove(usr1, ex.addr, 2000L);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> User1 grants to the Exchange the allowance of 2000 tokens: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = ex.callRequestTransfer("PEPE", usr1, usr2, 1000L);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Exchange to request transfer 1000 tokens from User1 to User2, from the allowance granted by User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        //res = pepe.callAllowance(usr1, ex.addr);
        //Assert.assertEquals(2000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        //System.out.println(">> User1 grants to the Exchange the allowance of 2000 tokens: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = ex.callProcessExchangeTransaction(exchangeOwner);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Exchange to process the transactions: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr1);
        Assert.assertEquals(4000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User1: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callBalanceOf(usr2);
        Assert.assertEquals(6000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> balance of User2: " + ABIDecoder.decodeOneObject(res.getReturnData()));

        res = pepe.callAllowance(usr1, ex.addr);
        Assert.assertEquals(1000L, ABIDecoder.decodeOneObject(res.getReturnData()));
        System.out.println(">> Allowance User1 grants to Exchange: " + ABIDecoder.decodeOneObject(res.getReturnData()));
    }
}
