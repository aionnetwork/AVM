package org.aion.avm.tooling.testExchange;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class Exchange {

    private static AionMap<String, Address> coinListing;

    private static ExchangeTransaction toProcess;

    private static Address owner;

    public static void init(){
        owner = BlockchainRuntime.getCaller();
        coinListing = new AionMap<>();
    }

    private static boolean verifyContractAddress(char[] name, Address contract){
        return true;
    }

    public static boolean listCoin(char[] name, Address contractAddr){
        if (coinListing.containsKey(String.valueOf(name))){
            return false;
        }

        if (BlockchainRuntime.getCaller().equals(owner) && verifyContractAddress(name, contractAddr)){
            coinListing.put(String.valueOf(name), contractAddr);
            return true;
        }

        return false;
    }

    public boolean postBid(char[] coin, long amount, char[] offerCoin, long bidAmount){
        return true;
    }

    public boolean postAsk(char[] coin, long amount, char[] askCoin){
        return true;
    }

    public static boolean requestTransfer(char[] coin, Address to, long amount){
        if (!coinListing.containsKey(String.valueOf(coin))){
            return false;
        }
        Address coinContract = coinListing.get(String.valueOf(coin));

        Address sender = BlockchainRuntime.getCaller();

        byte[] methodNameBytes = ABIEncoder.encodeOneString("allowance");
        byte[] argBytes1 = ABIEncoder.encodeOneAddress(sender);
        byte[] argBytes2 = ABIEncoder.encodeOneAddress(BlockchainRuntime.getAddress());
        byte[] args = concatenateArrays(methodNameBytes, argBytes1, argBytes2);

        byte[] result = BlockchainRuntime.call(coinContract, BigInteger.ZERO, args, 1000000L).getReturnData();

        if (((long)ABIDecoder.decodeOneObject(result)) >= amount){
            toProcess = new ExchangeTransaction(coin, sender, to, amount);
            return true;
        }

        return false;
    }

    public static boolean processExchangeTransaction(){
        if (!BlockchainRuntime.getCaller().equals(owner)){
            return false;
        }

        if (null == toProcess){
            return false;
        }

        if (!coinListing.containsKey(String.valueOf(toProcess.getCoin()))){
            return false;
        }

        Address coinContract = coinListing.get(String.valueOf(toProcess.getCoin()));

        byte[] args = ABIEncoder.encodeMethodArguments("transferFrom", toProcess.getFrom(), toProcess.getTo(), toProcess.getAmount());

        byte[] result = BlockchainRuntime.call(coinContract, BigInteger.ZERO, args, 1000000L).getReturnData();

        if ((boolean)ABIDecoder.decodeOneObject(result)){
            toProcess = null;
        }

        return (boolean)ABIDecoder.decodeOneObject(result);
    }

    private static byte[] concatenateArrays(byte[]... arrays) {
        int length = 0;
        for(byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int writtenSoFar = 0;
        for(byte[] array : arrays) {
            System.arraycopy(array, 0, result, writtenSoFar, array.length);
            writtenSoFar += array.length;
        }
        return result;
    }

    public static class ExchangeTransaction {

        private Address from;
        private Address to;
        private char[] coin;
        private long amount;

        ExchangeTransaction(char[] coin, Address from, Address to, long amount){
            this.from = from;
            this.to = to;
            this.coin = coin;
            this.amount = amount;
        }

        public Address getFrom() {
            return from;
        }

        public Address getTo() {
            return to;
        }

        public char[] getCoin() {
            return coin;
        }

        public long getAmount() {
            return amount;
        }
    }
}
