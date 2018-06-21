package org.aion.avm.api;

public interface Block {

    Address getCoinbase();

    long getNumber();

    long getTimestamp();
}
