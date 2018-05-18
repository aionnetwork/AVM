package com.example.twoclasses;

import org.aion.avm.rt.BlockchainRuntime;
import org.aion.avm.rt.Contract;

public class Main implements Contract {
    @Override
    public byte[] run(byte[] input, BlockchainRuntime rt) {
        return new byte[0];
    }
}
