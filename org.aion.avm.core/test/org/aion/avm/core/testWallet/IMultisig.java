package org.aion.avm.core.testWallet;

import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;


//interface contract for multisig proxy contracts; see below for docs.
public interface IMultisig {
    // EXTERNAL
    public void changeOwner(BlockchainRuntime runtime, Address from, Address to);
    // EXTERNAL
    public byte[] execute(BlockchainRuntime runtime, Address to, long value, byte[] data);

    public boolean confirm(BlockchainRuntime runtime, byte[] h);
}
