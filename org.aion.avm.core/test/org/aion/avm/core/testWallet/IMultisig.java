package org.aion.avm.core.testWallet;

import org.aion.avm.rt.Address;


//interface contract for multisig proxy contracts; see below for docs.
public interface IMultisig {
    // EXTERNAL
    public void changeOwner(IFutureRuntime runtime, Address from, Address to);
    // EXTERNAL
    public byte[] execute(IFutureRuntime runtime, Address to, long value, byte[] data);

    public boolean confirm(IFutureRuntime runtime, byte[] h);
}
