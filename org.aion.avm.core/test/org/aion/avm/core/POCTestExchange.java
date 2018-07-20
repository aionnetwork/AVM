package org.aion.avm.core;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.testExchange.*;
import org.aion.avm.core.testWallet.ByteArrayHelpers;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class POCTestExchange {

    private static AvmImpl avm;

    private byte[] pepeJar;
    private byte[] memeJar;

    @Before
    public void setup() {
        avm = new AvmImpl();
        pepeJar = Helpers.readFileToBytes("../examples/build/testExchangeJar/com.example.pepe.jar");
        memeJar = Helpers.readFileToBytes("../examples/build/testExchangeJar/com.example.meme.jar");
    }

    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 5_000_000;

    private byte[] pepeCoinAddr = Helpers.randomBytes(Address.LENGTH);
    private byte[] memeCoinAddr = Helpers.randomBytes(Address.LENGTH);
    private byte[] pepeMinter = Helpers.randomBytes(Address.LENGTH);
    private byte[] memeMinter = Helpers.randomBytes(Address.LENGTH);
    private byte[] owner = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr1 = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr2 = Helpers.randomBytes(Address.LENGTH);
    private byte[] usr3 = Helpers.randomBytes(Address.LENGTH);

    class CoinContract{
        private byte[] addr;
        private byte[] minter;

        CoinContract(byte[] contractAddr, byte[] minter, byte[] jar){
            this.addr = contractAddr;
            this.minter = minter;
            this.addr = initCoin(jar);
        }

        private byte[] initCoin(byte[] jar){
            Transaction createTransaction = new Transaction(Transaction.Type.CREATE, minter, addr, 0, jar, energyLimit);
            TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
            TransactionResult createResult = avm.run(createContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
            return createResult.getReturnData();
        }

        public TransactionResult callTotalSupply() throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("totalSupply");

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }

        private TransactionResult callBalanceOf(byte[] toQuery) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("balanceOf", new ByteArray(toQuery));
            // TODO - consider to add "Address" to ABI

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }

        private TransactionResult callOpenAccount(byte[] toOpen) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("openAccount", new ByteArray(toOpen));

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }

        private TransactionResult callMint(byte[] receiver) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("mint", new ByteArray(receiver), 5000L);

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }

        private TransactionResult callTransfer(byte[] sender, byte[] receiver, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("transfer", new ByteArray(receiver), amount);

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, sender, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }

        private TransactionResult callAllowance(byte[] owner, byte[] spender) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("allowance", new ByteArray(owner), new ByteArray(spender));

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, minter, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }

        private TransactionResult callApprove(byte[] owner, byte[] spender, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("approve", new ByteArray(spender), amount);

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, owner, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }

        private TransactionResult callTransferFrom(byte[] executor, byte[] from, byte[] to, long amount) throws InvalidTxDataException {
            byte[] args = ABIEncoder.encodeMethodArguments("transferFrom", new ByteArray(from), new ByteArray(to), amount);

            Transaction callTransaction = new Transaction(Transaction.Type.CALL, executor, addr, 0, args, energyLimit);
            TransactionContext callContext = new TransactionContextImpl(callTransaction, block);
            TransactionResult callResult = avm.run(callContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, callResult.getStatusCode());
            return callResult;
        }
    }

    @Test
    public void testERC20() throws InvalidTxDataException{

        TransactionResult res;

        CoinContract pepe = new CoinContract(pepeCoinAddr, pepeMinter, pepeJar);

        res = pepe.callTotalSupply();

        Assert.assertEquals(PepeCoin.TOTAL_SUPPLY, ByteArrayHelpers.decodeLong(res.getReturnData()));
        // TODO - use ABI decoder to decode the returnData

        res = pepe.callBalanceOf(usr1);

        Assert.assertEquals(-1L, ByteArrayHelpers.decodeLong(res.getReturnData()));

        res = pepe.callOpenAccount(usr1);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.getReturnData()));

        res = pepe.callBalanceOf(usr1);

        Assert.assertEquals(0L, ByteArrayHelpers.decodeLong(res.getReturnData()));

        res = pepe.callOpenAccount(usr1);

        Assert.assertEquals(false, ByteArrayHelpers.decodeBoolean(res.getReturnData()));

        res = pepe.callBalanceOf(usr2);

        Assert.assertEquals(-1L, ByteArrayHelpers.decodeLong(res.getReturnData()));

        res = pepe.callOpenAccount(usr2);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.getReturnData()));

        res = pepe.callOpenAccount(usr3);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.getReturnData()));

        res = pepe.callMint(usr1);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.getReturnData()));

        // TODO - fix these tests after using ABI decoder
/*
        res = pepe.callBalanceOf(usr1);

        Assert.assertEquals(5000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callMint(usr2);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = pepe.callMint(usr2);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = pepe.callBalanceOf(usr2);

        Assert.assertEquals(10000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callTransfer(usr1, usr2, 2000L);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = pepe.callBalanceOf(usr1);

        Assert.assertEquals(3000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callBalanceOf(usr2);

        Assert.assertEquals(12000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callAllowance(usr1, usr2);

        Assert.assertEquals(0L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callApprove(usr1, usr3, 1000L);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = pepe.callAllowance(usr1, usr3);

        Assert.assertEquals(1000L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callTransferFrom(usr3, usr1, usr2, 500L);

        Assert.assertEquals(true, ByteArrayHelpers.decodeBoolean(res.returnData));

        res = pepe.callAllowance(usr1, usr3);

        Assert.assertEquals(500L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callBalanceOf(usr1);

        Assert.assertEquals(2500L, ByteArrayHelpers.decodeLong(res.returnData));

        res = pepe.callBalanceOf(usr2);

        Assert.assertEquals(12500L, ByteArrayHelpers.decodeLong(res.returnData));*/
    }


    @Test
    public void testExchange() {
        CoinContract pepe = new CoinContract(pepeCoinAddr, pepeMinter, pepeJar);
        CoinContract meme = new CoinContract(memeCoinAddr, memeMinter, memeJar);
    }
}
