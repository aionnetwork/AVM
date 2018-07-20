package com.example.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class PepeController {

    private static IAionToken coinbase;

    public static void init(){
        coinbase = new PepeCoin(BlockchainRuntime.getSender());
    }

    public long totalSupply(){
        return coinbase.totalSupply();
    }

    public long balanceOf(byte[] tokenOwner){
        return coinbase.balanceOf(new Address(tokenOwner));
    }

    public long allowance(byte[] tokenOwner, byte[] spender){
        return coinbase.allowance(new Address(tokenOwner),new Address(spender));
    }

    public boolean transfer(byte[] to, long tokens){
        return coinbase.transfer(new Address(to), tokens);
    }

    public boolean approve(byte[] spender, long tokens){
        return coinbase.approve(new Address(spender), tokens);
    }

    public boolean transferFrom(byte[] spender, byte[] to, long tokens){
        return coinbase.transferFrom(new Address(spender), new Address(to), tokens);
    }

    public boolean mint(byte[] receiver, long tokens){
        return coinbase.mint(new Address(receiver), tokens);
    }

    public boolean openAccount(byte[] request){
        return coinbase.openAccount(new Address(request));
    }

    public static byte[] main(){
        return ABIDecoder.decodeAndRun(new PepeController(), BlockchainRuntime.getData());
    }
}
