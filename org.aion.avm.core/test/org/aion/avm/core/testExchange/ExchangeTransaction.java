package org.aion.avm.core.testExchange;

import org.aion.avm.api.Address;

public class ExchangeTransaction {

    private Address from;
    private Address to;
    private String coin;
    private long amount;

    ExchangeTransaction(String coin, Address from, Address to, long amount){
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

    public String getCoin() {
        return coin;
    }

    public long getAmount() {
        return amount;
    }
}
