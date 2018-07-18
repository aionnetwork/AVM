package org.aion.avm.core.testICO;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;

public class PepeCoin implements IAionToken{
    public static final String SYMBOL = "PEPE";

    public static final String NAME = "Pepe Coin";

    public static final long TOTAL_SUPPLY = 10000000L;

    public final Address minter;

    private AionMap<Address, Long> ledger;

    private AionMap<Address, AionMap<Address, Long>> allowance;

    public PepeCoin(Address minter){
        this.ledger = new AionMap<>();
        this.allowance = new AionMap<>();
        this.minter = minter;
    }

    private boolean checkAccount(Address toCheck){
        return ledger.containsKey(toCheck);
    }

    public boolean mint(Address receiver, long tokens){
        if (BlockchainRuntime.getSender().equals(this.minter)){
            long receiverBalance = this.ledger.get(receiver);
            if ((tokens > 0) && (receiverBalance + tokens > 0)){
                this.ledger.put(receiver, receiverBalance + tokens);
                BlockchainRuntime.log("mint".getBytes(), receiver.unwrap());
                return true;
            }
        }
        return false;
    }

    public boolean openAccount(Address request){
        if (BlockchainRuntime.getSender().equals(this.minter)){
            if (!checkAccount(request)){
                this.ledger.put(request, 0L);
                return true;
            }
        }
        return false;
    }

    public long totalSupply(){
        return TOTAL_SUPPLY;
    }

    public long balanceOf(Address tokenOwner){
        if (this.ledger.containsKey(tokenOwner)){
            return this.ledger.get(tokenOwner);
        }

        return -1;
    }

    public long allowance(Address tokenOwner, Address spender){
        return this.allowance.get(tokenOwner).get(spender);
    }

    public boolean transfer(Address receiver, long tokens){
        Address sender = BlockchainRuntime.getSender();

        if (!(checkAccount(sender) && checkAccount(receiver))){
            return false;
        }

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
