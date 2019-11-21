package org.aion.avm.embed.testExchange;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

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

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] args = encoder.encodeOneString("allowance")
            .encodeOneAddress(sender)
            .encodeOneAddress(Blockchain.getAddress())
            .toBytes();

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

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] args = encoder.encodeOneString("transferFrom")
            .encodeOneAddress(toProcess.getFrom())
            .encodeOneAddress(toProcess.getTo())
            .encodeOneLong(toProcess.getAmount())
            .toBytes();

        byte[] result = Blockchain.call(coinContract, BigInteger.ZERO, args, 1000000L).getReturnData();

        ABIDecoder decoder = new ABIDecoder(result);
        boolean decodedBool = decoder.decodeOneBoolean();

        if (decodedBool){
            toProcess = null;
        }

        return decodedBool;
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
