package org.aion.kernel;

import org.aion.avm.core.util.ByteArrayWrapper;

import java.util.HashMap;
import java.util.Map;

public class AccountState {

    public long balance = 0;

    public long nonce = 0;

    public byte[] code = null;

    public Map<ByteArrayWrapper, byte[]> storage = new HashMap<>();

    public AccountState() {
    }

    public AccountState(long balance, long nonce) {
        this.balance = balance;
        this.nonce = nonce;
    }
}
