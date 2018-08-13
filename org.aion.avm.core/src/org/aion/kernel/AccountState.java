package org.aion.kernel;

import java.math.BigInteger;

public class AccountState {

    public BigInteger balance;

    public long nonce;

    public AccountState() {
        this(BigInteger.ZERO, 0);
    }

    public AccountState(BigInteger balance, long nonce) {
        this.balance = balance;
        this.nonce = nonce;
    }
}
