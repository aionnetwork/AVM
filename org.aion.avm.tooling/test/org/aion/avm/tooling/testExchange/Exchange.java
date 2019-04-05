package org.aion.avm.tooling.testExchange;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class Exchange {

    private static AionMap<String, Address> coinListing;

    private static ExchangeTransaction toProcess;

    private static Address owner;

    public static void init(){
        owner = Blockchain.getCaller();
        coinListing = new AionMap<>();
    }

    private static boolean verifyContractAddress(char[] name, Address contract){
        return true;
    }

    public static boolean listCoin(char[] name, Address contractAddr){
        if (coinListing.containsKey(String.valueOf(name))){
            return false;
        }

        if (Blockchain.getCaller().equals(owner) && verifyContractAddress(name, contractAddr)){
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

        Address sender = Blockchain.getCaller();

        byte[] methodNameBytes = ABIEncoder.encodeOneString("allowance");
        byte[] argBytes1 = ABIEncoder.encodeOneAddress(sender);
        byte[] argBytes2 = ABIEncoder.encodeOneAddress(Blockchain.getAddress());
        byte[] args = concatenateArrays(methodNameBytes, argBytes1, argBytes2);

        byte[] result = Blockchain.call(coinContract, BigInteger.ZERO, args, 1000000L).getReturnData();

        ABIDecoder decoder = new ABIDecoder(result);

        if (decoder.decodeOneLong() >= amount){
            toProcess = new ExchangeTransaction(coin, sender, to, amount);
            return true;
        }

        return false;
    }

    public static boolean processExchangeTransaction(){
        if (!Blockchain.getCaller().equals(owner)){
            return false;
        }

        if (null == toProcess){
            return false;
        }

        if (!coinListing.containsKey(String.valueOf(toProcess.getCoin()))){
            return false;
        }

        Address coinContract = coinListing.get(String.valueOf(toProcess.getCoin()));

        byte[] methodNameBytes = ABIEncoder.encodeOneString("transferFrom");
        byte[] argBytes1 = ABIEncoder.encodeOneAddress(toProcess.getFrom());
        byte[] argBytes2 = ABIEncoder.encodeOneAddress(toProcess.getTo());
        byte[] argBytes3 = ABIEncoder.encodeOneLong(toProcess.getAmount());
        byte[] args = concatenateArrays(methodNameBytes, argBytes1, argBytes2, argBytes3);

        byte[] result = Blockchain.call(coinContract, BigInteger.ZERO, args, 1000000L).getReturnData();

        ABIDecoder decoder = new ABIDecoder(result);
        boolean decodedBool = decoder.decodeOneBoolean();

        if (decodedBool){
            toProcess = null;
        }

        return decodedBool;
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
