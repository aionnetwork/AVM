package org.aion.avm.rt;

import org.aion.avm.java.lang.String;

public interface IAvmProxy {
    IAvmResultProxy deploy(byte[] module, String codeVersion, BlockchainRuntime rt);

    IAvmResultProxy removeDapp(Address beneficiary, BlockchainRuntime rt);
}

