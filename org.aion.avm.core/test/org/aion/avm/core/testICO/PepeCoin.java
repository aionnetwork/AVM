package org.aion.avm.core.testICO;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;

public class PepeCoin{

    public static final String SYMBOL = "PEPE";

    public static final String NAME = "Pepe Coin";

    public final Address minter;

    public static final long TOTAL_SUPPLY = 10000000L;

    private AionMap<Address, Long> ledger;

    private AionMap<Address, AionMap<Address, Long>> allowance;

    public PepeCoin(Address minter){
        this.ledger = new AionMap<>();
        this.allowance = new AionMap<>();
        this.minter = minter;
    }

    public long totalSupply(){
        return TOTAL_SUPPLY;
    }

    public long balanceOf(Address tokenOwner){
        return this.ledger.get(tokenOwner);
    }

    public long allowance(Address tokenOwner, Address spender){
        return this.allowance.get(tokenOwner).get(spender);
    }

    public boolean transfer(Address receiver, long tokens){
        Address sender = BlockchainRuntime.getSender();

        long senderBalance = this.ledger.get(sender);
        long receiverBalance = this.ledger.get(receiver);

        if ((senderBalance >= tokens) && (tokens > 0) && (receiverBalance + tokens > 0)){
            this.ledger.put(sender, senderBalance - tokens);
            this.ledger.put(receiver, receiverBalance + tokens);
            return true;
        }

        return false;
    }

    public boolean approve(Address spender, long tokens){
        this.allowance.get(BlockchainRuntime.getSender()).put(spender, tokens);
        return true;
    }

    public boolean transferFrom(Address from, Address to, long tokens){
        Address sender = BlockchainRuntime.getSender();

        long fromBalance = this.ledger.get(from);
        long toBalance = this.ledger.get(to);
        long limit = this.allowance.get(from).get(sender);

        if ((fromBalance > tokens) && (limit > tokens) && (toBalance + tokens > 0)){
            this.ledger.put(from, fromBalance - tokens);
            this.allowance.get(from).put(sender, limit - tokens);
            this.ledger.put(to, toBalance - tokens);
            return true;
        }

        return false;
    }
}
