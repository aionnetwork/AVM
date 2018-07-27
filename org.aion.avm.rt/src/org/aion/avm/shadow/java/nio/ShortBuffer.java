package org.aion.avm.shadow.java.nio;

import org.aion.avm.internal.IHelper;

public abstract class ShortBuffer extends Buffer {

    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    ShortBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}
