package org.aion.kernel;

import java.math.BigInteger;

public class AccountState {

    public long balance;

    public long nonce;

    public AccountState() {
        this(0, 0);
    }

    public AccountState(long balance, long nonce) {
        this.balance = balance;
        this.nonce = nonce;
    }
}
