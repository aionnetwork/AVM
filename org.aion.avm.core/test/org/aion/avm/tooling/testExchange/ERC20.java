package org.aion.avm.tooling.testExchange;

import org.aion.avm.api.Address;

public interface ERC20 {

    String name();

    String symbol();

    int decimals();

    long totalSupply();

    long balanceOf(Address tokenOwner);

    long allowance(Address tokenOwner, Address spender);

    boolean transfer(Address to, long tokens);

    boolean approve(Address spender, long tokens);

    boolean transferFrom(Address spender, Address to, long tokens);

    // NOT PART OF ERC20
    boolean mint(Address receiver, long tokens);
}
