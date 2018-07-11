package org.aion.avm.core.testICO;

import org.aion.avm.api.Address;

public interface IAionToken {

    long totalSupply();

    long balanceOf(Address tokenOwner);

    long allowance(Address tokenOwner, Address spender);

    boolean transfer(Address to, long tokens);

    boolean approve(Address spender, long tokens);

    boolean transferFrom(Address spender, Address to, long tokens);

}
