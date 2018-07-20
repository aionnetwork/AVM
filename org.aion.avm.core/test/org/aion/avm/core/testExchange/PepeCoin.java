package org.aion.avm.core.testExchange;

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

    private boolean checkAccount(Address... toCheck){
        boolean res = true;
        for (Address cur : toCheck){
            res = res && ledger.containsKey(cur);
        }
        return res;
    }

    public boolean mint(Address receiver, long tokens){
        if (BlockchainRuntime.getSender().equals(this.minter)){
            long receiverBalance = this.ledger.get(receiver);
            if ((tokens > 0) && (receiverBalance + tokens > 0)){
                this.ledger.put(receiver, receiverBalance + tokens);
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

        if (!checkAccount(tokenOwner)){
            return -1;
        }

        return this.ledger.get(tokenOwner);
    }

    public long allowance(Address tokenOwner, Address spender){
        if (!checkAccount(tokenOwner, spender)){
            return -1;
        }

        if (!this.allowance.containsKey(tokenOwner)){
            return 0;
        }

        if (!this.allowance.get(tokenOwner).containsKey(spender)){
            return 0;
        }

        return this.allowance.get(tokenOwner).get(spender);
    }

    public boolean transfer(Address receiver, long tokens){
        Address sender = BlockchainRuntime.getSender();

        if (!checkAccount(sender, receiver)){
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
        Address sender = BlockchainRuntime.getSender();

        if (!checkAccount(sender, spender)){
            BlockchainRuntime.log("PepeCoin".getBytes(), "Approve: invalid account".getBytes());
            return false;
        }

        if (!this.allowance.containsKey(sender)){
            AionMap<Address, Long> newEntry = new AionMap<>();
            this.allowance.put(sender, newEntry);
        }

        this.allowance.get(sender).put(spender, tokens);

        return true;
    }

    public boolean transferFrom(Address from, Address to, long tokens){
        Address sender = BlockchainRuntime.getSender();

        if (!checkAccount(sender, from, to)){
            BlockchainRuntime.log("PepeCoin".getBytes(), "TransferFrom: invalid account".getBytes());
            return false;
        }

        long fromBalance = this.ledger.get(from);
        long toBalance = this.ledger.get(to);

        long limit = allowance(from, sender);

        if ((fromBalance > tokens) && (limit > tokens) && (toBalance + tokens > 0)){
            this.ledger.put(from, fromBalance - tokens);
            this.allowance.get(from).put(sender, limit - tokens);
            this.ledger.put(to, toBalance + tokens);
            return true;
        }

        return false;
    }
}
